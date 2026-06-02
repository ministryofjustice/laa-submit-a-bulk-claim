package uk.gov.justice.laa.bulkclaim.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.client.HttpClientErrorException.BadRequest;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionOutcomeFilter;
import uk.gov.justice.laa.bulkclaim.dto.submission.search.SubmissionSearchQuery;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;
import uk.gov.justice.laa.bulkclaim.util.PaginationLinksBuilder;
import uk.gov.justice.laa.bulkclaim.util.PaginationUtil;
import uk.gov.justice.laa.bulkclaim.validation.SubmissionSearchValidator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
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
  @Mock private PaginationLinksBuilder paginationLinksBuilder;
  @Mock private OidcAttributeUtils oidcAttributeUtils;
  @Mock private SessionStatus sessionStatus;

  @InjectMocks private SearchController searchController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("Search GET should initialise query if not present")
  void searchShouldAddQueryIfNotPresent() {
    when(model.containsAttribute("submissionSearchQuery")).thenReturn(false);

    String view = searchController.search(model, sessionStatus, getOidcUser());

    verify(model).addAttribute(eq("submissionSearchQuery"), any(SubmissionSearchQuery.class));
    verify(sessionStatus).setComplete();
    assertEquals("pages/submissions-search", view);
  }

  @Test
  @DisplayName("Handle search should redirect to query if validation errors")
  void handleSearchShouldRedirectBackOnErrors() {
    when(bindingResult.hasErrors()).thenReturn(true);
    final SubmissionSearchQuery query =
        SubmissionSearchQuery.builder().submissionPeriod("01/01/2024").build();
    final Model localModel = new ExtendedModelMap();

    String view = searchController.handleSearch(getOidcUser(), query, bindingResult, localModel);

    assertEquals("pages/submissions-search", view);
    assertEquals(query, localModel.getAttribute("submissionSearchQuery"));
  }

  @Test
  @DisplayName("Handle search should redirect with query params when valid")
  void handleSearchShouldRedirectWithParamsOnSuccess() {
    when(bindingResult.hasErrors()).thenReturn(false);
    final SubmissionSearchQuery query =
        SubmissionSearchQuery.builder()
            .submissionPeriod("JAN-2024")
            .areaOfLaw(AreaOfLaw.CRIME_LOWER.getValue())
            .offices(List.of("12345"))
            .submissionStatuses(SubmissionOutcomeFilter.SUCCEEDED)
            .build();
    final Model localModel = new ExtendedModelMap();

    String view = searchController.handleSearch(getOidcUser(), query, bindingResult, localModel);

    assertEquals(
        "redirect:/submissions/search/results?page=0&submissionPeriod=JAN-2024&areaOfLaw=CRIME "
            + "LOWER&offices=12345&submissionStatuses=SUCCEEDED&sort=createdOn,desc",
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
    when(claimsRestService.search(anyList(), any(), any(), any(), anyInt(), anyInt(), any()))
        .thenReturn(Mono.just(response));
    when(paginationUtil.fromSubmissionsResultSet(response, 0, 10))
        .thenReturn(new Page().totalElements(1));

    var query =
        new SubmissionSearchQuery(
            null,
            null,
            "JAN-2024",
            AreaOfLaw.CRIME_LOWER.name(),
            List.of(),
            SubmissionOutcomeFilter.SUCCEEDED);

    String view =
        searchController.submissionsSearchResults(
            query, model, getOidcUser(), sessionStatus, session);

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
    when(claimsRestService.search(anyList(), any(), any(), any(), anyInt(), anyInt(), any()))
        .thenThrow(BadRequest.class);

    var query = SubmissionSearchQuery.builder().build();

    String view =
        searchController.submissionsSearchResults(
            query, model, getOidcUser(), sessionStatus, session);

    assertEquals("error", view);
  }

  @Test
  @DisplayName("Submissions search results should return error when generic exception is thrown")
  void submissionsSearchResultsShouldReturnErrorOnGenericException() {
    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("1"));
    when(claimsRestService.search(anyList(), any(), any(), any(), anyInt(), anyInt(), any()))
        .thenThrow(new RuntimeException("Boom"));

    var query = SubmissionSearchQuery.builder().build();

    String view =
        searchController.submissionsSearchResults(
            query, model, getOidcUser(), sessionStatus, session);

    assertEquals("error", view);
  }
}
