package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.BULK_SUBMISSION_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.BulkSubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.GetBulkSubmissionStatusById200Response;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BulkUploadBeingCheckedController {

  private final DataClaimsRestClient dataClaimsRestClient;

  private final List<BulkSubmissionStatus> completedStatuses =
      List.of(BulkSubmissionStatus.VALIDATION_SUCCEEDED, BulkSubmissionStatus.VALIDATION_FAILED);

  private final List<BulkSubmissionStatus> pendingStatuses =
      List.of(BulkSubmissionStatus.READY_FOR_PARSING, BulkSubmissionStatus.PARSING_COMPLETED);

  /**
   * Shows the import in progress page, and refreshes every 5 seconds. Redirects if the submission
   * is ready.
   */
  @GetMapping("/upload-is-being-checked")
  public String uploadBeingChecked(
      Model model,
      @SessionAttribute(name = SUBMISSION_ID, required = false) UUID submissionId,
      @SessionAttribute(name = BULK_SUBMISSION_ID, required = false) UUID bulkSubmissionId) {

    if (submissionId == null || bulkSubmissionId == null) {
      log.info("Submission IDs not found in session, redirecting to landing page");
      return "redirect:/";
    }

    try {
      GetBulkSubmissionStatusById200Response bulkSubmission =
          dataClaimsRestClient.getBulkSubmissionSummary(bulkSubmissionId).block();
      Assert.notNull(bulkSubmission, "Bulk submission summary cannot be null");

      BulkSubmissionStatus bulkSubmissionStatus = bulkSubmission.getStatus();
      if (bulkSubmissionStatus == BulkSubmissionStatus.PARSING_FAILED) {
        throw new SubmitBulkClaimException(
            "Bulk submission parsing failed for: " + bulkSubmissionId);
      }
      if (pendingStatuses.contains(bulkSubmissionStatus)) {
        model.addAttribute("shouldRefresh", true);
        return "pages/upload-being-checked";
      }
      if (completedStatuses.contains(bulkSubmissionStatus)) {
        return "redirect:/submissions/%s".formatted(submissionId.toString());
      }
      throw new SubmitBulkClaimException(
          "Unexpected bulk submission status returned for: " + bulkSubmissionId);
    } catch (WebClientResponseException e) {
      if (e.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(404))) {
        log.debug(
            "No bulk submission found, will retry: %s".formatted(bulkSubmissionId.toString()));
        model.addAttribute("shouldRefresh", true);
        return "pages/upload-being-checked";
      }
      throw new SubmitBulkClaimException("Claims API returned an error", e);
    }
  }
}
