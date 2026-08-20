package uk.gov.justice.laa.bulkclaim.view;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.justice.laa.bulkclaim.constants.ViewSubmissionNavigationTab;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessageRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSource;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

class SubmissionDetailsErrorFieldViewTest extends SubmissionDetailsViewTestBase {

  @Test
  void viewSubmissionDetailHasSortableClaimErrorHeaders_crime() {
    mockErrorMessages(CRIME_LOWER, MessagesSource.CLAIM);
    var doc = renderDocumentWithParams(Map.of("navTab", "CLAIM_MESSAGES"));
    Elements headers = getTableHeaders(doc);
    assertTableHeaderIsSortable(
        headers.get(0), "none", "Client surname", errorSortLink("client_surname"));
    assertTableHeaderIsSortable(
        headers.get(1), "none", "Client initial", errorSortLink("client_forename"));
    assertTableHeaderIsSortable(headers.get(2), "none", "UFN", errorSortLink("unique_file_number"));
    assertTableHeaderIsSortable(
        headers.get(3), "none", "Messages", errorSortLink("display_message"));
  }

  @Test
  void viewSubmissionDetailHasSortableClaimErrorHeaders_civil() {
    mockErrorMessages(LEGAL_HELP, MessagesSource.CLAIM);
    var doc = renderDocumentWithParams(Map.of("navTab", "CLAIM_MESSAGES"));
    Elements headers = getTableHeaders(doc);
    assertTableHeaderIsSortable(
        headers.get(0), "none", "Client surname", errorSortLink("client_surname"));
    assertTableHeaderIsSortable(
        headers.get(1), "none", "Client initial", errorSortLink("client_forename"));
    assertTableHeaderIsSortable(headers.get(2), "none", "UFN", errorSortLink("unique_file_number"));
    assertTableHeaderIsSortable(
        headers.get(3), "none", "UCN", errorSortLink("unique_client_number"));
    assertTableHeaderIsSortable(
        headers.get(4), "none", "Messages", errorSortLink("display_message"));
  }

  @Test
  void viewSubmissionDetailHasSortableClaimErrorHeaders_mediation() {
    mockErrorMessages(MEDIATION, MessagesSource.CLAIM);
    var doc = renderDocumentWithParams(Map.of("navTab", "CLAIM_MESSAGES"));
    Elements headers = getTableHeaders(doc);
    assertTableHeaderIsSortable(
        headers.get(0), "none", "Client 1 surname", errorSortLink("client_surname"));
    assertTableHeaderIsSortable(
        headers.get(1), "none", "Client 1 forename", errorSortLink("client_forename"));
    assertTableHeaderIsSortable(
        headers.get(2), "none", "Client 1 UCN", errorSortLink("unique_client_number"));
    assertTableHeaderIsSortable(
        headers.get(3), "none", "Client 2 surname", errorSortLink("client_2_surname"));
    assertTableHeaderIsSortable(
        headers.get(4), "none", "Client 2 forename", errorSortLink("client_2_forename"));
    assertTableHeaderIsSortable(
        headers.get(5), "none", "Client 2 UCN", errorSortLink("client_2_ucn"));
    assertTableHeaderIsSortable(
        headers.get(6), "none", "Messages", errorSortLink("display_message"));
  }

