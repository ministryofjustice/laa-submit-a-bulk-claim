package uk.gov.justice.laa.bulkclaim.service;

import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.confirmation.ConfirmationProblem;
import uk.gov.justice.laa.bulkclaim.exception.DraftConfirmationValidationException;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.BulkSubmissionPatch;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.BulkSubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionPatch;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@Component
@RequiredArgsConstructor
public class DraftSubmissionService {

  private final DataClaimsRestClient dataClaimsRestClient;
  private final ObjectMapper objectMapper;

  public void submitDraftSubmission(UUID submissionId) {
    // Get submission TODO: Perhaps have event service do this step through a message?
    var submission = dataClaimsRestClient.getSubmission(submissionId);
    SubmissionPatch submissionPatch =
        new SubmissionPatch()
            .submissionId(submissionId)
            .status(SubmissionStatus.VALIDATION_SUCCEEDED);
    UUID bulkSubmissionId = submission.block().getBulkSubmissionId();
    BulkSubmissionPatch bulkSubmissionPatch =
        new BulkSubmissionPatch()
            .bulkSubmissionId(bulkSubmissionId)
            .status(BulkSubmissionStatus.VALIDATION_SUCCEEDED);

    try {
      dataClaimsRestClient.updateSubmission(submissionId, submissionPatch);
    } catch (WebClientResponseException exception) {
      if (exception.getStatusCode() == HttpStatus.BAD_REQUEST) {
        Optional<DraftConfirmationValidationException> validationException =
            parseConfirmationValidationException(exception);
        if (validationException.isPresent()) {
          throw validationException.get();
        }
      }
      throw exception;
    }
    dataClaimsRestClient.updateBulkSubmission(bulkSubmissionId, bulkSubmissionPatch);
  }

  private Optional<DraftConfirmationValidationException> parseConfirmationValidationException(
      WebClientResponseException exception) {
    try {
      ConfirmationProblem problem =
          objectMapper.readValue(exception.getResponseBodyAsString(), ConfirmationProblem.class);
      if (problem.claimReports() == null || problem.claimReports().isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(new DraftConfirmationValidationException(problem.claimReports()));
    } catch (Exception parsingException) {
      return Optional.empty();
    }
  }
}
