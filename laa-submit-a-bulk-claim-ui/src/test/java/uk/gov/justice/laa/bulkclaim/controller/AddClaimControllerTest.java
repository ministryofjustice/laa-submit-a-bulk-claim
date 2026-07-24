package uk.gov.justice.laa.bulkclaim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimPost;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.CreateClaim201Response;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

class AddClaimControllerTest {

  private final DataClaimsRestClient client = org.mockito.Mockito.mock(DataClaimsRestClient.class);
  private final AddClaimController controller = new AddClaimController(client);

  @Test
  void addsClaimToDraftAndReturnsToUpdatedSubmission() {
    UUID submissionId = UUID.randomUUID();
    UUID claimId = UUID.randomUUID();
    when(client.getSubmission(submissionId))
        .thenReturn(
            Mono.just(
                new SubmissionResponse()
                    .status(SubmissionStatus.READY_FOR_SUBMISSION)
                    .officeAccountNumber("OFFICE")));
    when(client.getClaims("OFFICE", submissionId, 0, 10_000))
        .thenReturn(
            ResponseEntity.ok(
                new ClaimResultSet()
                    .content(
                        List.of(
                            new ClaimResponse().lineNumber(2),
                            new ClaimResponse().lineNumber(7)))));
    when(client.createClaim(eq(submissionId), any()))
        .thenReturn(ResponseEntity.ok(new CreateClaim201Response().id(claimId)));

    String view = controller.addClaim(submissionId);

    ArgumentCaptor<ClaimPost> claim = ArgumentCaptor.forClass(ClaimPost.class);
    verify(client).createClaim(eq(submissionId), claim.capture());
    assertThat(claim.getValue().getCreatedByUserId()).isEqualTo("Submit-a-bulk-claim");
    assertThat(claim.getValue().getStatus()).isEqualTo(ClaimStatus.INVALID);
    assertThat(claim.getValue().getLineNumber()).isEqualTo(8);
    assertThat(claim.getValue().getMatterTypeCode()).isEqualTo("TBC");
    assertThat(view).isEqualTo("redirect:/submission/%s".formatted(submissionId));
  }

  @Test
  void doesNotAddClaimWhenSubmissionIsNoLongerDraft() {
    UUID submissionId = UUID.randomUUID();
    when(client.getSubmission(submissionId))
        .thenReturn(
            Mono.just(new SubmissionResponse().status(SubmissionStatus.VALIDATION_SUCCEEDED)));

    String view = controller.addClaim(submissionId);

    verify(client, never()).createClaim(any(), any());
    assertThat(view).isEqualTo("redirect:/submission/%s".formatted(submissionId));
  }
}
