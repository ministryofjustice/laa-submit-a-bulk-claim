package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;
import static uk.gov.justice.laa.bulkclaim.constants.ViewSubmissionNavigationTab.CLAIM_DETAILS;

import jakarta.servlet.http.HttpSession;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionClaimDetailsBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMatterStartsDetailsBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionSummaryBuilder;
import uk.gov.justice.laa.bulkclaim.constants.ViewSubmissionNavigationTab;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionMatterStartsDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;

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
  private final SubmissionMatterStartsDetailsBuilder submissionMatterStartsDetailsBuilder;
  private final DataClaimsRestService dataClaimsRestService;

  /**
   * Gets the submission reference, stores it in the session and redirects to the view submission.
   *
   * @param submissionReference the submission reference
   * @param httpSession the http session
   * @return the redirect to view a submission detail
   */
  @GetMapping("/submission/{submissionReference}")
  public String getSubmission(
      @PathVariable("submissionReference") UUID submissionReference, HttpSession httpSession) {
    httpSession.setAttribute(SUBMISSION_ID, submissionReference);
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
      @ModelAttribute(SUBMISSION_ID) UUID submissionId,
      @RequestParam(value = "navTab", required = false, defaultValue = "CLAIM_DETAILS")
          ViewSubmissionNavigationTab navigationTab) {
    GetSubmission200Response submissionResponse =
        dataClaimsRestService.getSubmission(submissionId).block();

    SubmissionSummary submissionSummary = submissionSummaryBuilder.build(submissionResponse);
    if (CLAIM_DETAILS.equals(navigationTab)) {
      SubmissionClaimDetails claimDetails = submissionClaimDetailsBuilder.build(submissionResponse);
      model.addAttribute("claimDetails", claimDetails);
    } else {
      SubmissionMatterStartsDetails build =
          submissionMatterStartsDetailsBuilder.build(submissionResponse);
      model.addAttribute("matterStartsDetails", build);
    }
    model.addAttribute("submissionSummary", submissionSummary);
    model.addAttribute("navTab", navigationTab);

    return "pages/view-submission-detail";
  }
}
