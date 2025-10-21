package uk.gov.justice.laa.bulkclaim.controller;

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
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

/**
 * Controller for handling the upload being checked page after a user has submitted a bulk claim.
 *
 * @author Jamie Briggs
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@SessionAttributes({SUBMISSION_ID, UPLOADED_FILENAME, SUBMISSION_DATE_TIME})
public class BulkUploadBeingCheckedController {

  private final DataClaimsRestClient dataClaimsRestClient;

  private final List<SubmissionStatus> completedStatuses =
      List.of(SubmissionStatus.VALIDATION_SUCCEEDED, SubmissionStatus.VALIDATION_FAILED);

  /**
   * Shows the import in progress page, and refreshes every 5 seconds. Redirects if the submission
   * is ready.
   *
   * @param model the Spring model.
   * @param submissionId the submission id session attribute.
   * @return the import in progress view or redirects to view submission.
   */
  @GetMapping("/upload-is-being-checked")
  public String uploadBeingChecked(Model model, @ModelAttribute(SUBMISSION_ID) UUID submissionId) {

    // Check submission exists otherwise they will be stuck in a loop on this page.
    SubmissionResponse submission;

    try {
      submission = dataClaimsRestClient.getSubmission(submissionId).block();
    } catch (WebClientResponseException e) {
      if (e.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(404))) {
        log.debug("No submission found, will retry: %s".formatted(submissionId.toString()));
        model.addAttribute("shouldRefresh", true);
        return "pages/upload-being-checked";
      }
      throw new SubmitBulkClaimException("Claims API returned an error", e);
    }

    // Redirect if submission is complete
    if (completedStatuses.contains(submission.getStatus())) {
      return "redirect:/submission/%s".formatted(submissionId.toString());
    }

    model.addAttribute("shouldRefresh", true);
    return "pages/upload-being-checked";
  }
}
