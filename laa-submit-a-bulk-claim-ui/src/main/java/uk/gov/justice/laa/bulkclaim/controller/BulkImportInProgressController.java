package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.BULK_SUBMISSION;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.BULK_SUBMISSION_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.UPLOADED_FILENAME;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.laa.bulkclaim.dto.UploadInProgressSummary;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;
import uk.gov.justice.laa.claims.model.SubmissionStatus;

/**
 * Controller for handling the import in progress page after a user has submitted a bulk claim.
 *
 * @author Jamie Briggs
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@SessionAttributes({BULK_SUBMISSION_ID, BULK_SUBMISSION, UPLOADED_FILENAME})
public class BulkImportInProgressController {

  private final DataClaimsRestService dataClaimsRestService;

  private final List<SubmissionStatus> completedStatuses =
      List.of(SubmissionStatus.VALIDATION_SUCCEEDED, SubmissionStatus.VALIDATION_FAILED);

  /**
   * Shows the import in progress page, and refreshes every 5 seconds. Redirects if the submission
   * is ready.
   *
   * @param model the Spring model.
   * @param bulkSubmissionId the bulk submission id session attribute.
   * @return the import in progress view or redirects to view submission.
   */
  @GetMapping("/import-in-progress")
  public String importInProgress(
      Model model,
      @ModelAttribute(BULK_SUBMISSION_ID) UUID bulkSubmissionId,
      @ModelAttribute(UPLOADED_FILENAME) String uploadedFilename) {

    // Check submission exists otherwise they will be stuck in a loop on this page.
    GetSubmission200Response submission;
    try {
      submission = dataClaimsRestService.getSubmission(bulkSubmissionId).block();
    } catch (WebClientResponseException e) {
      if (e.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(404))) {
        log.debug("No submission found, will retry: %s".formatted(bulkSubmissionId.toString()));
        model.addAttribute("shouldRefresh", true);
        return "pages/upload-in-progress";
      }
      throw new SubmitBulkClaimException("Claims API returned an error", e);
    }

    // Check submission. If the response from data claims API is 200, these fields
    //  should be not null.
    Assert.notNull(submission, "Submission fields is null");

    // Get summary
    UploadInProgressSummary summary =
        new UploadInProgressSummary(
            submission.getSubmitted(), submission.getSubmissionId(), uploadedFilename);
    model.addAttribute("inProgressSummary", summary);

    // Check for NIL submission
    if (Boolean.TRUE.equals(submission.getIsNilSubmission())) {
      log.info("NIL submission found, will redirect: %s".formatted(bulkSubmissionId.toString()));
      return "redirect:/view-submission-summary";
    }

    // Redirect if submission is complete
    if (completedStatuses.contains(submission.getStatus())) {
      return "redirect:/view-submission-summary";
    }
    model.addAttribute("shouldRefresh", true);
    return "pages/upload-in-progress";
  }
}
