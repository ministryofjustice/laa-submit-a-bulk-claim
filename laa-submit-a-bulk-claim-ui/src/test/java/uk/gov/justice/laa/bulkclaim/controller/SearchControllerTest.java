package uk.gov.justice.laa.bulkclaim.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper.getOidcUser;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.CRIME_LOWER;

import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import uk.gov.justice.laa.bulkclaim.util.SubmissionPeriodUtil;
import uk.gov.justice.laa.bulkclaim.validation.SubmissionSearchValidator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
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
  @Mock private SubmissionPeriodUtil submissionPeriodUtil;

  @InjectMocks private SearchController searchController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("Search GET should initialise query and default offices/status if not present")
  void searchShouldAddQueryIfNotPresent() {
    when(model.containsAttribute("submissionSearchQuery")).thenReturn(false);
    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("12345", "67890"));

    String view = searchController.search(model, sessionStatus, getOidcUser());

    ArgumentCaptor<SubmissionSearchQuery> queryCaptor =
        ArgumentCaptor.forClass(SubmissionSearchQuery.class);
    verify(model).addAttribute(eq("submissionSearchQuery"), queryCaptor.capture());
    assertEquals(List.of("12345", "67890"), queryCaptor.getValue().getOffices());
    assertEquals(SubmissionOutcomeFilter.SUCCEEDED, queryCaptor.getValue().getSubmissionStatuses());
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
            .areaOfLaw(CRIME_LOWER)
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
            null, null, "JAN-2024", CRIME_LOWER, List.of(), SubmissionOutcomeFilter.SUCCEEDED);

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

  @Test
  @DisplayName("Search GET should not add query to model if already present")
  void searchShouldNotOverrideQueryIfAlreadyPresent() {
    when(model.containsAttribute("submissionSearchQuery")).thenReturn(true);

    searchController.search(model, sessionStatus, getOidcUser());

    verify(model, never())
        .addAttribute(eq("submissionSearchQuery"), any(SubmissionSearchQuery.class));
  }

  @Test
  @DisplayName("Search results should trim whitespace submissionPeriod before calling API")
  void submissionsSearchResultsTrimsSubmissionPeriod() {
    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("1"));
    mockApiSuccess();

    var query = new SubmissionSearchQuery(0, null, "  JAN-2024  ", null, List.of("1"), null);
    searchController.submissionsSearchResults(query, model, getOidcUser(), sessionStatus, session);

    verify(claimsRestService)
        .search(eq(List.of("1")), eq("JAN-2024"), any(), any(), anyInt(), anyInt(), any());
  }

  @Test
  @DisplayName("Search results should pass null submission statuses through to API")
  void submissionsSearchResultsPassesNullSubmissionStatuses() {
    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("1"));
    mockApiSuccess();

    var query = new SubmissionSearchQuery(0, null, null, null, List.of("1"), null);
    searchController.submissionsSearchResults(query, model, getOidcUser(), sessionStatus, session);

    verify(claimsRestService)
        .search(eq(List.of("1")), any(), any(), isNull(), anyInt(), anyInt(), any());
  }

  @Nested
  @DisplayName("API parameter passing")
  class ApiParameterPassing {

    @Test
    @DisplayName("Searches when office is provided to API")
    void submissionsSearchResultsPassesNoOptionalFiltersToApi() {
      when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("1"));
      mockApiSuccess();

      var query = new SubmissionSearchQuery(0, null, null, null, List.of("1"), null);
      searchController.submissionsSearchResults(
          query, model, getOidcUser(), sessionStatus, session);

      verify(claimsRestService)
          .search(any(), isNull(), isNull(), isNull(), anyInt(), anyInt(), any());
    }

    @Test
    @DisplayName("Searches when submission period is provided to API")
    void submissionsSearchResultsPassesSubmissionPeriodToApi() {
      when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("1"));
      mockApiSuccess();

      var query = new SubmissionSearchQuery(0, null, "JAN-2024", null, List.of("1"), null);
      searchController.submissionsSearchResults(
          query, model, getOidcUser(), sessionStatus, session);

      verify(claimsRestService)
          .search(any(), eq("JAN-2024"), any(), any(), anyInt(), anyInt(), any());
    }

    @Test
    @DisplayName("Searches when area of law is provided to API")
    void submissionsSearchResultsPassesAreaOfLawToApi() {
      when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("1"));
      mockApiSuccess();

      var query = new SubmissionSearchQuery(0, null, null, CRIME_LOWER, List.of("1"), null);
      searchController.submissionsSearchResults(
          query, model, getOidcUser(), sessionStatus, session);

      verify(claimsRestService)
          .search(any(), any(), eq(CRIME_LOWER), any(), anyInt(), anyInt(), any());
    }

    @Test
    @DisplayName("Searches when submission status is provided to API")
    void submissionsSearchResultsPassesSubmissionStatusToApi() {
      when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("1"));
      mockApiSuccess();

      var query =
          new SubmissionSearchQuery(
              0, null, null, null, List.of("1"), SubmissionOutcomeFilter.SUCCEEDED);
      searchController.submissionsSearchResults(
          query, model, getOidcUser(), sessionStatus, session);

      verify(claimsRestService)
          .search(
              any(),
              any(),
              any(),
              eq(List.of(SubmissionStatus.VALIDATION_SUCCEEDED)),
              anyInt(),
              anyInt(),
              any());
    }

    @Test
    @DisplayName("Passes all parameters to API")
    void submissionsSearchResultsPassesAllParametersToApi() {
      when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("12345"));
      mockApiSuccess();

      var query =
          new SubmissionSearchQuery(
              0,
              null,
              "JAN-2024",
              CRIME_LOWER,
              List.of("12345"),
              SubmissionOutcomeFilter.SUCCEEDED);
      searchController.submissionsSearchResults(
          query, model, getOidcUser(), sessionStatus, session);

      verify(claimsRestService)
          .search(
              eq(List.of("12345")),
              eq("JAN-2024"),
              eq(CRIME_LOWER),
              eq(List.of(SubmissionStatus.VALIDATION_SUCCEEDED)),
              eq(0),
              eq(10),
              any());
    }
  }

  @Nested
  @DisplayName("Office security filtering")
  class OfficeSecurityFiltering {

    @Test
    @DisplayName("Strips offices from the query that do not belong to the user")
    void submissionsSearchResultsFiltersOfficesNotBelongingToUser() {
      when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("12345", "67890"));
      mockApiSuccess();

      var query = new SubmissionSearchQuery(0, null, null, null, List.of("12345", "1"), null);
      searchController.submissionsSearchResults(
          query, model, getOidcUser(), sessionStatus, session);

      verify(claimsRestService)
          .search(eq(List.of("12345")), any(), any(), any(), anyInt(), anyInt(), any());
    }

    @Test
    @DisplayName("Searches only the offices the user selected")
    void submissionsSearchResultsUsesOnlySelectedOfficesFromUserOffices() {
      when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("12345", "67890"));
      mockApiSuccess();

      var query = new SubmissionSearchQuery(0, null, null, null, List.of("12345"), null);
      searchController.submissionsSearchResults(
          query, model, getOidcUser(), sessionStatus, session);

      verify(claimsRestService)
          .search(eq(List.of("12345")), any(), any(), any(), anyInt(), anyInt(), any());
    }

    @Test
    @DisplayName("Searches with empty list when none of the query offices are owned by the user")
    void submissionsSearchResultsSearchesEmptyWhenNoQueryOfficesOwnedByUser() {
      when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("12345"));
      mockApiSuccess();

      var query = new SubmissionSearchQuery(0, null, null, null, List.of("1"), null);
      searchController.submissionsSearchResults(
          query, model, getOidcUser(), sessionStatus, session);

      verify(claimsRestService)
          .search(eq(Collections.emptyList()), any(), any(), any(), anyInt(), anyInt(), any());
    }
  }

  private void mockApiSuccess() {
    var response = new SubmissionsResultSet();
    response.setContent(Collections.emptyList());
    response.setNumber(0);
    response.setSize(10);
    response.setTotalPages(1);
    response.setTotalElements(0);
    when(claimsRestService.search(anyList(), any(), any(), any(), anyInt(), anyInt(), any()))
        .thenReturn(Mono.just(response));
    when(paginationUtil.fromSubmissionsResultSet(any(), anyInt(), anyInt()))
        .thenReturn(new Page().totalElements(0));
  }
}
