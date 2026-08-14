package uk.gov.justice.laa.bulkclaim.view;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.CRIME_LOWER;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.LEGAL_HELP;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.MEDIATION;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.constants.ViewSubmissionNavigationTab;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimsDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessageRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSource;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

class ClaimDetailsViewTest extends SubmissionDetailsViewTestBase {

  @Test
  void viewHasSortableClaimHeaders_crime() {
    mockClaims(CRIME_LOWER);
    var doc = renderDocument();
    Elements headers = getTableHeaders(doc);
    assertTableHeaderIsNotSortable(headers.get(0), "Claim");
    assertTableHeaderIsSortable(
        headers.get(1), "none", "Client surname", claimSortLink("client_surname"));
    assertTableHeaderIsSortable(
        headers.get(2), "none", "Client initial", claimSortLink("client_forename"));
    assertTableHeaderIsSortable(headers.get(3), "none", "UFN", claimSortLink("unique_file_number"));
    assertTableHeaderIsSortable(headers.get(4), "none", "Fee code", claimSortLink("fee_code"));
    assertTableHeaderIsSortable(
        headers.get(5), "none", "Date work concluded", claimSortLink("case_concluded_date"));
    assertTableHeaderIsSortable(
        headers.get(6), "none", "Calculated value", claimSortLink("total_amount"));
    assertTableHeaderIsSortable(
        headers.get(7), "none", "Escape case", claimSortLink("escape_case_flag"));
    assertTableHeaderIsSortable(
        headers.get(8), "none", "Messages", claimSortLink("total_warnings"));
  }

  @Test
  void viewHasSortableClaimHeaders_civil() {
    mockClaims(LEGAL_HELP);
    var doc = renderDocument();
    Elements headers = getTableHeaders(doc);
    assertTableHeaderIsNotSortable(headers.get(0), "Claim");
    assertTableHeaderIsSortable(
        headers.get(1), "none", "Client surname", claimSortLink("client_surname"));
    assertTableHeaderIsSortable(
        headers.get(2), "none", "Client forename", claimSortLink("client_forename"));
    assertTableHeaderIsSortable(headers.get(3), "none", "UFN", claimSortLink("unique_file_number"));
    assertTableHeaderIsSortable(
        headers.get(4), "none", "UCN", claimSortLink("unique_client_number"));
    assertTableHeaderIsSortable(headers.get(5), "none", "Fee code", claimSortLink("fee_code"));
    assertTableHeaderIsSortable(
        headers.get(6), "none", "Calculated value", claimSortLink("total_amount"));
    assertTableHeaderIsSortable(
        headers.get(7), "none", "Escape case", claimSortLink("escape_case_flag"));
    assertTableHeaderIsSortable(
        headers.get(8), "none", "Messages", claimSortLink("total_warnings"));
  }

  @Test
  void viewHasSortableClaimHeaders_mediation() {
    mockClaims(MEDIATION);
    var doc = renderDocument();
    Elements headers = getTableHeaders(doc);
    assertTableHeaderIsNotSortable(headers.get(0), "Claim");
    assertTableHeaderIsSortable(
        headers.get(1), "none", "Client 1 surname", claimSortLink("client_surname"));
    assertTableHeaderIsSortable(
        headers.get(2), "none", "Client 1 forename", claimSortLink("client_forename"));
    assertTableHeaderIsSortable(
        headers.get(3), "none", "Client 1 UCN", claimSortLink("unique_client_number"));
    assertTableHeaderIsSortable(
        headers.get(4), "none", "Client 2 surname", claimSortLink("client_2_surname"));
    assertTableHeaderIsSortable(
        headers.get(5), "none", "Client 2 forename", claimSortLink("client_2_forename"));
    assertTableHeaderIsSortable(
        headers.get(6), "none", "Client 2 UCN", claimSortLink("client_2_ucn"));
    assertTableHeaderIsSortable(headers.get(7), "none", "Fee code", claimSortLink("fee_code"));
    assertTableHeaderIsSortable(
        headers.get(8), "none", "Calculated value", claimSortLink("total_amount"));
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClientSurnameClaimFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        CRIME_LOWER,
        1,
        "client_surname",
        "Client surname",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClientForenameClaimFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        CRIME_LOWER,
        2,
        "client_forename",
        "Client initial",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailUniqueFileNumberClaimFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        CRIME_LOWER,
        3,
        "unique_file_number",
        "UFN",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailFeeCodeClaimFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        CRIME_LOWER,
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
  void viewSubmissionDetailCaseConcludedDateClaimFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        CRIME_LOWER,
        5,
        "case_concluded_date",
        "Date work concluded",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailTotalAmountClaimFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        CRIME_LOWER,
        6,
        "total_amount",
        "Calculated value",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailEscapeCaseFlagClaimFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        CRIME_LOWER,
        7,
        "escape_case_flag",
        "Escape case",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailTotalWarningsClaimFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        CRIME_LOWER,
        8,
        "total_warnings",
        "Messages",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClientSurnameClaimFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        LEGAL_HELP,
        1,
        "client_surname",
        "Client surname",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClientForenameClaimFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        LEGAL_HELP,
        2,
        "client_forename",
        "Client forename",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailUniqueFileNumberClaimFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        LEGAL_HELP,
        3,
        "unique_file_number",
        "UFN",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailUniqueClientNumberClaimFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        LEGAL_HELP,
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
  void viewSubmissionDetailFeeCodeClaimFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        LEGAL_HELP,
        5,
        "fee_code",
        "Fee code",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailTotalAmountClaimFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        LEGAL_HELP,
        6,
        "total_amount",
        "Calculated value",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailEscapeCaseFlagClaimFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        LEGAL_HELP,
        7,
        "escape_case_flag",
        "Escape case",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailTotalWarningsClaimFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        LEGAL_HELP,
        8,
        "total_warnings",
        "Messages",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClientSurnameClaimFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        MEDIATION,
        1,
        "client_surname",
        "Client 1 surname",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClientForenameClaimFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        MEDIATION,
        2,
        "client_forename",
        "Client 1 forename",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailUniqueClientNumberClaimFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        MEDIATION,
        3,
        "unique_client_number",
        "Client 1 UCN",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClient2SurnameClaimFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        MEDIATION,
        4,
        "client_2_surname",
        "Client 2 surname",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClient2ForenameClaimFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        MEDIATION,
        5,
        "client_2_forename",
        "Client 2 forename",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClient2UcnClaimFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        MEDIATION,
        6,
        "client_2_ucn",
        "Client 2 UCN",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailFeeCodeClaimFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        MEDIATION,
        7,
        "fee_code",
        "Fee code",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailTotalAmountClaimFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        MEDIATION,
        8,
        "total_amount",
        "Calculated value",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("paginationRendersArgs")
  void viewSubmissionDetailRendersClaimPaginationAcrossPages(
      int currentPage,
      int totalPages,
      List<Integer> expectedVisiblePages,
      boolean expectedPreviousLink,
      boolean expectedNextLink,
      int expectedEllipsesCount) {
    mockAcceptedSubmission(
        CRIME_LOWER, pagination(currentPage, totalPages), pagination(0, 1), "client_surname,desc");
    var doc =
        renderDocumentWithParams(
            Map.of("page", String.valueOf(currentPage), "sort", "client_surname,desc"));
    assertPaginationRenders(
        doc,
        "page",
        currentPage,
        expectedVisiblePages,
        expectedPreviousLink,
        expectedNextLink,
        expectedEllipsesCount);
  }

  private void assertClaimFieldIsSortable(
      AreaOfLaw areaOfLaw,
      int headerIndex,
      String fieldKey,
      String fieldName,
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    mockAcceptedSubmission(areaOfLaw, pagination(0, 1), pagination(0, 1), "client_surname,desc");
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
        "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=%s,%s"
            .formatted(submissionId, fieldKey, expectedLinkDirection));
  }

