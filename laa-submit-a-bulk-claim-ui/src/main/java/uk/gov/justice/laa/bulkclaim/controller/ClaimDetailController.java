package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.CLAIM_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimFeeCalculationBreakdownMapper;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimSummaryMapper;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;

/**
 * Controller for handling viewing a claim from a submission.
 *
 * @author Jamie Briggs
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@SessionAttributes({SUBMISSION_ID, CLAIM_ID})
public final class ClaimDetailController {

  private final DataClaimsRestClient dataClaimsRestClient;
  private final ClaimSummaryMapper claimSummaryMapper;
  private final ClaimFeeCalculationBreakdownMapper claimFeeCalculationBreakdownMapper;

  /**
   * Gets the claim reference, stores it in the session and redirects to the view claim detail
   * page.
   *
   * @param claimReference the claim reference
   * @param httpSession    the http session
   * @return the redirect to view a claim detail
   */
  @GetMapping("/submission/claim/{claimReference}")
  public String getClaimDetail(
      @PathVariable("claimReference") UUID claimReference, HttpSession httpSession) {
    httpSession.setAttribute(CLAIM_ID, claimReference);
    return "redirect:/view-claim-detail";
  }

  /**
   * Views the submission detail page.
   *
   * @param model        the spring model
   * @param submissionId the submission id in the session
   * @param claimId      the claim id in the session
   * @return the view claim detail page
   */
  @GetMapping("/view-claim-detail")
  public String getClaimDetail(
      Model model,
      @ModelAttribute(SUBMISSION_ID) final UUID submissionId,
      @ModelAttribute(CLAIM_ID) final UUID claimId) {

    String areaOfLaw =
        simplifyAreaOfLaw(dataClaimsRestClient.getSubmission(submissionId).block().getAreaOfLaw());
    ClaimResponse claimResponse =
        dataClaimsRestClient
            .getSubmissionClaim(submissionId, claimId)
            .blockOptional()
            .orElseThrow(
                () ->
                    new SubmitBulkClaimException(
                        "Claim %s does not exist for submission %s"
                            .formatted(claimId.toString(), submissionId.toString())));
    model.addAttribute("ufn", claimResponse.getUniqueFileNumber());

    Assert.notNull(claimResponse.getFeeCalculationResponse(), "Fee calculation response is null");
    model.addAttribute(
        "feeDetails",
        claimFeeCalculationBreakdownMapper.toClaimFeeCalculationBreakdown(claimResponse));
    model.addAttribute("claimSummary", claimSummaryMapper.toClaimSummary(claimResponse, areaOfLaw));

    return "pages/view-claim-detail";
  }

  private String simplifyAreaOfLaw(String areaOfLaw) {
    return areaOfLaw.toUpperCase(Locale.ROOT).replace("LEGAL HELP", "CIVIL")
        .replace("CRIME LOWER", "CRIME");
  }
}
