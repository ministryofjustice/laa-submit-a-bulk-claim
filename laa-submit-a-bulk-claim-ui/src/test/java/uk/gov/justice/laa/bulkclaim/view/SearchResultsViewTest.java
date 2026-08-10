package uk.gov.justice.laa.bulkclaim.view;

import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.controller.SearchController;
import uk.gov.justice.laa.bulkclaim.dto.PaginationLinks;
import uk.gov.justice.laa.bulkclaim.dto.PaginationPageLink;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionOutcomeFilter;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;
import uk.gov.justice.laa.bulkclaim.util.PaginationLinksBuilder;
import uk.gov.justice.laa.bulkclaim.util.PaginationUtil;
import uk.gov.justice.laa.bulkclaim.util.SubmissionPeriodUtil;
import uk.gov.justice.laa.bulkclaim.validation.SubmissionSearchValidator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionBase;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

@WebMvcTest(SearchController.class)
class SearchResultsViewTest extends ViewTestBase {

  private static final int PAGE_SIZE = 50;
  private static final String OFFICE = "12345";
  private static final String SUBMISSION_STATUSES = SubmissionOutcomeFilter.ALL.name();

  @MockitoBean DataClaimsRestClient claimsRestService;
  @MockitoBean SubmissionSearchValidator submissionSearchValidator;
  @MockitoBean PaginationUtil paginationUtil;
  @MockitoBean OidcAttributeUtils oidcAttributeUtils;
  @MockitoBean PaginationLinksBuilder paginationLinksBuilder;

  @MockitoBean("submissionPeriodUtil") // Naming required as this bean is used in thymeleaf
  SubmissionPeriodUtil submissionPeriodUtil;

  SearchResultsViewTest() {
    this.mapping = "/submissions/search/results";
  }

  @Test
  void searchResultsHasDefaultSortableHeaders() {
    mockSearchResults(0, 1, 1);
    var doc = renderDocumentWithParams(baseSearchResultsParams());

    Elements headers = getTableHeaders(doc);

    assertTableHeaderIsSortable(
        headers.get(0),
        "descending",
        "Date submitted",
        "/submissions/search/results?page=0&offices=%s&submissionStatuses=%s&sort=createdOn,asc"
            .formatted(OFFICE, SUBMISSION_STATUSES));
    assertTableHeaderIsSortable(
        headers.get(1),
        "none",
        "Office account",
        "/submissions/search/results?page=0&offices=%s&submissionStatuses=%s&sort=officeAccountNumber,asc"
            .formatted(OFFICE, SUBMISSION_STATUSES));
    assertTableHeaderIsSortable(
        headers.get(2),
        "none",
        "Area of law",
        "/submissions/search/results?page=0&offices=%s&submissionStatuses=%s&sort=areaOfLaw,asc"
            .formatted(OFFICE, SUBMISSION_STATUSES));
    assertTableHeaderIsSortable(
        headers.get(3),
        "none",
        "Submission period",
        "/submissions/search/results?page=0&offices=%s&submissionStatuses=%s&sort=submissionPeriod,asc"
            .formatted(OFFICE, SUBMISSION_STATUSES));
    assertTableHeaderIsSortable(
        headers.get(4),
        "none",
        "Status",
        "/submissions/search/results?page=0&offices=%s&submissionStatuses=%s&sort=status,asc"
            .formatted(OFFICE, SUBMISSION_STATUSES));
  }

  static Stream<Arguments> fieldIsSortableArgs() {
    return Stream.of(
        of("desc", 0, "descending", "asc"),
        of("asc", 0, "ascending", "desc"),
        of("asc", 5, "ascending", "desc"));
  }

