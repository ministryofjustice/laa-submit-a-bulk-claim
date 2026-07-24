package uk.gov.justice.laa.bulkclaim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import uk.gov.justice.laa.bulkclaim.dto.confirmation.ClaimConfirmationError;
import uk.gov.justice.laa.bulkclaim.exception.DraftConfirmationValidationException;
import uk.gov.justice.laa.bulkclaim.service.DraftSubmissionService;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessagePatch;

class SubmitDraftControllerTest {

  @Test
  void rejectedConfirmationReturnsProviderToConfirmationPageWithClaimErrors() {
    UUID submissionId = UUID.randomUUID();
    UUID claimId = UUID.randomUUID();
    ClaimConfirmationError error =
        new ClaimConfirmationError(
            claimId,
            List.of(new ValidationMessagePatch().displayMessage("Complete the inquest details")));
    DraftSubmissionService draftSubmissionService =
        org.mockito.Mockito.mock(DraftSubmissionService.class);
    doThrow(new DraftConfirmationValidationException(List.of(error)))
        .when(draftSubmissionService)
        .submitDraftSubmission(submissionId);
    SubmitDraftController controller =
        new SubmitDraftController(null, null, draftSubmissionService);
    RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

    String view = controller.postSubmitDraft(submissionId, redirectAttributes);

    assertThat(view)
        .isEqualTo("redirect:/submit-draft-submission?submissionId=%s".formatted(submissionId));
    assertThat(redirectAttributes.getFlashAttributes().get("confirmationErrors"))
        .isEqualTo(List.of(error));
  }
}
