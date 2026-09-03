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
  private static final UUID THIRD_CLAIM_ID = UUID.randomUUID();
  private static final UUID FOURTH_CLAIM_ID = UUID.randomUUID();

  @BeforeEach
  void beforeEachMediationViewTest() {
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
    SubmissionClaimRow assessedClaim =
        SubmissionClaimRow.builder()
            .id(THIRD_CLAIM_ID)
            .lineNumber(3)
            .ufn("011015/127")
            .ucn("UCN127")
            .clientForename("Third")
            .clientSurname("Assessed")
            .client2Forename("ThirdTwo")
            .client2Surname("Surname3")
            .client2Ucn("UCN227")
            .concludedOrClaimedDate(LocalDate.of(2025, 5, 13))
            .feeCode("MASS")
            .status("ASSESSED")
            .escapeCase(false)
            .calculatedValue(new BigDecimal("75.00"))
            .build();
    SubmissionClaimRow voidedClaim =
        SubmissionClaimRow.builder()
            .id(FOURTH_CLAIM_ID)
            .lineNumber(4)
            .ufn("011015/128")
            .ucn("UCN128")
            .clientForename("Fourth")
            .clientSurname("Voided")
            .client2Forename("FourthTwo")
            .client2Surname("Surname4")
            .client2Ucn("UCN228")
            .concludedOrClaimedDate(LocalDate.of(2025, 5, 14))
            .feeCode("MVOI")
            .status("VOIDED")
            .escapeCase(false)
            .calculatedValue(new BigDecimal("0.00"))
            .build();
    Page claimsPagination =
        Page.builder().number(0).totalPages(1).size(PAGE_SIZE).totalElements(4).build();
    when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt(), anyString()))
        .thenReturn(
            new SubmissionClaimsDetails(
                List.of(firstClaim, secondClaim, assessedClaim, voidedClaim),
                claimsPagination,
                new BigDecimal("5125.00")));

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
    assertThat(summaryList).hasSize(6);
    assertRowContainsValues(
        summaryList.get(0), "Submission date and time", "1 Jan 2025 at 10:10AM");
    assertRowContainsValues(summaryList.get(1), "Account", "123456");
    assertRowContainsValues(summaryList.get(2), "Area of law", "Mediation");
    assertRowContainsValues(summaryList.get(3), "Submission period", "MAY-2025");
    assertRowContainsValues(
        summaryList.get(4), "Submission reference", String.valueOf(submissionId));
    assertRowContainsValues(summaryList.get(5), "Calculated bulk claim value", "£5,125.00");

    // Mediation claims are never escaped, so no warning banner is shown
    assertThat(doc.selectFirst(".moj-alert--warning")).isNull();

    var navItemTexts = doc.select(".moj-sub-navigation__item").eachText();
    assertThat(navItemTexts).contains("Claims (4)", "Messages (0)", "Matter starts (5)");

    var rows = doc.select(".govuk-table__body tr");
    assertThat(rows).hasSize(4);

    var firstRowCells = rows.get(0).select("td");
    assertThat(firstRowCells.get(0).text()).isEqualTo("First Surname");
    assertThat(firstRowCells.get(1).text()).isEqualTo("UCN125");
    assertThat(firstRowCells.get(2).text()).isEqualTo("FirstTwo Surname2");
    assertThat(firstRowCells.get(3).text()).isEqualTo("UCN225");
    assertThat(firstRowCells.get(4).text()).isEqualTo("ASSA");
    assertThat(firstRowCells.get(5).text()).isEqualTo("£50.00");
    assertThat(firstRowCells.get(6).text()).isEqualTo("No");
    assertThat(selectFirst(rows.get(0), ".govuk-tag--green").text()).isEqualTo("Accepted");
    assertThat(selectFirst(rows.get(0), "a").attr("href"))
        .contains("/submissions/%s/claims/%s".formatted(submissionId, FIRST_CLAIM_ID));

    var secondRowCells = rows.get(1).select("td");
    assertThat(secondRowCells.get(0).text()).isEqualTo("Second Surname");
    assertThat(secondRowCells.get(1).text()).isEqualTo("UCN126");
    assertThat(secondRowCells.get(2).text()).isEqualTo("SecondTwo Surname2");
    assertThat(secondRowCells.get(3).text()).isEqualTo("UCN226");
    assertThat(secondRowCells.get(4).text()).isEqualTo("ASST");
    assertThat(secondRowCells.get(5).text()).isEqualTo("£5,000.00");
    assertThat(secondRowCells.get(6).text()).isEqualTo("No");
    assertThat(selectFirst(rows.get(1), ".govuk-tag--yellow").text()).isEqualTo("Amended");
    assertThat(selectFirst(rows.get(1), "a").attr("href"))
        .contains("/submissions/%s/claims/%s".formatted(submissionId, SECOND_CLAIM_ID));

    var assessedRowCells = rows.get(2).select("td");
    assertThat(assessedRowCells.get(0).text()).isEqualTo("Third Assessed");
    assertThat(assessedRowCells.get(1).text()).isEqualTo("UCN127");
    assertThat(assessedRowCells.get(2).text()).isEqualTo("ThirdTwo Surname3");
    assertThat(assessedRowCells.get(3).text()).isEqualTo("UCN227");
    assertThat(assessedRowCells.get(4).text()).isEqualTo("MASS");
    assertThat(assessedRowCells.get(5).text()).isEqualTo("£75.00");
    assertThat(assessedRowCells.get(6).text()).isEqualTo("No");
    assertThat(selectFirst(rows.get(2), ".govuk-tag--blue").text()).isEqualTo("Assessed");
    assertThat(selectFirst(rows.get(2), "a").attr("href"))
        .contains("/submissions/%s/claims/%s".formatted(submissionId, THIRD_CLAIM_ID));

    var voidedRowCells = rows.get(3).select("td");
    assertThat(voidedRowCells.get(0).text()).isEqualTo("Fourth Voided");
    assertThat(voidedRowCells.get(1).text()).isEqualTo("UCN128");
    assertThat(voidedRowCells.get(2).text()).isEqualTo("FourthTwo Surname4");
    assertThat(voidedRowCells.get(3).text()).isEqualTo("UCN228");
    assertThat(voidedRowCells.get(4).text()).isEqualTo("MVOI");
    assertThat(voidedRowCells.get(5).text()).isEqualTo("£0.00");
    assertThat(voidedRowCells.get(6).text()).isEqualTo("No");
    assertThat(selectFirst(rows.get(3), ".govuk-tag--red").text()).isEqualTo("Voided");
    assertThat(selectFirst(rows.get(3), "a").attr("href"))
        .contains("/submissions/%s/claims/%s".formatted(submissionId, FOURTH_CLAIM_ID));
  }

  @Test
  void acceptedSubmissionPageShowsDownloadAndPrintActions() {
    var doc = renderDocument();

    assertPageHasSecondaryButton(doc, "Download claims");

    var exportButton = selectFirst(doc, "#export-button");
    assertThat(exportButton.attr("href"))
        .contains("/submissions/%s/export".formatted(submissionId))
        .contains("office=123456")
        .contains("areaOfLaw=MEDIATION");

    assertThat(
            selectFirst(doc, "[data-module=laa-print-button]").attr("data-print-action-container"))
        .isEqualTo("secondary-action-container");
  }

  @Test
  void checkMatterStartsTabContainsMatterStarts() {
    Document doc = renderDocumentWithParams(Map.of("navTab", "MATTER_STARTS"));

    var matterStartsContainer = selectFirst(doc, "#matter-starts").parent();
    var summaryList = matterStartsContainer.select(".govuk-summary-list__row");
    assertThat(summaryList).hasSize(1);
    assertThat(selectFirst(summaryList.get(0), ".govuk-summary-list__key").text())
        .isEqualTo("Mediation type MDAC");
    assertThat(selectFirst(summaryList.get(0), ".govuk-summary-list__value").text()).isEqualTo("5");
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