  private String claimSortLink(String field) {
    return "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=%s,asc"
        .formatted(submissionId, field);
  }

  private void mockClaims(AreaOfLaw areaOfLaw) {
    Page pagination = Page.builder().totalPages(1).totalElements(1).number(0).size(10).build();
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder()
            .submissionId(submissionId)
            .status(SubmissionStatus.VALIDATION_SUCCEEDED)
            .areaOfLaw(areaOfLaw)
            .build();
    when(dataClaimsRestClient.getSubmission(submissionId))
        .thenReturn(Mono.just(submissionResponse));
    when(submissionSummaryBuilder.build(any()))
        .thenReturn(
            new SubmissionSummary(
                submissionId,
                "Submitted",
                LocalDate.of(2025, 5, 1),
                "AQ2B3C",
                BigDecimal.ONE,
                areaOfLaw.getValue(),
                OffsetDateTime.of(2025, 1, 1, 10, 10, 10, 0, ZoneOffset.UTC)));
    when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt(), anyString()))
        .thenReturn(
            new SubmissionClaimsDetails(
                List.of(SubmissionClaimRow.builder().build()), pagination, BigDecimal.ONE));
    when(submissionMessagesBuilder.build(any(), any(), any(), anyInt(), anyInt(), any()))
        .thenReturn(new MessagesSummary(List.of(), 0, 0, pagination, MessagesSource.CLAIM));
  }

  private void mockAcceptedSubmission(
      AreaOfLaw areaOfLaw, Page claimPagination, Page messagesPagination, String defaultSort) {
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder()
            .submissionId(submissionId)
            .status(SubmissionStatus.VALIDATION_SUCCEEDED)
            .areaOfLaw(areaOfLaw)
            .build();
    when(dataClaimsRestClient.getSubmission(submissionId))
        .thenReturn(Mono.just(submissionResponse));
    when(submissionSummaryBuilder.build(any()))
        .thenReturn(
            new SubmissionSummary(
                submissionId,
                "Submitted",
                LocalDate.of(2025, 5, 1),
                "AQ2B3C",
                BigDecimal.ONE,
                areaOfLaw.getValue(),
                OffsetDateTime.of(2025, 1, 1, 10, 10, 10, 0, ZoneOffset.UTC)));
    when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt(), anyString()))
        .thenReturn(
            new SubmissionClaimsDetails(
                List.of(SubmissionClaimRow.builder().build()), claimPagination, BigDecimal.ONE));
    when(submissionMessagesBuilder.build(any(), any(), any(), anyInt(), anyInt(), any()))
        .thenReturn(
            new MessagesSummary(
                List.of(
                    MessageRow.builder().claimReference(Optional.of(UUID.randomUUID())).build()),
                0,
                0,
                messagesPagination,
                MessagesSource.CLAIM));
    when(paginationLinksBuilder.build(any(), eq(claimPagination), eq("page"), any(Object[].class)))
        .thenReturn(
            buildSubmissionDetailPaginationLinks(
                submissionId,
                claimPagination.getNumber(),
                claimPagination.getTotalPages(),
                "page",
                ViewSubmissionNavigationTab.CLAIM_DETAILS,
                defaultSort));
    when(paginationLinksBuilder.build(
            any(), eq(messagesPagination), eq("messagesPage"), any(Object[].class)))
        .thenReturn(
            buildSubmissionDetailPaginationLinks(
                submissionId,
                messagesPagination.getNumber(),
                messagesPagination.getTotalPages(),
                "messagesPage",
                ViewSubmissionNavigationTab.CLAIM_MESSAGES,
                defaultSort));
  }
}
