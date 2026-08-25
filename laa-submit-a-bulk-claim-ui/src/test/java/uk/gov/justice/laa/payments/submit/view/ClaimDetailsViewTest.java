package uk.gov.justice.laa.payments.submit.view;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.CRIME_LOWER;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.LEGAL_HELP;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.MEDIATION;
import static uk.gov.justice.laa.payments.submit.controller.ControllerTestHelper.OIDC_USER;

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
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.payments.submit.constants.ViewSubmissionNavigationTab;
import uk.gov.justice.laa.payments.submit.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.SubmissionClaimRow;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.SubmissionClaimsDetails;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessageRow;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessagesSource;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessagesSummary;

class ClaimDetailsViewTest extends SubmissionDetailsViewTestBase {

  @Test
  void viewHasSortableClaimHeaders_crime() {
    mockClaims(CRIME_LOWER);
    var doc = renderDocument();
    Elements headers = getTableHeaders(doc);
    assertTableHeaderIsSortable(
        headers.get(0), "none", "Client name", claimSortLink("client_forename"));
    assertTableHeaderIsSortable(headers.get(1), "none", "UFN", claimSortLink("unique_file_number"));
    assertTableHeaderIsSortable(headers.get(2), "none", "Fee code", claimSortLink("fee_code"));
    assertTableHeaderIsSortable(
        headers.get(3), "none", "Initial calculated value", claimSortLink("total_amount"));
    assertTableHeaderIsSortable(
        headers.get(4), "none", "Escape case", claimSortLink("escape_case_flag"));
    assertTableHeaderIsSortable(
        headers.get(5), "none", "Status", claimSortLink("derived_claim_status"));
  }

  @Test
  void viewHasSortableClaimHeaders_legalHelp() {
    mockClaims(LEGAL_HELP);
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

  @Test
  void viewHasSortableClaimHeaders_mediation() {
    mockClaims(MEDIATION);
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
  void viewSubmissionDetailClientNameClaimFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        CRIME_LOWER,
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
  void viewSubmissionDetailUniqueFileNumberClaimFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        CRIME_LOWER,
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
  void viewSubmissionDetailFeeCodeClaimFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        CRIME_LOWER,
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
  void viewSubmissionDetailInitialCalculatedValueClaimFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        CRIME_LOWER,
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
  void viewSubmissionDetailEscapeCaseFlagClaimFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        CRIME_LOWER,
        4,
        "escape_case_flag",
        "Escape case",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailStatusClaimFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        CRIME_LOWER,
        5,
        "derived_claim_status",
        "Status",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClientNameClaimFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        LEGAL_HELP,
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
  void viewSubmissionDetailUniqueFileNumberClaimFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        LEGAL_HELP,
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
  void viewSubmissionDetailFeeCodeClaimFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        LEGAL_HELP,
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
  void viewSubmissionDetailInitialCalculatedValueClaimFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        LEGAL_HELP,
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
  void viewSubmissionDetailEscapeCaseFlagClaimFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        LEGAL_HELP,
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
  void viewSubmissionDetailStatusClaimFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        LEGAL_HELP,
        6,
        "derived_claim_status",
        "Status",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClient1NameClaimFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        MEDIATION,
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
  void viewSubmissionDetailClient1UcnClaimFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        MEDIATION,
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
  void viewSubmissionDetailClient2NameClaimFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        MEDIATION,
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
  void viewSubmissionDetailClient2UcnClaimFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        MEDIATION,
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
  void viewSubmissionDetailFeeCodeClaimFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        MEDIATION,
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
  void viewSubmissionDetailInitialCalculatedValueClaimFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        MEDIATION,
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
  void viewSubmissionDetailEscapeCaseFlagClaimFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        MEDIATION,
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
  void viewSubmissionDetailStatusClaimFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimFieldIsSortable(
        MEDIATION,
        7,
        "derived_claim_status",
        "Status",
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
        CRIME_LOWER, pagination(currentPage, totalPages), pagination(0, 1), "client_forename,desc");
    var doc =
        renderDocumentWithParams(
            Map.of("page", String.valueOf(currentPage), "sort", "client_forename,desc"));
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
    mockAcceptedSubmission(areaOfLaw, pagination(0, 1), pagination(0, 1), "client_forename,desc");
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

  private void mockClaims(AreaOfLaw areaOfLaw) {
    Page pagination = Page.builder().totalPages(1).totalElements(1).number(0).size(10).build();
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder()
            .submissionId(submissionId)
            .officeAccountNumber(OFFICE_CODE)
            .status(SubmissionStatus.VALIDATION_SUCCEEDED)
            .areaOfLaw(areaOfLaw)
            .build();
    when(submissionService.getSubmission(submissionId, OIDC_USER)).thenReturn(submissionResponse);
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
    when(submissionMessagesBuilder.build(any(), any(), any(), any(), anyInt(), anyInt(), any()))
        .thenReturn(new MessagesSummary(List.of(), 0, 0, pagination, MessagesSource.CLAIM));
  }

  private void mockAcceptedSubmission(
      AreaOfLaw areaOfLaw, Page claimPagination, Page messagesPagination, String defaultSort) {
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder()
            .submissionId(submissionId)
            .status(SubmissionStatus.VALIDATION_SUCCEEDED)
            .officeAccountNumber(OFFICE_CODE)
            .areaOfLaw(areaOfLaw)
            .build();
    when(submissionService.getSubmission(submissionId, OIDC_USER)).thenReturn(submissionResponse);
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
    when(submissionMessagesBuilder.build(any(), any(), any(), any(), anyInt(), anyInt(), any()))
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
