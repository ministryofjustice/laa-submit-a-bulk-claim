package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.BULK_SUBMISSION_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_DATE_TIME;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.UPLOADED_FILENAME;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.BulkSubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.GetBulkSubmission200Response;

/**
 * Controller for handling the upload being checked page after a user has submitted a bulk claim.
 *
 * @author Jamie Briggs
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@SessionAttributes({SUBMISSION_ID, BULK_SUBMISSION_ID, UPLOADED_FILENAME, SUBMISSION_DATE_TIME})
public class BulkUploadBeingCheckedController {

  private final DataClaimsRestClient dataClaimsRestClient;

  private final List<BulkSubmissionStatus> completedStatuses =
      List.of(BulkSubmissionStatus.VALIDATION_SUCCEEDED, BulkSubmissionStatus.VALIDATION_FAILED);

  /**
   * Shows the import in progress page, and refreshes every 5 seconds. Redirects if the submission
   * is ready.
   *
   * @param model the Spring model.
   * @param bulkSubmissionId the submission id session attribute.
   * @return the import in progress view or redirects to view submission.
   */
  @GetMapping("/upload-is-being-checked")
  public String uploadBeingChecked(
      Model model,
      @ModelAttribute(SUBMISSION_ID) UUID submissionId,
      @ModelAttribute(BULK_SUBMISSION_ID) UUID bulkSubmissionId) {

    GetBulkSubmission200Response bulkSubmission;
    try {
      bulkSubmission = dataClaimsRestClient.getBulkSubmission(bulkSubmissionId).block();
      if (bulkSubmission != null && completedStatuses.contains(bulkSubmission.getStatus())) {
        return "redirect:/submission/%s".formatted(submissionId.toString());
      }
    } catch (WebClientResponseException e) {
      if (e.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(404))) {
        log.debug(
            "No bulk submission found, will retry: %s".formatted(bulkSubmissionId.toString()));
      } else {
        throw new SubmitBulkClaimException("Claims API returned an error", e);
      }
    }
    model.addAttribute("shouldRefresh", true);
    return "pages/upload-being-checked";
  }
}
