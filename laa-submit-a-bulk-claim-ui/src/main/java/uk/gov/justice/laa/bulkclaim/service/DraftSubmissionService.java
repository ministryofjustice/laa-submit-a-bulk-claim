package uk.gov.justice.laa.bulkclaim.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.BulkSubmissionPatch;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.BulkSubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionPatch;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@Component
@RequiredArgsConstructor
public class DraftSubmissionService {

  private final DataClaimsRestClient dataClaimsRestClient;

  public void submitDraftSubmission(UUID submissionId) {
    // Get submission TODO: Perhaps have event service do this step through a message?
    var submission =
        dataClaimsRestClient
            .getSubmission(submissionId)
            .blockOptional()
            .orElseThrow(
                () ->
                    new IllegalStateException(
                        "Submission %s does not exist".formatted(submissionId)));

    SubmissionPatch submissionPatch =
        new SubmissionPatch()
            .submissionId(submissionId)
            .status(SubmissionStatus.VALIDATION_SUCCEEDED);

    UUID bulkSubmissionId = submission.getBulkSubmissionId();
    BulkSubmissionPatch bulkSubmissionPatch =
        new BulkSubmissionPatch()
            .bulkSubmissionId(bulkSubmissionId)
            .status(BulkSubmissionStatus.READY_FOR_SUBMISSION);

    dataClaimsRestClient.updateSubmission(submissionId, submissionPatch);
    dataClaimsRestClient.updateBulkSubmission(bulkSubmissionId, bulkSubmissionPatch);
  }
}
