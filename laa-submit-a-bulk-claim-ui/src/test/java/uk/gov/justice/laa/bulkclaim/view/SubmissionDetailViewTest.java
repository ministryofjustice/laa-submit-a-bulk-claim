package uk.gov.justice.laa.bulkclaim.view;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionClaimDetailsBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMatterStartsDetailsBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMessagesBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionSummaryBuilder;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.controller.SubmissionDetailController;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimsDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessageRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSource;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.bulkclaim.util.PaginationLinksBuilder;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@WebMvcTest(SubmissionDetailController.class)
class SubmissionDetailViewTest extends ViewTestBase {

  @MockitoBean SubmissionSummaryBuilder submissionSummaryBuilder;
  @MockitoBean SubmissionClaimDetailsBuilder submissionClaimDetailsBuilder;
  @MockitoBean SubmissionMessagesBuilder submissionMessagesBuilder;
  @MockitoBean SubmissionMatterStartsDetailsBuilder submissionMatterStartsDetailsBuilder;
  @MockitoBean DataClaimsRestClient dataClaimsRestClient;
  @MockitoBean PaginationLinksBuilder paginationLinksBuilder;

  SubmissionDetailViewTest() {
    this.mapping = String.format("/view-submission-detail?submissionId=%s", submissionId);
  }

