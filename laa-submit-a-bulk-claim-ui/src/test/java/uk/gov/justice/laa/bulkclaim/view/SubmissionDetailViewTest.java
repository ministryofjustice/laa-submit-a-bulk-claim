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
  void viewSubmissionDetailHasSortableClaimHeaders() {

    Page pagination = Page.builder().totalPages(1).totalElements(1).number(0).size(10).build();
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder()
            .submissionId(submissionId)
            .status(SubmissionStatus.VALIDATION_SUCCEEDED)
            .areaOfLaw(AreaOfLaw.CRIME_LOWER)
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
                AreaOfLaw.CRIME_LOWER.getValue(),
                OffsetDateTime.of(2025, 1, 1, 10, 10, 10, 0, ZoneOffset.UTC)));

    when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt(), anyString()))
        .thenReturn(
            new SubmissionClaimsDetails(
                List.of(SubmissionClaimRow.builder().build()), pagination, BigDecimal.ONE));

    when(submissionMessagesBuilder.build(any(), any(), any(), anyInt(), anyInt()))
        .thenReturn(
            new MessagesSummary(Collections.emptyList(), 0, 0, pagination, MessagesSource.CLAIM));

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
}
