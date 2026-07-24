package uk.gov.justice.laa.bulkclaim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionSummaryBuilder;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.submission.view.SubmissionViewQuery;
import uk.gov.justice.laa.bulkclaim.service.DraftSubmissionService;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

class DiscardDraftControllerTest {

  private final DataClaimsRestClient client = org.mockito.Mockito.mock(DataClaimsRestClient.class);
  private final SubmissionSummaryBuilder summaryBuilder =
      org.mockito.Mockito.mock(SubmissionSummaryBuilder.class);
  private final DraftSubmissionService service =
      org.mockito.Mockito.mock(DraftSubmissionService.class);
  private final DiscardDraftController controller =
      new DiscardDraftController(client, summaryBuilder, service);

  @Test
  void openingConfirmationForDraftHasNoEffectAndStoresTheConfirmedSubmission() {
    UUID submissionId = UUID.randomUUID();
    SubmissionViewQuery query = SubmissionViewQuery.builder().submissionId(submissionId).build();
    when(client.getSubmission(submissionId))
        .thenReturn(
            Mono.just(new SubmissionResponse().status(SubmissionStatus.READY_FOR_SUBMISSION)));
    ConcurrentModel model = new ConcurrentModel();

    String view = controller.getDiscardDraft(model, query);

    assertThat(view).isEqualTo("pages/confirm-discard-draft-submission");
    assertThat(model.getAttribute(SUBMISSION_ID)).isEqualTo(submissionId);
    verify(service, never()).discardDraftSubmission(submissionId);
  }

  @Test
  void nonDraftCannotOpenDiscardConfirmation() {
    UUID submissionId = UUID.randomUUID();
    SubmissionViewQuery query = SubmissionViewQuery.builder().submissionId(submissionId).build();
    when(client.getSubmission(submissionId))
        .thenReturn(
            Mono.just(new SubmissionResponse().status(SubmissionStatus.VALIDATION_SUCCEEDED)));

    String view = controller.getDiscardDraft(new ConcurrentModel(), query);

    assertThat(view).isEqualTo("redirect:/submission/%s".formatted(submissionId));
    verify(service, never()).discardDraftSubmission(submissionId);
  }

  @Test
  void confirmingDiscardUsesTheSubmissionShownOnTheConfirmationPage() {
    UUID submissionId = UUID.randomUUID();

    String view = controller.postDiscardDraft(submissionId);

    verify(service).discardDraftSubmission(submissionId);
    assertThat(view).isEqualTo("redirect:/submission/%s".formatted(submissionId));
  }
}