  @ParameterizedTest
  @MethodSource("fieldIsSortableArgs")
  void searchResultsDateSubmittedIsSortable(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    searchResultsFieldIsSortable(
        0,
        "createdOn",
        "Date submitted",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("fieldIsSortableArgs")
  void searchResultsOfficeAccountIsSortable(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    searchResultsFieldIsSortable(
        1,
        "officeAccountNumber",
        "Office account",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("fieldIsSortableArgs")
  void searchResultsAreaOfLawIsSortable(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    searchResultsFieldIsSortable(
        2,
        "areaOfLaw",
        "Area of law",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("fieldIsSortableArgs")
  void searchResultsSubmissionPeriodIsSortable(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    searchResultsFieldIsSortable(
        3,
        "submissionPeriod",
        "Submission period",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("fieldIsSortableArgs")
  void searchResultsStatusIsSortable(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    searchResultsFieldIsSortable(
        4,
        "status",
        "Status",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("paginationRendersArgs")
  void searchResultsRendersPaginationAcrossPages(
      int currentPage,
      int totalPages,
      List<Integer> expectedVisiblePages,
      boolean expectedPreviousLink,
      boolean expectedNextLink,
      int expectedEllipsesCount) {
    mockSearchResults(currentPage, totalPages, totalPages * PAGE_SIZE);

    var doc = renderSearchResultsWithSort(currentPage, "createdOn,desc");

    assertPaginationRenders(
        doc,
        "page",
        currentPage,
        expectedVisiblePages,
        expectedPreviousLink,
        expectedNextLink,
        expectedEllipsesCount);
  }

  private void searchResultsFieldIsSortable(
      int fieldHeaderIndex,
      String fieldKey,
      String fieldName,
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    mockSearchResults(currentPage, 10, 100);
    var doc =
        renderSearchResultsWithSort(currentPage, "%s,%s".formatted(fieldKey, currentDirection));

    Elements headers = getTableHeaders(doc);

    assertTableHeaderIsSortable(
        headers.get(fieldHeaderIndex),
        expectedAriaDirection,
        fieldName,
        "/submissions/search/results?page=0&offices=%s&submissionStatuses=%s&sort=%s,%s"
            .formatted(OFFICE, SUBMISSION_STATUSES, fieldKey, expectedLinkDirection));
  }

  private static PaginationLinks buildSearchPaginationLinks(int currentPage, int totalPages) {
    List<PaginationPageLink> pageLinks = new ArrayList<>();
    for (int pageNumber = 0; pageNumber < totalPages; pageNumber++) {
      pageLinks.add(new PaginationPageLink(pageNumber, searchResultsPageHref(pageNumber)));
    }

    String previousHref = currentPage > 0 ? searchResultsPageHref(currentPage - 1) : null;
    String nextHref = currentPage < totalPages - 1 ? searchResultsPageHref(currentPage + 1) : null;
    return new PaginationLinks(previousHref, nextHref, pageLinks);
  }

  private static String searchResultsPageHref(int pageNumber) {
    return "/submissions/search/results?page=%s&offices=%s&submissionStatuses=%s&sort=createdOn,desc"
        .formatted(pageNumber, OFFICE, SUBMISSION_STATUSES);
  }

  private void mockSearchResults(int currentPage, int totalPages, int totalElements) {
    var response = buildSearchResultsResponse(currentPage, totalPages, totalElements);
    var pagination = buildPagination(currentPage, totalPages, totalElements);
    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("1"));
    when(claimsRestService.search(anyList(), any(), any(), any(), anyInt(), anyInt(), any()))
        .thenReturn(Mono.just(response));
    when(paginationUtil.fromSubmissionsResultSet(response, currentPage, PAGE_SIZE))
        .thenReturn(pagination);
    when(paginationLinksBuilder.build(any(), any(), any(), any(Object[].class)))
        .thenReturn(buildSearchPaginationLinks(currentPage, totalPages));
  }

  private SubmissionsResultSet buildSearchResultsResponse(
      int currentPage, int totalPages, int totalElements) {
    var submission = SubmissionBase.builder().submissionId(submissionId).build();
    return SubmissionsResultSet.builder()
        .content(List.of(submission))
        .totalElements(totalElements)
        .number(currentPage)
        .size(PAGE_SIZE)
        .totalPages(totalPages)
        .build();
  }

  private static Page buildPagination(int currentPage, int totalPages, int totalElements) {
    return Page.builder()
        .totalElements(totalElements)
        .number(currentPage)
        .size(PAGE_SIZE)
        .totalPages(totalPages)
        .build();
  }

  private static Map<String, String> baseSearchResultsParams() {
    return Map.of("offices", OFFICE, "submissionStatuses", SUBMISSION_STATUSES);
  }

  private static Map<String, String> searchResultsParams(int page, String sort) {
    var params = new HashMap<>(baseSearchResultsParams());
    params.put("page", String.valueOf(page));
    params.put("sort", sort);
    return params;
  }

  private org.jsoup.nodes.Document renderSearchResultsWithSort(int page, String sort) {
    return renderDocumentWithParams(searchResultsParams(page, sort));
  }
}
