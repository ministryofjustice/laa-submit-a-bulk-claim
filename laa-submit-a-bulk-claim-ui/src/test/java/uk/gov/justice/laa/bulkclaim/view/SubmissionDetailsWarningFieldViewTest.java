package uk.gov.justice.laa.bulkclaim.view;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper.OIDC_USER;
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

class SubmissionDetailsWarningFieldViewTest extends SubmissionDetailsViewTestBase {

  @Test
  void viewSubmissionDetailHasSortableWarningHeaders_crime() {
    mockWarningMessages(CRIME_LOWER);
    var doc = renderDocumentWithParams(Map.of("navTab", "CLAIM_MESSAGES"));
    Elements headers = getTableHeaders(doc);
    assertTableHeaderIsNotSortable(headers.get(0), "Claim");
    assertTableHeaderIsSortable(
        headers.get(1), "none", "Client surname", warningSortLink("client_surname"));
    assertTableHeaderIsSortable(
        headers.get(2), "none", "Client initial", warningSortLink("client_forename"));
    assertTableHeaderIsSortable(
        headers.get(3), "none", "UFN", warningSortLink("unique_file_number"));
    assertTableHeaderIsSortable(
        headers.get(4), "none", "Messages", warningSortLink("display_message"));
  }

  @Test
  void viewSubmissionDetailHasSortableWarningHeaders_civil() {
    mockWarningMessages(LEGAL_HELP);
    var doc = renderDocumentWithParams(Map.of("navTab", "CLAIM_MESSAGES"));
    Elements headers = getTableHeaders(doc);
    assertTableHeaderIsNotSortable(headers.get(0), "Claim");
    assertTableHeaderIsSortable(
        headers.get(1), "none", "Client surname", warningSortLink("client_surname"));
    assertTableHeaderIsSortable(
        headers.get(2), "none", "Client initial", warningSortLink("client_forename"));
    assertTableHeaderIsSortable(
        headers.get(3), "none", "UFN", warningSortLink("unique_file_number"));
    assertTableHeaderIsSortable(
        headers.get(4), "none", "UCN", warningSortLink("unique_client_number"));
    assertTableHeaderIsSortable(
        headers.get(5), "none", "Messages", warningSortLink("display_message"));
  }

