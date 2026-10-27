package uk.gov.justice.laa.payments.submit.view.submissiondetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import java.util.stream.Stream;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.payments.submit.constants.ViewSubmissionNavigationTab;
import uk.gov.justice.laa.payments.submit.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessageRow;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessagesSource;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessagesSummary;

class SubmissionDetailsErrorFieldViewTest extends SubmissionDetailsViewTestBase {

  @Test
  void rendersInvalidSubmissionBannerAndSummary() {
    var doc = renderRejectedPage();

    assertThat(doc.select(".moj-alert--error")).isNotEmpty();
    assertPageHasTitle(doc, "Submission details");
    assertPageHasHeading(doc, "Submission summary");
    assertPageHasContent(doc, "2 claims have errors for missing or incorrect information");
    assertPageHasContent(doc, "Resolve the errors and upload the file again.");
    assertThat(selectFirst(doc, ".govuk-tag--red").text()).isEqualTo("Rejected");
  }

  @Test
  void rendersSummaryFieldsWithoutCalculatedValue() {
    var doc = renderRejectedPage();

    var summaryList = getFirstSummaryList(doc);
    assertThat(summaryList).hasSize(5);
    assertRowContainsValues(
        summaryList.get(0), "Submission date and time", "1 Jan 2025 at 10:10AM");
    assertRowContainsValues(summaryList.get(1), "Account", "0P322F");
    assertRowContainsValues(summaryList.get(2), "Area of law", "Crime lower");
    assertRowContainsValues(summaryList.get(3), "Submission period", "MAY-2025");
    assertRowContainsValues(summaryList.get(4), "Submission reference", submissionId.toString());
  }

  @Test
  void rendersPrintActionAndNoDownloadAction() {
    var doc = renderRejectedPage();

    assertThat(
            selectFirst(doc, "[data-module=laa-print-button]").attr("data-print-action-container"))
        .isEqualTo("secondary-action-container");

    assertThat(doc.select("#export-button")).isEmpty();
    assertThat(doc.select(".govuk-button--secondary").eachText()).doesNotContain("Download claims");
  }

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

  @Test
  void rendersSubmissionErrorsWithoutClaimErrorsTable() {
    var doc =
        renderRejectedPage(
            LEGAL_HELP,
            MessagesSource.SUBMISSION,
            2,
            0,
            List.of(
                submissionMessage("Submissions for periods before JAN-2015 are not accepted."),
                submissionMessage("The provider is not contracted for the area of law.")));

    assertThat(selectFirst(doc, ".moj-alert__heading").text())
        .isEqualTo("2 errors were found with your submission");
    assertPageHasContent(doc, "Resolve the errors and upload the file again.");
    assertThat(selectFirst(doc, ".govuk-tag--red").text()).isEqualTo("Rejected");
    assertThat(selectFirst(doc, "#messages-errors-heading").text())
        .isEqualTo("2 submission errors");
    assertThat(selectFirst(doc, "#message-error-0").text())
        .isEqualTo("Submissions for periods before JAN-2015 are not accepted.");
    assertThat(selectFirst(doc, "#message-error-1").text())
        .isEqualTo("The provider is not contracted for the area of law.");
    assertThat(doc.select("#claim-messages-errors-heading")).isEmpty();
    assertThat(doc.select("[id^=claim-message-error-]")).isEmpty();
  }

  @Test
  void rendersSingularSubmissionErrorBannerAndHeading() {
    var doc =
        renderRejectedPage(
            CRIME_LOWER,
            MessagesSource.SUBMISSION,
            1,
            0,
            List.of(submissionMessage("Enter a valid schedule number.")));

    assertThat(selectFirst(doc, ".moj-alert__heading").text())
        .isEqualTo("1 error was found with your submission");
    assertThat(selectFirst(doc, "#messages-errors-heading").text()).isEqualTo("1 submission error");
  }

