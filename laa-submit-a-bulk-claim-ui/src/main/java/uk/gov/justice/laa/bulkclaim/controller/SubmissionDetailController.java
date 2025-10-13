package uk.gov.justice.laa.bulkclaim.controller;

import static org.springframework.beans.support.PagedListHolder.DEFAULT_PAGE_SIZE;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;
import static uk.gov.justice.laa.bulkclaim.constants.ViewSubmissionNavigationTab.CLAIM_DETAILS;

import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionClaimDetailsBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionClaimMessagesBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMatterStartsDetailsBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionSummaryBuilder;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.constants.ViewSubmissionNavigationTab;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.ClaimMessagesSummary;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimsDetails;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionBase;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

/**
 * Controller for handling viewing a submission.
 *
 * @author Jamie Briggs
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@SessionAttributes({SUBMISSION_ID})
public class SubmissionDetailController {

  private final SubmissionSummaryBuilder submissionSummaryBuilder;
  private final SubmissionClaimDetailsBuilder submissionClaimDetailsBuilder;
  private final SubmissionClaimMessagesBuilder submissionClaimMessagesBuilder;
  private final SubmissionMatterStartsDetailsBuilder submissionMatterStartsDetailsBuilder;
  private final DataClaimsRestClient dataClaimsRestClient;

  /**
   * Gets the submission reference, stores it in the session and redirects to the view submission.
   *
   * @param submissionReference the submission reference
   * @param httpSession the http session
   * @return the redirect to view a submission detail
   */
  @GetMapping("/submission/{submissionReference}")
  public String getSubmissionReference(
      @PathVariable("submissionReference") UUID submissionReference,
      @SessionAttribute("submissions") SubmissionsResultSet submissions,
      HttpSession httpSession,
      RedirectAttributes redirectAttributes) {

    // Find the submission by ID
    SubmissionBase submission =
        submissions.getContent().stream()
            .filter(s -> submissionReference.equals(s.getSubmissionId()))
            .findFirst()
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Submission not found for user"));

    // Store the submission ID in session
    httpSession.setAttribute(SUBMISSION_ID, submissionReference);

    // If validation is in progress, redirect to /import-in-progress and add submission details
    if (submission.getStatus() == SubmissionStatus.VALIDATION_IN_PROGRESS) {
      redirectAttributes.addFlashAttribute("submission", submission);
      return "redirect:/import-in-progress";
    }

    // Otherwise, redirect to the standard submission details view
    return "redirect:/view-submission-detail";
  }

  /**
   * Views the submission detail page.
   *
   * @param model the spring model
   * @param submissionId the submission id in the session
   * @return the view submission detail page
   */
  @GetMapping("/view-submission-detail")
  public String getSubmissionDetail(
      Model model,
      @RequestParam(value = "page", defaultValue = "0") final int page,
      @ModelAttribute(SUBMISSION_ID) UUID submissionId,
      @RequestParam(value = "navTab", required = false, defaultValue = "CLAIM_DETAILS")
          ViewSubmissionNavigationTab navigationTab) {
    final SubmissionResponse submissionResponse =
        dataClaimsRestClient
            .getSubmission(submissionId)
            .blockOptional()
            .orElseThrow(
                () ->
                    new SubmitBulkClaimException(
                        "Submission %s does not exist".formatted(submissionId.toString())));

    final SubmissionSummary submissionSummary = submissionSummaryBuilder.build(submissionResponse);

    if ("invalid".equalsIgnoreCase(submissionSummary.status())) {
      final ClaimMessagesSummary claimErrorSummary =
          submissionClaimMessagesBuilder.buildErrors(submissionId, page, DEFAULT_PAGE_SIZE);
      model.addAttribute("claimErrorDetails", claimErrorSummary);
    }
    if (CLAIM_DETAILS.equals(navigationTab)) {
      final SubmissionClaimsDetails claimDetails =
          submissionClaimDetailsBuilder.build(submissionResponse, page, DEFAULT_PAGE_SIZE);
      model.addAttribute("claimDetails", claimDetails);
    } else {
      final SubmissionMatterStartsDetails build =
          submissionMatterStartsDetailsBuilder.build(submissionResponse);
      model.addAttribute("matterStartsDetails", build);
    }
    model.addAttribute("submissionSummary", submissionSummary);
    model.addAttribute("navTab", navigationTab);

    return "pages/view-submission-detail";
  }
}
