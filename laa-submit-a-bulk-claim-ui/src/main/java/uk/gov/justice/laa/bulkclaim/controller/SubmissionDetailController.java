package uk.gov.justice.laa.bulkclaim.controller;

import static org.springframework.beans.support.PagedListHolder.DEFAULT_PAGE_SIZE;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import java.util.List;
import java.util.Optional;
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
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMatterStartsDetailsBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMessagesBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionSummaryBuilder;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.constants.ViewSubmissionNavigationTab;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimsDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionBase;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessageType;

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
  private final SubmissionMessagesBuilder submissionMessagesBuilder;
  private final SubmissionMatterStartsDetailsBuilder submissionMatterStartsDetailsBuilder;
  private final DataClaimsRestClient dataClaimsRestClient;

  /**
   * Gets the submission reference, stores it in the session and redirects to the view submission.
   *
   * @param submissionReference the submission reference
   * @return the redirect to view a submission detail
   */
  @GetMapping("/submission/{submissionReference}")
  public String getSubmissionReference(
      @PathVariable("submissionReference") UUID submissionReference,
      @SessionAttribute(value = "submissions", required = false) SubmissionsResultSet submissions,
      @SessionAttribute(value = SUBMISSION_ID, required = false) UUID submissionId,
      RedirectAttributes redirectAttributes,
      Model model) {

    // Validate that either submissions or submissionId is available
    if ((submissions == null || submissions.getContent() == null) && submissionId == null) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No submissions found in session");
    }

    // Try to locate submission by reference in session submissions
    SubmissionBase submission = null;
    if (submissions != null && submissions.getContent() != null) {
      submission =
          submissions.getContent().stream()
              .filter(s -> submissionReference.equals(s.getSubmissionId()))
              .findFirst()
              .orElse(null);
    }

    // If not found, check if submissionId in session matches the path variable
    boolean matchesSessionId = submissionId != null && submissionReference.equals(submissionId);
    if (submission == null && !matchesSessionId) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Submission not found for user");
    }

    // Store the submission ID in session
    model.addAttribute(SUBMISSION_ID, submissionReference);

    // Redirect based on submission status
    if (submission != null && submission.getStatus() == SubmissionStatus.VALIDATION_IN_PROGRESS) {
      redirectAttributes.addFlashAttribute("submission", submission);
      return "redirect:/upload-is-being-checked";
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

    SubmissionSummary submissionSummary = submissionSummaryBuilder.build(submissionResponse);
    boolean submissionAccepted =
        submissionResponse.getStatus() == SubmissionStatus.VALIDATION_SUCCEEDED;

    if (submissionAccepted) {
      submissionSummary =
          handleAcceptedSubmission(
              model, submissionSummary, submissionResponse, submissionId, navigationTab, page);
      addCommonSubmissionAttributes(model, submissionSummary, submissionResponse, navigationTab);
      return "pages/view-submission-detail-accepted";
    } else {
      handleInvalidSubmission(model, submissionResponse, submissionId, page);
      addCommonSubmissionAttributes(model, submissionSummary, submissionResponse, navigationTab);
      return "pages/view-submission-detail-invalid";
    }
  }

  private SubmissionSummary handleAcceptedSubmission(
      Model model,
      SubmissionSummary submissionSummary,
      SubmissionResponse submissionResponse,
      UUID submissionId,
      ViewSubmissionNavigationTab navigationTab,
      int page) {

    SubmissionClaimsDetails claimDetails =
        submissionClaimDetailsBuilder.build(submissionResponse, page, DEFAULT_PAGE_SIZE);
    model.addAttribute("claimDetails", claimDetails);

    if (claimDetails.totalClaimValue() != null) {
      submissionSummary =
          new SubmissionSummary(
              submissionSummary.submissionReference(),
              submissionSummary.status(),
              submissionSummary.submissionPeriod(),
              submissionSummary.officeAccount(),
              claimDetails.totalClaimValue(),
              submissionSummary.areaOfLaw(),
              submissionSummary.submitted());
    }

    MessagesSummary messagesSummary =
        submissionMessagesBuilder.build(
            submissionId, null, ValidationMessageType.WARNING, page, DEFAULT_PAGE_SIZE);
    model.addAttribute("messagesSummary", messagesSummary);

    addCounts(model, claimDetails, messagesSummary);
    addMatterStartsIfApplicable(model, submissionResponse, navigationTab);

    return submissionSummary;
  }

  private void handleInvalidSubmission(
      Model model, SubmissionResponse submissionResponse, UUID submissionId, int page) {

    SubmissionClaimsDetails claimDetails =
        submissionClaimDetailsBuilder.build(submissionResponse, page, DEFAULT_PAGE_SIZE);
    model.addAttribute("claimDetails", claimDetails);

    MessagesSummary messagesSummary =
        submissionMessagesBuilder.buildErrors(submissionId, page, DEFAULT_PAGE_SIZE);
    model.addAttribute("messagesSummary", messagesSummary);

    addCounts(model, claimDetails, messagesSummary);
  }

  private void addMatterStartsIfApplicable(
      Model model,
      SubmissionResponse submissionResponse,
      ViewSubmissionNavigationTab navigationTab) {

    boolean isCrimeArea =
        Optional.ofNullable(submissionResponse.getAreaOfLaw())
            .map(AreaOfLaw::getValue)
            .map(String::toLowerCase)
            .map(area -> area.contains("crime"))
            .orElse(false);

    if (ViewSubmissionNavigationTab.MATTER_STARTS.equals(navigationTab) && !isCrimeArea) {
      List<SubmissionMatterStartsRow> matterStartsDetails =
          submissionMatterStartsDetailsBuilder.build(submissionResponse);
      model.addAttribute("matterStartsDetails", matterStartsDetails);
      // For mediation submissions
      model.addAttribute(
          "totalMatterStarts",
          matterStartsDetails.stream()
              .mapToLong(SubmissionMatterStartsRow::numberOfMatterStarts)
              .sum());
    }
  }

  private void addCommonSubmissionAttributes(
      Model model,
      SubmissionSummary submissionSummary,
      SubmissionResponse submissionResponse,
      ViewSubmissionNavigationTab navigationTab) {

    model.addAttribute("submissionSummary", submissionSummary);
    model.addAttribute("submissionStatus", submissionResponse.getStatus());
    model.addAttribute("navTab", navigationTab);
  }

  private void addCounts(
      Model model, SubmissionClaimsDetails claimDetails, MessagesSummary messagesSummary) {

    int claimCount =
        Optional.ofNullable(claimDetails)
            .map(SubmissionClaimsDetails::pagination)
            .map(Page::getTotalElements)
            .orElse(0);

    int messageCount =
        Optional.ofNullable(messagesSummary).map(MessagesSummary::totalMessageCount).orElse(0);

    model.addAttribute("claimCount", claimCount);
    model.addAttribute("messageCount", messageCount);
  }
}