  @Test
  void viewSubmissionDetailHasSortableSubmissionErrorHeaders() {
    mockErrorMessages(CRIME_LOWER, MessagesSource.SUBMISSION);
    var doc = renderDocumentWithParams(Map.of("navTab", "CLAIM_MESSAGES"));
    Elements headers = getTableHeaders(doc);
    assertTableHeaderIsSortable(
        headers.get(0), "none", "Messages", errorSortLink("display_message"));
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClientSurnameClaimErrorFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimErrorFieldIsSortable(
        CRIME_LOWER,
        0,
        "client_surname",
        "Client surname",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClientInitialClaimErrorFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimErrorFieldIsSortable(
        CRIME_LOWER,
        1,
        "client_forename",
        "Client initial",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailUfnClaimErrorFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimErrorFieldIsSortable(
        CRIME_LOWER,
        2,
        "unique_file_number",
        "UFN",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailMessagesClaimErrorFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimErrorFieldIsSortable(
        CRIME_LOWER,
        3,
        "display_message",
        "Messages",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClientSurnameClaimErrorFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimErrorFieldIsSortable(
        LEGAL_HELP,
        0,
        "client_surname",
        "Client surname",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClientInitialClaimErrorFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimErrorFieldIsSortable(
        LEGAL_HELP,
        1,
        "client_forename",
        "Client initial",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailUfnClaimErrorFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimErrorFieldIsSortable(
        LEGAL_HELP,
        2,
        "unique_file_number",
        "UFN",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailUcnClaimErrorFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimErrorFieldIsSortable(
        LEGAL_HELP,
        3,
        "unique_client_number",
        "UCN",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailMessagesClaimErrorFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimErrorFieldIsSortable(
        LEGAL_HELP,
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
  void viewSubmissionDetailClient1SurnameClaimErrorFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimErrorFieldIsSortable(
        MEDIATION,
        0,
        "client_surname",
        "Client 1 surname",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClient1ForenameClaimErrorFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimErrorFieldIsSortable(
        MEDIATION,
        1,
        "client_forename",
        "Client 1 forename",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClient1UcnClaimErrorFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimErrorFieldIsSortable(
        MEDIATION,
        2,
        "unique_client_number",
        "Client 1 UCN",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClient2SurnameClaimErrorFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimErrorFieldIsSortable(
        MEDIATION,
        3,
        "client_2_surname",
        "Client 2 surname",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClient2ForenameClaimErrorFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimErrorFieldIsSortable(
        MEDIATION,
        4,
        "client_2_forename",
        "Client 2 forename",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailClient2UcnClaimErrorFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimErrorFieldIsSortable(
        MEDIATION,
        5,
        "client_2_ucn",
        "Client 2 UCN",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailMessagesClaimErrorFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertClaimErrorFieldIsSortable(
        MEDIATION,
        6,
        "display_message",
        "Messages",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailMessagesSubmissionErrorFieldIsSortable_crime(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertSubmissionErrorFieldIsSortable(
        CRIME_LOWER,
        0,
        "display_message",
        "Messages",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailMessagesSubmissionErrorFieldIsSortable_legalHelp(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertSubmissionErrorFieldIsSortable(
        LEGAL_HELP,
        0,
        "display_message",
        "Messages",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("detailFieldIsSortableArgs")
  void viewSubmissionDetailMessagesSubmissionErrorFieldIsSortable_mediation(
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    assertSubmissionErrorFieldIsSortable(
        MEDIATION,
        0,
        "display_message",
        "Messages",
        currentDirection,
        currentPage,
        expectedAriaDirection,
        expectedLinkDirection);
  }

  @ParameterizedTest
  @MethodSource("paginationRendersArgs")
  void viewSubmissionDetailRendersClaimErrorPaginationAcrossPages(
      int currentPage,
      int totalPages,
      List<Integer> expectedVisiblePages,
      boolean expectedPreviousLink,
      boolean expectedNextLink,
      int expectedEllipsesCount) {
    mockErrorMessages(
        CRIME_LOWER,
        MessagesSource.CLAIM,
        pagination(currentPage, totalPages),
        "client_surname,desc");
    var doc =
        renderDocumentWithParams(
            Map.of(
                "navTab",
                "CLAIM_MESSAGES",
                "messagesPage",
                String.valueOf(currentPage),
                "messagesSort",
                "client_surname,desc"));
    assertPaginationRenders(
        doc,
        "messagesPage",
        currentPage,
        expectedVisiblePages,
        expectedPreviousLink,
        expectedNextLink,
        expectedEllipsesCount);
  }

  @ParameterizedTest
  @MethodSource("paginationRendersArgs")
  void viewSubmissionDetailRendersSubmissionErrorPaginationAcrossPages(
      int currentPage,
      int totalPages,
      List<Integer> expectedVisiblePages,
      boolean expectedPreviousLink,
      boolean expectedNextLink,
      int expectedEllipsesCount) {
    mockErrorMessages(
        CRIME_LOWER,
        MessagesSource.SUBMISSION,
        pagination(currentPage, totalPages),
        "display_message,desc");
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

  private void assertClaimErrorFieldIsSortable(
      AreaOfLaw areaOfLaw,
      int columnIndex,
      String fieldName,
      String columnLabel,
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    mockErrorMessages(
        areaOfLaw,
        MessagesSource.CLAIM,
        pagination(currentPage, 1),
        "%s,%s".formatted(fieldName, currentDirection));
    var doc =
        renderDocumentWithParams(
            Map.of(
                "navTab",
                "CLAIM_MESSAGES",
                "messagesPage",
                String.valueOf(currentPage),
                "messagesSort",
                "%s,%s".formatted(fieldName, currentDirection)));
    var headers = getTableHeaders(doc);
    assertTableHeaderIsSortable(
        headers.get(columnIndex),
        expectedAriaDirection,
        columnLabel,
        errorSortLink(fieldName, expectedLinkDirection));
  }

  private void assertSubmissionErrorFieldIsSortable(
      AreaOfLaw areaOfLaw,
      int columnIndex,
      String fieldName,
      String columnLabel,
      String currentDirection,
      int currentPage,
      String expectedAriaDirection,
      String expectedLinkDirection) {
    mockErrorMessages(
        areaOfLaw,
        MessagesSource.SUBMISSION,
        pagination(currentPage, 1),
        "%s,%s".formatted(fieldName, currentDirection));
    var doc =
        renderDocumentWithParams(
            Map.of(
                "navTab",
                "CLAIM_MESSAGES",
                "messagesPage",
                String.valueOf(currentPage),
                "messagesSort",
                "%s,%s".formatted(fieldName, currentDirection)));
    var headers = getTableHeaders(doc);
    assertTableHeaderIsSortable(
        headers.get(columnIndex),
        expectedAriaDirection,
        columnLabel,
        errorSortLink(fieldName, expectedLinkDirection));
  }

  private String errorSortLink(String field) {
    return "/submissions/%s?navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=%s,asc"
        .formatted(submissionId, field);
  }

  private String errorSortLink(String field, String direction) {
    return "/submissions/%s?navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=%s,%s"
        .formatted(submissionId, field, direction);
  }

  private void mockErrorMessages(AreaOfLaw areaOfLaw, MessagesSource messagesSource) {
    Page pagination = Page.builder().totalPages(1).totalElements(1).number(0).size(10).build();
    mockErrorMessages(areaOfLaw, messagesSource, pagination, "display_message,desc");
  }

  private void mockErrorMessages(
      AreaOfLaw areaOfLaw, MessagesSource messagesSource, Page pagination, String sort) {
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder()
            .submissionId(submissionId)
            .status(SubmissionStatus.VALIDATION_FAILED)
            .officeAccountNumber(OFFICE_CODE)
            .areaOfLaw(areaOfLaw)
            .build();
    when(submissionService.getSubmission(submissionId, OIDC_USER)).thenReturn(submissionResponse);
    when(submissionSummaryBuilder.build(any()))
        .thenReturn(
            new SubmissionSummary(
                submissionId,
                "Invalid",
                LocalDate.of(2025, 5, 1),
                "AQ2B3C",
                BigDecimal.ONE,
                areaOfLaw.getValue(),
                OffsetDateTime.of(2025, 1, 1, 10, 10, 10, 0, ZoneOffset.UTC)));
    when(submissionMessagesBuilder.buildErrors(any(), any(), anyInt(), anyInt(), any()))
        .thenReturn(
            new MessagesSummary(
                List.of(MessageRow.builder().build()), 0, 0, pagination, messagesSource));
    when(paginationLinksBuilder.build(
            any(), eq(pagination), eq("messagesPage"), any(Object[].class)))
        .thenReturn(
            buildSubmissionDetailPaginationLinks(
                submissionId,
                pagination.getNumber(),
                pagination.getTotalPages(),
                "messagesPage",
                ViewSubmissionNavigationTab.CLAIM_MESSAGES,
                sort));
  }
}
