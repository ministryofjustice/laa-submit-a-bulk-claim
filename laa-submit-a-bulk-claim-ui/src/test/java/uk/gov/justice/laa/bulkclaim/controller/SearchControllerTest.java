package uk.gov.justice.laa.bulkclaim.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper.getOidcUser;

import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.client.HttpClientErrorException.BadRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionsSearchForm;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;
import uk.gov.justice.laa.bulkclaim.util.PaginationUtil;
import uk.gov.justice.laa.bulkclaim.validation.SubmissionSearchValidator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

@AutoConfigureMockMvc(addFilters = false)
class SearchControllerTest {

  @Mock private Model model;
  @Mock private BindingResult bindingResult;
  @Mock private HttpSession session;
  @Mock private SubmissionSearchValidator submissionSearchValidator;
  @Mock private DataClaimsRestClient claimsRestService;
  @Mock private PaginationUtil paginationUtil;
  @Mock private OidcAttributeUtils oidcAttributeUtils;
  @Mock private SessionStatus sessionStatus;

  @InjectMocks private SearchController searchController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("Search GET should initialise form if not present")
  void searchShouldAddFormIfNotPresent() {
    when(model.containsAttribute("submissionsSearchForm")).thenReturn(false);

    String view = searchController.search(model, sessionStatus);

    verify(model).addAttribute(eq("submissionsSearchForm"), any(SubmissionsSearchForm.class));
    verify(sessionStatus).setComplete();
    assertEquals("pages/submissions-search", view);
  }

  @Test
  @DisplayName("Handle search should redirect to form if validation errors")
  void handleSearchShouldRedirectBackOnErrors() {
    when(bindingResult.hasErrors()).thenReturn(true);
    final SubmissionsSearchForm form = new SubmissionsSearchForm("1234", "01/01/2024", null);
    final RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    String view = searchController.handleSearch(form, bindingResult, redirectAttributes);

    assertEquals("redirect:/submissions/search", view);
    assertEquals(form, redirectAttributes.getFlashAttributes().get("submissionsSearchForm"));
  }

  @Test
  @DisplayName("Handle search should redirect with query params when valid")
  void handleSearchShouldRedirectWithParamsOnSuccess() {
    when(bindingResult.hasErrors()).thenReturn(false);
    final SubmissionsSearchForm form =
        new SubmissionsSearchForm(
            "704b3dda-4aec-4883-a263-000d86511289", "01/01/2024", "02/01/2024");
    final RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

    String view = searchController.handleSearch(form, bindingResult, redirectAttributes);

    assertEquals(
        "redirect:/submissions/search/results?page=0&submissionId=704b3dda-4aec-4883-a263-000d86511289&submittedDateFrom=01/01/2024&submittedDateTo=02/01/2024",
        view);
  }

  @Test
  @DisplayName("Submissions search results should return results when API call succeeds")
  void submissionsSearchResultsShouldReturnResults() {
    final SubmissionsResultSet response = new SubmissionsResultSet();
    response.setContent(Collections.emptyList());
    response.setTotalElements(1);
    response.setNumber(0);
    response.setSize(10);
    response.setTotalPages(1);

    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("1"));
    when(claimsRestService.search(anyList(), any(), any(), any(), anyInt(), anyInt()))
        .thenReturn(Mono.just(response));
    when(paginationUtil.fromSubmissionsResultSet(response, 0, 10))
        .thenReturn(new Page().totalElements(1));

    String view =
        searchController.submissionsSearchResults(
            0, "1234", "01/01/2024", "02/01/2024", model, getOidcUser(), sessionStatus, session);

    verify(sessionStatus).setComplete();
    verify(model).addAttribute(eq("pagination"), any(Page.class));
    verify(model).addAttribute("submissions", response);
    verify(session).setAttribute("submissions", response);
    assertEquals("pages/submissions-search-results", view);
  }

  @Test
  @DisplayName(
      "Submissions search results should return error when HttpClientErrorException is thrown")
  void submissionsSearchResultsShouldReturnErrorOnHttpClientError() {
    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("1"));
    when(claimsRestService.search(anyList(), any(), any(), any(), anyInt(), anyInt()))
        .thenThrow(BadRequest.class);

    String view =
        searchController.submissionsSearchResults(
            0, "1234", null, null, model, getOidcUser(), sessionStatus, session);

    assertEquals("error", view);
  }

  @Test
  @DisplayName("Submissions search results should return error when generic exception is thrown")
  void submissionsSearchResultsShouldReturnErrorOnGenericException() {
    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("1"));
    when(claimsRestService.search(anyList(), any(), any(), any(), anyInt(), anyInt()))
        .thenThrow(new RuntimeException("Boom"));

    String view =
        searchController.submissionsSearchResults(
            0, "1234", null, null, model, getOidcUser(), sessionStatus, session);

    assertEquals("error", view);
  }

  @Test
  @DisplayName("Submissions search results should handle invalid date formats gracefully")
  void submissionsSearchResultsShouldHandleInvalidDates() {
    final SubmissionsResultSet response = new SubmissionsResultSet();
    response.setContent(Collections.emptyList());

    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("1"));
    when(claimsRestService.search(anyList(), any(), isNull(), isNull(), anyInt(), anyInt()))
        .thenReturn(Mono.just(response));
    when(paginationUtil.fromSubmissionsResultSet(response, 0, 10))
        .thenReturn(new Page().totalElements(0));

    String view =
        searchController.submissionsSearchResults(
            0, "1234", "invalid-date", "bad-date", model, getOidcUser(), sessionStatus, session);

    verify(model).addAttribute(eq("pagination"), any(Page.class));
    verify(model).addAttribute("submissions", response);
    verify(session).setAttribute("submissions", response);
    assertEquals("pages/submissions-search-results", view);
  }
}
