package uk.gov.justice.laa.payments.submit.view.submissiondetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.MEDIATION;
import static uk.gov.justice.laa.payments.submit.controller.ControllerTestHelper.OIDC_USER;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.payments.submit.constants.ViewSubmissionNavigationTab;
import uk.gov.justice.laa.payments.submit.dto.submission.SubmissionMatterStartsRow;
import uk.gov.justice.laa.payments.submit.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.SubmissionClaimRow;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.SubmissionClaimsDetails;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessagesSource;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessagesSummary;

class SubmissionDetailsMediationViewTest extends SubmissionDetailsViewTestBase {

  private static final UUID FIRST_CLAIM_ID = UUID.randomUUID();
  private static final UUID SECOND_CLAIM_ID = UUID.randomUUID();

  @BeforeEach
  void beforeEach() {
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder()
            .submissionId(submissionId)
            .status(SubmissionStatus.VALIDATION_SUCCEEDED)
            .officeAccountNumber(OFFICE_CODE)
            .areaOfLaw(MEDIATION)
            .build();
    when(submissionService.getSubmission(submissionId, OIDC_USER)).thenReturn(submissionResponse);

    when(submissionSummaryBuilder.build(any()))
        .thenReturn(
            new SubmissionSummary(
                submissionId,
                "Submitted",
                LocalDate.of(2025, 5, 1),
                OFFICE_CODE,
                BigDecimal.ZERO,
                MEDIATION.getValue(),
                OffsetDateTime.of(2025, 1, 1, 10, 10, 0, 0, ZoneOffset.UTC)));

    // Mediation claims are never escaped
    SubmissionClaimRow firstClaim =
        SubmissionClaimRow.builder()
            .id(FIRST_CLAIM_ID)
            .lineNumber(1)
            .ufn("011015/125")
            .ucn("UCN125")
            .clientForename("First")
            .clientSurname("Surname")
            .client2Forename("FirstTwo")
            .client2Surname("Surname2")
            .client2Ucn("UCN225")
            .concludedOrClaimedDate(LocalDate.of(2025, 5, 10))
            .feeCode("ASSA")
            .status("ACCEPTED")
            .escapeCase(false)
            .calculatedValue(new BigDecimal("50.00"))
            .build();
    SubmissionClaimRow secondClaim =
        SubmissionClaimRow.builder()
            .id(SECOND_CLAIM_ID)
            .lineNumber(2)
            .ufn("011015/126")
            .ucn("UCN126")
            .clientForename("Second")
            .clientSurname("Surname")
            .client2Forename("SecondTwo")
            .client2Surname("Surname2")
            .client2Ucn("UCN226")
            .concludedOrClaimedDate(LocalDate.of(2025, 5, 12))
            .feeCode("ASST")
            .status("AMENDED")
            .escapeCase(false)
            .calculatedValue(new BigDecimal("5000.00"))
            .build();
    Page claimsPagination =
        Page.builder().number(0).totalPages(1).size(PAGE_SIZE).totalElements(2).build();
    when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt(), anyString()))
        .thenReturn(
            new SubmissionClaimsDetails(
                List.of(firstClaim, secondClaim), claimsPagination, new BigDecimal("5050.00")));

    Page messagesPagination =
        Page.builder().number(0).totalPages(0).size(PAGE_SIZE).totalElements(0).build();
    when(submissionMessagesBuilder.build(any(), any(), any(), any(), anyInt(), anyInt(), any()))
        .thenReturn(new MessagesSummary(List.of(), 0, 0, messagesPagination, MessagesSource.CLAIM));

    when(submissionMatterStartsDetailsBuilder.build(any()))
        .thenReturn(List.of(new SubmissionMatterStartsRow("Mediation type MDAC", 5)));
  }

  @Test
  void renderMediationSubmissionDetails() {
    Document doc = renderDocument();

    // Summary
    var summaryList = getFirstSummaryList(doc);
    Assertions.assertThat(summaryList).hasSize(6);
    assertRowContainsValues(summaryList.get(2), "Area of law", "Mediation");
    assertRowContainsValues(summaryList.get(5), "Calculated bulk claim value", "£5,050.00");

    // Mediation claims are never escaped, so no warning banner is shown
    assertThat(doc.selectFirst(".moj-alert--warning")).isNull();

    var navItemTexts = doc.select(".moj-sub-navigation__item").eachText();
    assertThat(navItemTexts).contains("Claims (2)", "Messages (0)", "Matter starts (5)");

    var rows = doc.select(".govuk-table__body tr");
    assertThat(rows).hasSize(2);

    var firstRowCells = rows.get(0).select("td");
    assertThat(firstRowCells.get(0).text()).isEqualTo("First Surname");
    assertThat(firstRowCells.get(1).text()).isEqualTo("UCN125");
    assertThat(firstRowCells.get(2).text()).isEqualTo("FirstTwo Surname2");
    assertThat(firstRowCells.get(3).text()).isEqualTo("UCN225");
    assertThat(firstRowCells.get(4).text()).isEqualTo("ASSA");
    assertThat(firstRowCells.get(5).text()).isEqualTo("£50.00");
    assertThat(firstRowCells.get(6).text()).isEqualTo("No");
    Assertions.assertThat(selectFirst(rows.get(0), ".govuk-tag--green").text())
        .isEqualTo("Accepted");
    Assertions.assertThat(selectFirst(rows.get(0), "a").attr("href"))
        .contains("/submissions/%s/claims/%s".formatted(submissionId, FIRST_CLAIM_ID));

    var secondRowCells = rows.get(1).select("td");
    assertThat(secondRowCells.get(0).text()).isEqualTo("Second Surname");
    assertThat(secondRowCells.get(1).text()).isEqualTo("UCN126");
    assertThat(secondRowCells.get(2).text()).isEqualTo("SecondTwo Surname2");
    assertThat(secondRowCells.get(3).text()).isEqualTo("UCN226");
    assertThat(secondRowCells.get(4).text()).isEqualTo("ASST");
    assertThat(secondRowCells.get(5).text()).isEqualTo("£5,000.00");
    assertThat(secondRowCells.get(6).text()).isEqualTo("No");
    Assertions.assertThat(selectFirst(rows.get(1), ".govuk-tag--yellow").text())
        .isEqualTo("Amended");
    Assertions.assertThat(selectFirst(rows.get(1), "a").attr("href"))
        .contains("/submissions/%s/claims/%s".formatted(submissionId, SECOND_CLAIM_ID));
  }

  @Test
  void checkMatterStartsTabContainsMatterStarts() {
    Document doc = renderDocumentWithParams(Map.of("navTab", "MATTER_STARTS"));

    var matterStartsContainer = selectFirst(doc, "#matter-starts").parent();
    var summaryList = matterStartsContainer.select(".govuk-summary-list__row");
    Assertions.assertThat(summaryList).hasSize(1);
    Assertions.assertThat(selectFirst(summaryList.get(0), ".govuk-summary-list__key").text())
        .isEqualTo("Mediation type MDAC");
    Assertions.assertThat(selectFirst(summaryList.get(0), ".govuk-summary-list__value").text())
        .isEqualTo("5");
  }

  @Test
  void viewHasSortableClaimHeaders() {
    mockClaims(SubmissionClaimRow.builder().build());
    var doc = renderDocument();
    Elements headers = getTableHeaders(doc);
    assertTableHeaderIsSortable(
        headers.get(0), "none", "Client 1 name", claimSortLink("client_forename"));
    assertTableHeaderIsSortable(
        headers.get(1), "none", "Client 1 UCN", claimSortLink("unique_client_number"));
    assertTableHeaderIsSortable(
        headers.get(2), "none", "Client 2 name", claimSortLink("client_2_forename"));
    assertTableHeaderIsSortable(
        headers.get(3), "none", "Client 2 UCN", claimSortLink("client_2_ucn"));
    assertTableHeaderIsSortable(headers.get(4), "none", "Fee code", claimSortLink("fee_code"));
    assertTableHeaderIsSortable(
        headers.get(5), "none", "Initial calculated value", claimSortLink("total_amount"));
    assertTableHeaderIsSortable(
        headers.get(6), "none", "Escape case", claimSortLink("escape_case_flag"));
    assertTableHeaderIsSortable(
        headers.get(7), "none", "Status", claimSortLink("derived_claim_status"));
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClient1NameClaimFieldIsSortable(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        0,
        "client_forename",
        "Client 1 name",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClient1UcnClaimFieldIsSortable(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        1,
        "unique_client_number",
        "Client 1 UCN",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClient2NameClaimFieldIsSortable(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        2,
        "client_2_forename",
        "Client 2 name",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClient2UcnClaimFieldIsSortable(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        3,
        "client_2_ucn",
        "Client 2 UCN",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailFeeCodeClaimFieldIsSortable(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        4,
        "fee_code",
        "Fee code",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailInitialCalculatedValueClaimFieldIsSortable(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        5,
        "total_amount",
        "Initial calculated value",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailEscapeCaseFlagClaimFieldIsSortable(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        6,
        "escape_case_flag",
        "Escape case",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailStatusClaimFieldIsSortable(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        7,
        "derived_claim_status",
        "Status",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  private void assertClaimFieldIsSortable(
      int headerIndex,
      String fieldKey,
      String fieldName,
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    mockClaimsPagination(pagination(0, 1), "client_forename,desc");
    var doc =
        renderDocumentWithParams(
            Map.of(
                "page",
                String.valueOf(currentPage),
                "sort",
                "%s,%s".formatted(fieldKey, currentDirection)));
    Elements headers = getTableHeaders(doc);
    assertTableHeaderIsSortable(
        headers.get(headerIndex),
        expectedAriaDirection,
        fieldName,
        "/submissions/%s?navTab=CLAIM_DETAILS&page=0&sort=%s,%s"
            .formatted(submissionId, fieldKey, expectedLinkDirection));
  }

  private String claimSortLink(String field) {
    return "/submissions/%s?navTab=CLAIM_DETAILS&page=0&sort=%s,asc".formatted(submissionId, field);
  }

  private void mockClaims(SubmissionClaimRow claimRow) {
    Page pagination = Page.builder().totalPages(1).totalElements(1).number(0).size(10).build();
    when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt(), anyString()))
        .thenReturn(new SubmissionClaimsDetails(List.of(claimRow), pagination, BigDecimal.ONE));
  }

  private void mockClaimsPagination(Page claimPagination, String defaultSort) {
    when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt(), anyString()))
        .thenReturn(
            new SubmissionClaimsDetails(
                List.of(SubmissionClaimRow.builder().build()), claimPagination, BigDecimal.ONE));
    when(paginationLinksBuilder.build(any(), eq(claimPagination), eq("page"), any(Object[].class)))
        .thenReturn(
            buildSubmissionDetailPaginationLinks(
                submissionId,
                claimPagination.getNumber(),
                claimPagination.getTotalPages(),
                "page",
                ViewSubmissionNavigationTab.CLAIM_DETAILS,
                defaultSort));
  }
}