  @Test
  void rendersClaimErrorsWithoutSubmissionErrorsTable() {
    var doc =
        renderRejectedPage(
            CRIME_LOWER,
            MessagesSource.CLAIM,
            3,
            1,
            List.of(
                claimMessage("Unique File Number is required"),
                claimMessage("Outcome Code is required"),
                claimMessage("Client Surname is required")));

    assertThat(selectFirst(doc, ".moj-alert__heading").text())
        .isEqualTo("1 claim has errors for missing or incorrect information");
    assertThat(selectFirst(doc, "#claim-messages-errors-heading").text())
        .isEqualTo("3 claim errors");
    assertThat(doc.select("[id^=claim-message-error-]")).hasSize(3);
    assertThat(doc.select("#messages-errors-heading")).isEmpty();
    assertThat(doc.select("[id^=message-error-]")).isEmpty();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("claimErrorRowArgs")
  void rendersClaimErrorRowValuesInAreaOfLawColumns(
      AreaOfLaw areaOfLaw, List<String> expectedCells) {
    var row =
        MessageRow.builder()
            .claimReference(Optional.of(UUID.randomUUID()))
            .clientSurname("Doe")
            .clientForename("John")
            .ufn("010125/001")
            .ucn("01011990/J/DOE")
            .client2Surname("Smith")
            .client2Forename("Jane")
            .client2Ucn("02021991/J/SMIT")
            .message("Case Start Date must be between 01/01/1995 and today")
            .build();

    var doc = renderRejectedPage(areaOfLaw, MessagesSource.CLAIM, 1, 1, List.of(row));

    assertThat(doc.select("#claim-message-error-0 .govuk-table__cell").eachText())
        .containsExactlyElementsOf(expectedCells);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("claimErrorMissingValueArgs")
  void rendersDashForMissingClaimErrorRowValues(AreaOfLaw areaOfLaw, List<String> expectedCells) {
    var row =
        MessageRow.builder()
            .claimReference(Optional.of(UUID.randomUUID()))
            .message("Client Surname is required")
            .build();

    var doc = renderRejectedPage(areaOfLaw, MessagesSource.CLAIM, 1, 1, List.of(row));

    assertThat(doc.select("#claim-message-error-0 .govuk-table__cell").eachText())
        .containsExactlyElementsOf(expectedCells);
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
    when(submissionMessagesService.getErrorMessages(
            any(OidcUser.class), any(), anyInt(), anyInt(), any()))
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

  private Document renderRejectedPage(
      AreaOfLaw areaOfLaw,
      MessagesSource messagesSource,
      int totalMessageCount,
      int totalClaimsWithErrors,
      List<MessageRow> messages) {
    Page pagination =
        Page.builder().totalPages(1).totalElements(totalMessageCount).number(0).size(10).build();
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
                "0P322F",
                BigDecimal.ZERO,
                areaOfLaw.getValue(),
                OffsetDateTime.of(2025, 1, 1, 10, 10, 0, 0, ZoneOffset.UTC)));
    when(submissionMessagesService.getErrorMessages(
            any(OidcUser.class), any(), anyInt(), anyInt(), any()))
        .thenReturn(
            new MessagesSummary(
                messages, totalMessageCount, totalClaimsWithErrors, pagination, messagesSource));
    when(submissionMatterStartsDetailsService.getAll(any())).thenReturn(List.of());
    return renderDocument();
  }

  private static MessageRow submissionMessage(String message) {
    return MessageRow.builder().claimReference(Optional.empty()).message(message).build();
  }

  private static MessageRow claimMessage(String message) {
    return MessageRow.builder()
        .claimReference(Optional.of(UUID.randomUUID()))
        .clientSurname("Doe")
        .clientForename("John")
        .ufn("010125/001")
        .message(message)
        .build();
  }

  private Document renderRejectedPage() {
    return renderRejectedPage(
        CRIME_LOWER,
        MessagesSource.CLAIM,
        2,
        2,
        List.of(
            claimMessage(
                "The provider is not contracted for the category of law associated with the fee code"),
            claimMessage("A duplicate claim was found within the same submission")));
  }

  static Stream<Arguments> claimErrorRowArgs() {
    String message = "Case Start Date must be between 01/01/1995 and today";
    return Stream.of(
        Arguments.of(CRIME_LOWER, List.of("Doe", "John", "010125/001", message)),
        Arguments.of(LEGAL_HELP, List.of("Doe", "John", "010125/001", "01011990/J/DOE", message)),
        Arguments.of(
            MEDIATION,
            List.of("Doe", "John", "01011990/J/DOE", "Smith", "Jane", "02021991/J/SMIT", message)));
  }

  static Stream<Arguments> claimErrorMissingValueArgs() {
    String message = "Client Surname is required";
    return Stream.of(
        Arguments.of(CRIME_LOWER, List.of("-", "-", "-", message)),
        Arguments.of(LEGAL_HELP, List.of("-", "-", "-", "-", message)),
        Arguments.of(MEDIATION, List.of("-", "-", "-", "-", "-", "-", message)));
  }
}