  @Test
  void viewSubmissionDetailHasSortableWarningHeaders_mediation() {
    mockWarningMessages(MEDIATION);
    var doc = renderDocumentWithParams(Map.of("navTab", "CLAIM_MESSAGES"));
    Elements headers = getTableHeaders(doc);
    assertTableHeaderIsNotSortable(headers.get(0), "Claim");
    assertTableHeaderIsSortable(
        headers.get(1), "none", "Client 1 surname", warningSortLink("client_surname"));
    assertTableHeaderIsSortable(
        headers.get(2), "none", "Client 1 forename", warningSortLink("client_forename"));
    assertTableHeaderIsSortable(
        headers.get(3), "none", "Client 1 UCN", warningSortLink("unique_client_number"));
    assertTableHeaderIsSortable(
        headers.get(4), "none", "Client 2 surname", warningSortLink("client_2_surname"));
    assertTableHeaderIsSortable(
        headers.get(5), "none", "Client 2 forename", warningSortLink("client_2_forename"));
    assertTableHeaderIsSortable(
        headers.get(6), "none", "Client 2 UCN", warningSortLink("client_2_ucn"));
    assertTableHeaderIsSortable(
        headers.get(7), "none", "Messages", warningSortLink("display_message"));
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClientSurnameWarningFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertWarningFieldIsSortable(
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
  void viewSubmissionDetailClientForenameWarningFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertWarningFieldIsSortable(
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
  void viewSubmissionDetailUniqueFileNumberWarningFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertWarningFieldIsSortable(
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
  void viewSubmissionDetailDisplayMessageWarningFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertWarningFieldIsSortable(
        CRIME_LOWER,
        4,
        "display_message",
        "Messages",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClientSurnameWarningFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertWarningFieldIsSortable(
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
  void viewSubmissionDetailClientForenameWarningFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertWarningFieldIsSortable(
        LEGAL_HELP,
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
  void viewSubmissionDetailUniqueFileNumberWarningFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertWarningFieldIsSortable(
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
  void viewSubmissionDetailUniqueClientNumberWarningFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertWarningFieldIsSortable(
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
  void viewSubmissionDetailDisplayMessageWarningFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertWarningFieldIsSortable(
        LEGAL_HELP,
        5,
        "display_message",
        "Messages",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClientSurnameWarningFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertWarningFieldIsSortable(
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
  void viewSubmissionDetailClientForenameWarningFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertWarningFieldIsSortable(
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
  void viewSubmissionDetailUniqueClientNumberWarningFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertWarningFieldIsSortable(
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
  void viewSubmissionDetailClient2SurnameWarningFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertWarningFieldIsSortable(
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
  void viewSubmissionDetailClient2ForenameWarningFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertWarningFieldIsSortable(
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
  void viewSubmissionDetailClient2UcnWarningFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertWarningFieldIsSortable(
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
  void viewSubmissionDetailDisplayMessageWarningFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertWarningFieldIsSortable(
        MEDIATION,
        7,
        "display_message",
        "Messages",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("paginationRendersArgs")
  void viewSubmissionDetailRendersMessagesPaginationAcrossPages(
      int currentPage,
      int totalPages,
      List<Integer> expectedVisiblePages,
      boolean expectedPreviousLink,
      boolean expectedNextLink,
      int expectedEllipsesCount) {
    mockAcceptedSubmission(
        CRIME_LOWER, pagination(0, 1), pagination(currentPage, totalPages), "display_message,desc");
    var doc =
        renderDocumentWithParams(
            Map.of(
                "navTab",
                "CLAIM_MESSAGES",
                "messagesPage",
                String.valueOf(currentPage),
                "messagesSort",
                "display_message,desc"));
    assertPaginationRenders(
        doc,
        "messagesPage",
        currentPage,
        expectedVisiblePages,
        expectedPreviousLink,
        expectedNextLink,
        expectedEllipsesCount);
  }

  private void assertWarningFieldIsSortable(
      AreaOfLaw areaOfLaw,
      int headerIndex,
      String fieldKey,
      String fieldName,
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    mockAcceptedSubmission(areaOfLaw, pagination(0, 1), pagination(0, 1), "display_message,desc");
    var doc =
        renderDocumentWithParams(
            Map.of(
                "navTab",
                "CLAIM_MESSAGES",
                "messagesPage",
                String.valueOf(currentPage),
                "messagesSort",
                "%s,%s".formatted(fieldKey, currentDirection)));
    Elements headers = getTableHeaders(doc);
    assertTableHeaderIsSortable(
        headers.get(headerIndex),
        expectedAriaDirection,
        fieldName,
        "/submissions/%s?navTab=%s&messagesPage=0&messagesSort=%s,%s"
            .formatted(
                submissionId,
                ViewSubmissionNavigationTab.CLAIM_MESSAGES,
                fieldKey,
                expectedLinkDirection));
  }

  private String warningSortLink(String field) {
    return "/submissions/%s?navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=%s,asc"
        .formatted(submissionId, field);
  }

  private void mockWarningMessages(AreaOfLaw areaOfLaw) {
    Page pagination = Page.builder().totalPages(1).totalElements(1).number(0).size(10).build();
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
                List.of(SubmissionClaimRow.builder().build()), pagination, BigDecimal.ONE));
    when(submissionMessagesBuilder.build(any(), any(), any(), any(), anyInt(), anyInt(), any()))
        .thenReturn(
            new MessagesSummary(
                List.of(
                    MessageRow.builder().claimReference(Optional.of(UUID.randomUUID())).build()),
                0,
                0,
                pagination,
                MessagesSource.CLAIM));
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
