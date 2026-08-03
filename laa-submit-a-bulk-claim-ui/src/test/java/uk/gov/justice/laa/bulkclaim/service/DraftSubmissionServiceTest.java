package uk.gov.justice.laa.bulkclaim.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.exception.DraftConfirmationValidationException;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.BulkSubmissionPatch;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.BulkSubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionPatch;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@ExtendWith(MockitoExtension.class)
class DraftSubmissionServiceTest {

  @Mock private DataClaimsRestClient dataClaimsRestClient;

  @Test
  void confirmationMovesSubmissionAndBulkSubmissionToFinalStatus() {
    UUID submissionId = UUID.randomUUID();
    UUID bulkSubmissionId = UUID.randomUUID();
    when(dataClaimsRestClient.getSubmission(submissionId))
        .thenReturn(Mono.just(new SubmissionResponse().bulkSubmissionId(bulkSubmissionId)));
    DraftSubmissionService service =
        new DraftSubmissionService(dataClaimsRestClient, JsonMapper.builder().build());

    service.submitDraftSubmission(submissionId);

    ArgumentCaptor<SubmissionPatch> submissionPatch =
        ArgumentCaptor.forClass(SubmissionPatch.class);
    verify(dataClaimsRestClient).updateSubmission(eq(submissionId), submissionPatch.capture());
    assertThat(submissionPatch.getValue().getStatus())
        .isEqualTo(SubmissionStatus.VALIDATION_SUCCEEDED);

    ArgumentCaptor<BulkSubmissionPatch> bulkSubmissionPatch =
        ArgumentCaptor.forClass(BulkSubmissionPatch.class);
    verify(dataClaimsRestClient)
        .updateBulkSubmission(eq(bulkSubmissionId), bulkSubmissionPatch.capture());
    assertThat(bulkSubmissionPatch.getValue().getStatus())
        .isEqualTo(BulkSubmissionStatus.VALIDATION_SUCCEEDED);
  }

  @Test
  void validationErrorsLeaveBothSubmissionRecordsOpen() {
    UUID submissionId = UUID.randomUUID();
    UUID bulkSubmissionId = UUID.randomUUID();
    UUID claimId = UUID.randomUUID();
    String responseBody =
        """
        {
          "claimReports": [{
            "claimId": "%s",
            "validationMessages": [{
              "displayMessage": "Complete the inquest details",
              "technicalMessage": "Missing inquest fields",
              "type": "ERROR",
              "source": "CLAIMS_API"
            }]
          }]
        }
        """
            .formatted(claimId);
    WebClientResponseException rejection =
        WebClientResponseException.create(
            400,
            "Bad Request",
            HttpHeaders.EMPTY,
            responseBody.getBytes(StandardCharsets.UTF_8),
            StandardCharsets.UTF_8);
    when(dataClaimsRestClient.getSubmission(submissionId))
        .thenReturn(Mono.just(new SubmissionResponse().bulkSubmissionId(bulkSubmissionId)));
    when(dataClaimsRestClient.updateSubmission(eq(submissionId), any())).thenThrow(rejection);
    DraftSubmissionService service =
        new DraftSubmissionService(dataClaimsRestClient, JsonMapper.builder().build());

    assertThatThrownBy(() -> service.submitDraftSubmission(submissionId))
        .isInstanceOf(DraftConfirmationValidationException.class)
        .satisfies(
            exception ->
                assertThat(
                        ((DraftConfirmationValidationException) exception)
                            .getClaimReports()
                            .getFirst()
                            .claimId())
                    .isEqualTo(claimId));

    verify(dataClaimsRestClient, never()).updateBulkSubmission(any(), any());
  }

  @Test
  void unrelatedBadRequestIsNotMisclassifiedAsConfirmationValidation() {
    UUID submissionId = UUID.randomUUID();
    UUID bulkSubmissionId = UUID.randomUUID();
    WebClientResponseException rejection =
        WebClientResponseException.create(
            400,
            "Bad Request",
            HttpHeaders.EMPTY,
            "{\"detail\":\"Invalid patch\"}".getBytes(StandardCharsets.UTF_8),
            StandardCharsets.UTF_8);
    when(dataClaimsRestClient.getSubmission(submissionId))
        .thenReturn(Mono.just(new SubmissionResponse().bulkSubmissionId(bulkSubmissionId)));
    when(dataClaimsRestClient.updateSubmission(eq(submissionId), any())).thenThrow(rejection);
    DraftSubmissionService service =
        new DraftSubmissionService(dataClaimsRestClient, JsonMapper.builder().build());

    assertThatThrownBy(() -> service.submitDraftSubmission(submissionId)).isSameAs(rejection);

    verify(dataClaimsRestClient, never()).updateBulkSubmission(any(), any());
  }

  @Test
  void discardMovesBothRecordsToDiscarded() {
    UUID submissionId = UUID.randomUUID();
    UUID bulkSubmissionId = UUID.randomUUID();
    when(dataClaimsRestClient.getSubmission(submissionId))
        .thenReturn(
            Mono.just(
                new SubmissionResponse()
                    .status(SubmissionStatus.READY_FOR_SUBMISSION)
                    .bulkSubmissionId(bulkSubmissionId)));
    DraftSubmissionService service =
        new DraftSubmissionService(dataClaimsRestClient, JsonMapper.builder().build());

    service.discardDraftSubmission(submissionId);

    ArgumentCaptor<SubmissionPatch> submissionPatch =
        ArgumentCaptor.forClass(SubmissionPatch.class);
    verify(dataClaimsRestClient).updateSubmission(eq(submissionId), submissionPatch.capture());
    assertThat(submissionPatch.getValue().getStatus()).isEqualTo(SubmissionStatus.DISCARDED);
    verify(dataClaimsRestClient, never()).updateBulkSubmission(any(), any());
  }

  @Test
  void nonDraftCannotBeDiscarded() {
    UUID submissionId = UUID.randomUUID();
    when(dataClaimsRestClient.getSubmission(submissionId))
        .thenReturn(
            Mono.just(new SubmissionResponse().status(SubmissionStatus.VALIDATION_SUCCEEDED)));
    DraftSubmissionService service =
        new DraftSubmissionService(dataClaimsRestClient, JsonMapper.builder().build());

    assertThatThrownBy(() -> service.discardDraftSubmission(submissionId))
        .isInstanceOf(IllegalStateException.class);

    verify(dataClaimsRestClient, never()).updateSubmission(any(), any());
    verify(dataClaimsRestClient, never()).updateBulkSubmission(any(), any());
  }
}
