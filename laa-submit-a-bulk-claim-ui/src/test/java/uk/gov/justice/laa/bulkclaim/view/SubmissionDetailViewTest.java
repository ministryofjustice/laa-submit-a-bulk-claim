package uk.gov.justice.laa.bulkclaim.view;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
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
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimsDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSource;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.bulkclaim.util.PaginationLinksBuilder;
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
  void getSubmissionDetails() {

    final Page pagination =
        Page.builder().totalPages(1).totalElements(0).number(0).size(10).build();
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder().status(SubmissionStatus.VALIDATION_FAILED).build();
    when(dataClaimsRestClient.getSubmission(submissionId))
        .thenReturn(Mono.just(submissionResponse));
    when(submissionSummaryBuilder.build(any()))
        .thenReturn(
            new SubmissionSummary(
                submissionId,
                "Invalid",
                LocalDate.of(2025, 5, 1),
                "AQ2B3C",
                new BigDecimal("100.50"),
                "Legal aid",
                OffsetDateTime.of(2025, 1, 1, 10, 10, 10, 0, ZoneOffset.UTC)));
    when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt(), anyString()))
        .thenReturn(
            new SubmissionClaimsDetails(Collections.emptyList(), pagination, BigDecimal.ZERO));
    when(submissionMessagesBuilder.buildErrors(any(), anyInt(), anyInt()))
        .thenReturn(
            new MessagesSummary(Collections.emptyList(), 0, 0, pagination, MessagesSource.CLAIM));
    when(submissionMatterStartsDetailsBuilder.build(any()))
        .thenReturn(Arrays.asList(new SubmissionMatterStartsRow("Description", 34)));

    var doc = renderDocument();
  }
}