  @Test
  void viewHasSortableClaimHeaders_crime() {
    mockClaims(AreaOfLaw.CRIME_LOWER);

    var doc = renderDocument();

    Elements headers = getTableHeaders(doc);

    assertTableHeaderIsNotSortable(headers.get(0), "Claim");
    assertTableHeaderIsSortable(
        headers.get(1),
        "none",
        "Client surname",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=client_surname,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(2),
        "none",
        "Client initial",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=client_forename,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(3),
        "none",
        "UFN",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=unique_file_number,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(4),
        "none",
        "Fee code",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=fee_code,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(5),
        "none",
        "Date work concluded",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=case_concluded_date,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(6),
        "none",
        "Calculated value",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=total_amount,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(7),
        "none",
        "Escape case",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=escape_case_flag,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(8),
        "none",
        "Messages",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=total_warnings,asc",
            submissionId));
  }

  @Test
  void viewHasSortableClaimHeaders_civil() {
    mockClaims(AreaOfLaw.LEGAL_HELP);

    var doc = renderDocument();

    Elements headers = getTableHeaders(doc);

    assertTableHeaderIsNotSortable(headers.get(0), "Claim");
    assertTableHeaderIsSortable(
        headers.get(1),
        "none",
        "Client surname",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=client_surname,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(2),
        "none",
        "Client forename",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=client_forename,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(3),
        "none",
        "UFN",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=unique_file_number,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(4),
        "none",
        "UCN",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=unique_client_number,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(5),
        "none",
        "Fee code",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=fee_code,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(6),
        "none",
        "Calculated value",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=total_amount,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(7),
        "none",
        "Escape case",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=escape_case_flag,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(8),
        "none",
        "Messages",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=total_warnings,asc",
            submissionId));
  }

  @Test
  void viewHasSortableClaimHeaders_mediation() {
    mockClaims(AreaOfLaw.MEDIATION);

    var doc = renderDocument();

    Elements headers = getTableHeaders(doc);

    assertTableHeaderIsNotSortable(headers.get(0), "Claim");
    assertTableHeaderIsSortable(
        headers.get(1),
        "none",
        "Client 1 surname",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=client_surname,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(2),
        "none",
        "Client 1 forename",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=client_forename,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(3),
        "none",
        "Client 1 UCN",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=unique_client_number,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(4),
        "none",
        "Client 2 surname",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=client_2_surname,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(5),
        "none",
        "Client 2 forename",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=client_2_forename,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(6),
        "none",
        "Client 2 UCN",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=client_2_ucn,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(7),
        "none",
        "Fee code",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=fee_code,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(8),
        "none",
        "Calculated value",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_DETAILS&page=0&sort=total_amount,asc",
            submissionId));
  }

  @Test
  void viewSubmissionDetailHasSortableWarningHeaders_crime() {
    mockWarningMessages(AreaOfLaw.CRIME_LOWER);

    var doc = renderDocumentWithParams(Map.of("navTab", "CLAIM_MESSAGES"));

    Elements headers = getTableHeaders(doc);

    assertTableHeaderIsNotSortable(headers.get(0), "Claim");
    assertTableHeaderIsSortable(
        headers.get(1),
        "none",
        "Client surname",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=client_surname,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(2),
        "none",
        "Client initial",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=client_forename,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(3),
        "none",
        "UFN",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=unique_file_number,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(4),
        "none",
        "Messages",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=display_message,asc",
            submissionId));
  }

  @Test
  void viewSubmissionDetailHasSortableWarningHeaders_civil() {
    mockWarningMessages(AreaOfLaw.LEGAL_HELP);

    var doc = renderDocumentWithParams(Map.of("navTab", "CLAIM_MESSAGES"));

    Elements headers = getTableHeaders(doc);

    assertTableHeaderIsNotSortable(headers.get(0), "Claim");
    assertTableHeaderIsSortable(
        headers.get(1),
        "none",
        "Client surname",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=client_surname,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(2),
        "none",
        "Client initial",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=client_forename,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(3),
        "none",
        "UFN",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=unique_file_number,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(4),
        "none",
        "UCN",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=unique_client_number,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(5),
        "none",
        "Messages",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=display_message,asc",
            submissionId));
  }

  @Test
  void viewSubmissionDetailHasSortableWarningHeaders_mediation() {
    mockWarningMessages(AreaOfLaw.MEDIATION);

    var doc = renderDocumentWithParams(Map.of("navTab", "CLAIM_MESSAGES"));

    Elements headers = getTableHeaders(doc);

    assertTableHeaderIsNotSortable(headers.get(0), "Claim");
    assertTableHeaderIsSortable(
        headers.get(1),
        "none",
        "Client 1 surname",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=client_surname,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(2),
        "none",
        "Client 1 forename",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=client_forename,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(3),
        "none",
        "Client 1 UCN",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=unique_client_number,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(4),
        "none",
        "Client 2 surname",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=client_2_surname,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(5),
        "none",
        "Client 2 forename",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=client_2_forename,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(6),
        "none",
        "Client 2 UCN",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=client_2_ucn,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(7),
        "none",
        "Messages",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=display_message,asc",
            submissionId));
  }

  @Test
  void viewSubmissionDetailHasSortableClaimErrorHeaders_crime() {
    mockErrorMessages(AreaOfLaw.CRIME_LOWER, MessagesSource.CLAIM);

    var doc = renderDocumentWithParams(Map.of("navTab", "CLAIM_MESSAGES"));

    Elements headers = getTableHeaders(doc);

    assertTableHeaderIsSortable(
        headers.get(0),
        "none",
        "Client surname",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=client_surname,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(1),
        "none",
        "Client initial",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=client_forename,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(2),
        "none",
        "UFN",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=unique_file_number,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(3),
        "none",
        "Messages",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=display_message,asc",
            submissionId));
  }

  @Test
  void viewSubmissionDetailHasSortableClaimErrorHeaders_civil() {
    mockErrorMessages(AreaOfLaw.LEGAL_HELP, MessagesSource.CLAIM);

    var doc = renderDocumentWithParams(Map.of("navTab", "CLAIM_MESSAGES"));

    Elements headers = getTableHeaders(doc);

    assertTableHeaderIsSortable(
        headers.get(0),
        "none",
        "Client surname",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=client_surname,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(1),
        "none",
        "Client initial",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=client_forename,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(2),
        "none",
        "UFN",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=unique_file_number,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(3),
        "none",
        "UCN",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=unique_client_number,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(4),
        "none",
        "Messages",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=display_message,asc",
            submissionId));
  }

  @Test
  void viewSubmissionDetailHasSortableClaimErrorHeaders_mediation() {
    mockErrorMessages(AreaOfLaw.MEDIATION, MessagesSource.CLAIM);

    var doc = renderDocumentWithParams(Map.of("navTab", "CLAIM_MESSAGES"));

    Elements headers = getTableHeaders(doc);

    assertTableHeaderIsSortable(
        headers.get(0),
        "none",
        "Client 1 surname",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=client_surname,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(1),
        "none",
        "Client 1 forename",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=client_forename,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(2),
        "none",
        "Client 1 UCN",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=unique_client_number,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(3),
        "none",
        "Client 2 surname",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=client_2_surname,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(4),
        "none",
        "Client 2 forename",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=client_2_forename,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(5),
        "none",
        "Client 2 UCN",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=client_2_ucn,asc",
            submissionId));
    assertTableHeaderIsSortable(
        headers.get(6),
        "none",
        "Messages",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=display_message,asc",
            submissionId));
  }

  @Test
  void viewSubmissionDetailHasSortableSubmissionErrorHeaders() {
    mockErrorMessages(AreaOfLaw.CRIME_LOWER, MessagesSource.SUBMISSION);

    var doc = renderDocumentWithParams(Map.of("navTab", "CLAIM_MESSAGES"));

    Elements headers = getTableHeaders(doc);

    assertTableHeaderIsSortable(
        headers.get(0),
        "none",
        "Messages",
        String.format(
            "/view-submission-detail?submissionId=%s&navTab=CLAIM_MESSAGES&messagesPage=0&messagesSort=display_message,asc",
            submissionId));
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
        .thenReturn(
            new MessagesSummary(Collections.emptyList(), 0, 0, pagination, MessagesSource.CLAIM));
  }

  private void mockWarningMessages(AreaOfLaw areaOfLaw) {
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
        .thenReturn(
            new MessagesSummary(
                List.of(
                    MessageRow.builder().claimReference(Optional.of(UUID.randomUUID())).build()),
                0,
                0,
                pagination,
                MessagesSource.CLAIM));
  }

  private void mockErrorMessages(AreaOfLaw areaOfLaw, MessagesSource messagesSource) {
    Page pagination = Page.builder().totalPages(1).totalElements(1).number(0).size(10).build();
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder()
            .submissionId(submissionId)
            .status(SubmissionStatus.VALIDATION_FAILED)
            .areaOfLaw(areaOfLaw)
            .build();
    when(dataClaimsRestClient.getSubmission(submissionId))
        .thenReturn(Mono.just(submissionResponse));
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

    when(submissionMessagesBuilder.buildErrors(any(), anyInt(), anyInt(), any()))
        .thenReturn(
            new MessagesSummary(
                List.of(MessageRow.builder().build()), 0, 0, pagination, messagesSource));
  }
}
