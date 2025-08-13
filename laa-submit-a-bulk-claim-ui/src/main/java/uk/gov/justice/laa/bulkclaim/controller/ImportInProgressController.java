package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.config.SessionConstants.BULK_SUBMISSION;
import static uk.gov.justice.laa.bulkclaim.config.SessionConstants.BULK_SUBMISSION_ID;

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
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;
import uk.gov.justice.laa.claims.model.GetSubmission200ResponseClaimsInner;
/**
 * Controller for handling the import in progress page after a user has submitted a bulk claim.
 *
 * @author Jamie Briggs
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@SessionAttributes({BULK_SUBMISSION_ID, BULK_SUBMISSION})
public class ImportInProgressController {

  private final DataClaimsRestService dataClaimsRestService;

  private final List<String> completedStatuses =
      List.of("VALIDATION_SUCCEEDED", "VALIDATION_FAILED");

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
      Model model, @ModelAttribute(BULK_SUBMISSION_ID) UUID bulkSubmissionId) {

    // Check submission exists otherwise they will be stuck in a loop on this page.
    GetSubmission200Response bulkSubmission;
    try {
      bulkSubmission = dataClaimsRestService.getSubmission(bulkSubmissionId).block();
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
    Assert.notNull(getSubmission, "Submission is null");
    Assert.notNull(getSubmission.getSubmission(), "Submission fields is null");

    // Check for NIL submission
    if (Boolean.TRUE.equals(getSubmission.getSubmission().getIsNilSubmission())) {
      // TODO: Redirect to imported page CCMSPUI-788
      log.info("NIL submission found, will redirect: %s".formatted(bulkSubmissionId.toString()));
      return "redirect:/";
    }

    // Check submission has claims otherwise they will be stuck in a loop on this page.
    Assert.notEmpty(
        getSubmission.getClaims(),
        "No claims found for bulk submission: %s".formatted(bulkSubmissionId.toString()));

    boolean fullyImported =
        getSubmission.getClaims().stream()
            .map(GetSubmission200ResponseClaimsInner::getStatus)
            .allMatch(completedStatuses::contains);
    if (fullyImported) {
      return "redirect:/view-submission-summary";
    }
    model.addAttribute("shouldRefresh", true);
    return "pages/upload-in-progress";
  }
}
