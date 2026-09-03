package uk.gov.justice.laa.payments.submit.view.submissiondetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.LEGAL_HELP;
import static uk.gov.justice.laa.payments.submit.controller.ControllerTestHelper.OIDC_USER;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessageRow;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessagesSource;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessagesSummary;

class SubmissionDetailsLegalHelpViewTest extends SubmissionDetailsViewTestBase {

  private static final UUID ESCAPED_CLAIM_ID = UUID.randomUUID();
  private static final UUID FIXED_FEE_CLAIM_ID = UUID.randomUUID();
  private static final UUID ASSESSED_CLAIM_ID = UUID.randomUUID();
  private static final UUID VOIDED_CLAIM_ID = UUID.randomUUID();

  @BeforeEach
  void beforeEachLegalHelpViewTest() {
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder()
            .submissionId(submissionId)
            .status(SubmissionStatus.VALIDATION_SUCCEEDED)
            .officeAccountNumber(OFFICE_CODE)
            .areaOfLaw(LEGAL_HELP)
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
                LEGAL_HELP.getValue(),
                OffsetDateTime.of(2025, 1, 1, 10, 10, 0, 0, ZoneOffset.UTC)));

    SubmissionClaimRow escapedClaim =
        SubmissionClaimRow.builder()
            .id(ESCAPED_CLAIM_ID)
            .lineNumber(1)
            .ufn("011015/125")
            .ucn("UCN125")
            .clientForename("First")
            .clientSurname("Escaped")
            .concludedOrClaimedDate(LocalDate.of(2025, 5, 10))
            .feeCode("FPB020")
            .status("ACCEPTED")
            .escapeCase(true)
            .calculatedValue(new BigDecimal("2000.00"))
            .build();
    SubmissionClaimRow fixedFeeClaim =
        SubmissionClaimRow.builder()
            .id(FIXED_FEE_CLAIM_ID)
            .lineNumber(2)
            .ufn("011015/126")
            .ucn("UCN126")
            .clientForename("FirstTwo")
            .clientSurname("Unescaped")
            .concludedOrClaimedDate(LocalDate.of(2025, 5, 12))
            .feeCode("FPB010")
            .status("AMENDED")
            .escapeCase(false)
            .calculatedValue(new BigDecimal("00.00"))
            .build();
    SubmissionClaimRow assessedClaim =
        SubmissionClaimRow.builder()
            .id(ASSESSED_CLAIM_ID)
            .lineNumber(2)
            .ufn("011015/126")
            .ucn("UCN126")
            .clientForename("Forename")
            .clientSurname("Assessed")
            .concludedOrClaimedDate(LocalDate.of(2025, 5, 12))
            .feeCode("FPB010")
            .status("ASSESSED")
            .escapeCase(false)
            .calculatedValue(new BigDecimal("00.00"))
            .build();
    SubmissionClaimRow voidedClaim =
        SubmissionClaimRow.builder()
            .id(VOIDED_CLAIM_ID)
            .lineNumber(2)
            .ufn("011015/126")
            .ucn("UCN126")
            .clientForename("Forename")
            .clientSurname("Voided")
            .concludedOrClaimedDate(LocalDate.of(2025, 5, 12))
            .feeCode("FPB010")
            .status("VOIDED")
            .escapeCase(false)
            .calculatedValue(new BigDecimal("00.00"))
            .build();
    Page claimsPagination =
        Page.builder().number(0).totalPages(1).size(PAGE_SIZE).totalElements(4).build();
    when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt(), anyString()))
        .thenReturn(
            new SubmissionClaimsDetails(
                List.of(escapedClaim, fixedFeeClaim, assessedClaim, voidedClaim),
                claimsPagination,
                new BigDecimal("2000.00")));

    Page messagesPagination =
        Page.builder().number(0).totalPages(1).size(PAGE_SIZE).totalElements(1).build();
    when(submissionMessagesBuilder.build(any(), any(), any(), any(), anyInt(), anyInt(), any()))
        .thenReturn(
            new MessagesSummary(
                List.of(
                    MessageRow.builder()
                        .claimReference(Optional.of(ESCAPED_CLAIM_ID))
                        .clientSurname("Escaped")
                        .clientForename("First")
                        .ufn("011015/125")
                        .ucn("UCN125")
                        .message("This claim is escaped")
                        .build()),
                1,
                1,
                messagesPagination,
                MessagesSource.CLAIM));

    when(submissionMatterStartsDetailsBuilder.build(any()))
        .thenReturn(List.of(new SubmissionMatterStartsRow("Category AAP", 3)));
  }

  @Test
  void renderLegalHelpSubmissionDetails() {
    Document doc = renderDocument();

    // Summary
    var summaryList = getFirstSummaryList(doc);
    assertThat(summaryList).hasSize(6);
    assertRowContainsValues(summaryList.get(2), "Area of law", "Legal help");
    assertRowContainsValues(summaryList.get(5), "Calculated bulk claim value", "£2,000.00");

    // Warning count banner
    assertPageHasContent(doc, "1 claim has a warning message");

    var navItemTexts = doc.select(".moj-sub-navigation__item").eachText();
    assertThat(navItemTexts).contains("Claims (4)", "Messages (1)", "Matter starts (3)");

    var rows = doc.select(".govuk-table__body tr");
    assertThat(rows).hasSize(4);

    var escapedRowCells = rows.get(0).select("td");
    assertThat(escapedRowCells.get(0).text()).isEqualTo("First Escaped");
    assertThat(escapedRowCells.get(1).text()).isEqualTo("011015/125");
    assertThat(escapedRowCells.get(2).text()).isEqualTo("FPB020");
    assertThat(escapedRowCells.get(3).text()).isEqualTo("£2,000.00");
    assertThat(escapedRowCells.get(4).text()).isEqualTo("UCN125");
    assertThat(escapedRowCells.get(5).text()).isEqualTo("Yes");
    assertThat(selectFirst(rows.get(0), ".govuk-tag--green").text()).isEqualTo("Accepted");
    assertThat(selectFirst(rows.get(0), "a").attr("href"))
        .contains("/submissions/%s/claims/%s".formatted(submissionId, ESCAPED_CLAIM_ID));

    var fixedFeeRowCells = rows.get(1).select("td");
    assertThat(fixedFeeRowCells.get(0).text()).isEqualTo("FirstTwo Unescaped");
    assertThat(fixedFeeRowCells.get(1).text()).isEqualTo("011015/126");
    assertThat(fixedFeeRowCells.get(2).text()).isEqualTo("FPB010");
    assertThat(fixedFeeRowCells.get(3).text()).isEqualTo("£0.00");
    assertThat(fixedFeeRowCells.get(4).text()).isEqualTo("UCN126");
    assertThat(fixedFeeRowCells.get(5).text()).isEqualTo("No");
    assertThat(selectFirst(rows.get(1), ".govuk-tag--yellow").text()).isEqualTo("Amended");
    assertThat(selectFirst(rows.get(1), "a").attr("href"))
        .contains("/submissions/%s/claims/%s".formatted(submissionId, FIXED_FEE_CLAIM_ID));

    var assessedRowCells = rows.get(2).select("td");
    assertThat(assessedRowCells.get(0).text()).isEqualTo("Forename Assessed");
    assertThat(assessedRowCells.get(1).text()).isEqualTo("011015/126");
    assertThat(assessedRowCells.get(2).text()).isEqualTo("FPB010");
    assertThat(assessedRowCells.get(3).text()).isEqualTo("£0.00");
    assertThat(assessedRowCells.get(4).text()).isEqualTo("UCN126");
    assertThat(assessedRowCells.get(5).text()).isEqualTo("No");
    assertThat(selectFirst(rows.get(2), ".govuk-tag--blue").text()).isEqualTo("Assessed");
    assertThat(selectFirst(rows.get(2), "a").attr("href"))
        .contains("/submissions/%s/claims/%s".formatted(submissionId, ASSESSED_CLAIM_ID));

    var voidRowCells = rows.get(3).select("td");
    assertThat(voidRowCells.get(0).text()).isEqualTo("Forename Voided");
    assertThat(voidRowCells.get(1).text()).isEqualTo("011015/126");
    assertThat(voidRowCells.get(2).text()).isEqualTo("FPB010");
    assertThat(voidRowCells.get(3).text()).isEqualTo("£0.00");
    assertThat(voidRowCells.get(4).text()).isEqualTo("UCN126");
    assertThat(voidRowCells.get(5).text()).isEqualTo("No");
    assertThat(selectFirst(rows.get(3), ".govuk-tag--red").text()).isEqualTo("Voided");
    assertThat(selectFirst(rows.get(3), "a").attr("href"))
        .contains("/submissions/%s/claims/%s".formatted(submissionId, VOIDED_CLAIM_ID));
  }

  @Test
  void checkMessagesTabContainsEscapeMessage() {
    Document doc = renderDocumentWithParams(Map.of("navTab", "CLAIM_MESSAGES"));

    var rows = doc.select(".govuk-table__body tr");
    assertThat(rows).hasSize(1);

    var messageCells = rows.get(0).select("td");
    assertThat(messageCells.get(1).text()).isEqualTo("Escaped");
    assertThat(messageCells.get(2).text()).isEqualTo("First");
    assertThat(messageCells.get(3).text()).isEqualTo("011015/125");
    assertThat(messageCells.get(4).text()).isEqualTo("UCN125");
    assertThat(messageCells.get(5).text()).isEqualTo("This claim is escaped");
  }

  @Test
  void checkMatterStartsTabContainsMatterStarts() {
    Document doc = renderDocumentWithParams(Map.of("navTab", "MATTER_STARTS"));

    var matterStartsContainer = selectFirst(doc, "#matter-starts").parent();
    var summaryList = matterStartsContainer.select(".govuk-summary-list__row");
    assertThat(summaryList).hasSize(1);
    assertThat(selectFirst(summaryList.get(0), ".govuk-summary-list__key").text())
        .isEqualTo("Category AAP");
    assertThat(selectFirst(summaryList.get(0), ".govuk-summary-list__value").text()).isEqualTo("3");
  }

  @Test
  void viewHasNoDateWorkConcludedColumn() {
    mockClaims(SubmissionClaimRow.builder().build());

    var headers = getTableHeaders(renderDocument());

    assertThat(headers.eachText()).doesNotContain("Date work concluded");
  }

  @Test
  void viewHasSortableClaimHeaders() {
    mockClaims(SubmissionClaimRow.builder().build());
    var doc = renderDocument();
    Elements headers = getTableHeaders(doc);
    assertTableHeaderIsSortable(
        headers.get(0), "none", "Client name", claimSortLink("client_forename"));
    assertTableHeaderIsSortable(headers.get(1), "none", "UFN", claimSortLink("unique_file_number"));
    assertTableHeaderIsSortable(headers.get(2), "none", "Fee code", claimSortLink("fee_code"));
    assertTableHeaderIsSortable(
        headers.get(3), "none", "Initial calculated value", claimSortLink("total_amount"));
    assertTableHeaderIsSortable(
        headers.get(4), "none", "UCN", claimSortLink("unique_client_number"));
    assertTableHeaderIsSortable(
        headers.get(5), "none", "Escape case", claimSortLink("escape_case_flag"));
    assertTableHeaderIsSortable(
        headers.get(6), "none", "Status", claimSortLink("derived_claim_status"));
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClientNameClaimFieldIsSortable(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        0,
        "client_forename",
        "Client name",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailUniqueFileNumberClaimFieldIsSortable(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        1,
        "unique_file_number",
        "UFN",
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
        2,
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
        3,
        "total_amount",
        "Initial calculated value",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailUniqueClientNumberClaimFieldIsSortable(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        4,
        "unique_client_number",
        "UCN",
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
        5,
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
        6,
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
