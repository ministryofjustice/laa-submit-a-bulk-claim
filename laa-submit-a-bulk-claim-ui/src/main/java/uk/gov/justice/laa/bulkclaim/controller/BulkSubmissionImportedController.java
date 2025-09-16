package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.laa.bulkclaim.builder.BulkClaimSummaryBuilder;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.summary.BulkClaimImportSummary;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

/**
 * Controller which allows the user to view their submitted submissions and their errors if there
 * are any.
 *
 * @author Jamie Briggs
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@SessionAttributes({SUBMISSION_ID, SUBMISSION})
public class BulkSubmissionImportedController {

  private final DataClaimsRestClient dataClaimsRestClient;
  private final BulkClaimSummaryBuilder bulkClaimSummaryBuilder;

  /**
   * Shows the view submitted submissions page.
   *
   * @param model the Spring model.
   * @return the view submitted submission page.
   */
  @GetMapping("/view-submission-summary")
  public String getSubmission(Model model, @ModelAttribute(SUBMISSION_ID) UUID submissionId) {

    // Add bulk submission to session if it does not exist
    SubmissionResponse submission = (SubmissionResponse) model.getAttribute(SUBMISSION);

    // Add submission to session if it does not exist OR the id is different
    if (submission == null || !submissionId.equals(submission.getSubmissionId())) {
      try {
        SubmissionResponse freshSubmission =
            dataClaimsRestClient.getSubmission(submissionId).block();
        model.addAttribute(SUBMISSION, freshSubmission);
      } catch (WebClientResponseException e) {
        throw new SubmitBulkClaimException("Error retrieving submission from data claims API.", e);
      }
    }

    // Map submission summary to model
    BulkClaimImportSummary bulkClaimImportSummary =
        bulkClaimSummaryBuilder.build(
            Collections.singletonList((SubmissionResponse) model.getAttribute(SUBMISSION)));
    model.addAttribute("bulkClaimImportSummary", bulkClaimImportSummary);
    return "pages/view-submission-imported-summary";
  }
}
