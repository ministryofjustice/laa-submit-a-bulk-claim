package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.BULK_SUBMISSION;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.BULK_SUBMISSION_ID;

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
import uk.gov.justice.laa.bulkclaim.dto.summary.BulkClaimImportSummary;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
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
@SessionAttributes({BULK_SUBMISSION_ID, BULK_SUBMISSION})
public class BulkSubmissionImportedController {

  private final DataClaimsRestService dataClaimsRestService;
  private final BulkClaimSummaryBuilder bulkClaimSummaryBuilder;

  /**
   * Shows the view submitted submissions page.
   *
   * @param model the Spring model.
   * @return the view submitted submission page.
   */
  @GetMapping("/view-submission-summary")
  public String getSubmission(
      Model model, @ModelAttribute(BULK_SUBMISSION_ID) UUID bulkSubmissionId) {

    // Add bulk submission to session if it does not exist
    if (!model.containsAttribute(BULK_SUBMISSION)) {
      try {
        model.addAttribute(
            BULK_SUBMISSION, dataClaimsRestService.getSubmission(bulkSubmissionId).block());
      } catch (WebClientResponseException e) {
        throw new SubmitBulkClaimException("Error retrieving submission from data claims API.", e);
      }
    }

    // Map submission summary to model
    BulkClaimImportSummary bulkClaimImportSummary =
        bulkClaimSummaryBuilder.build(
            Collections.singletonList(
                (SubmissionResponse) model.getAttribute(BULK_SUBMISSION)));
    model.addAttribute("bulkClaimImportSummary", bulkClaimImportSummary);
    return "pages/view-submission-imported-summary";
  }
}
