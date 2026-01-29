package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.CLAIM_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMessagesBuilder;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.constants.ViewSubmissionNavigationTab;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimFeeCalculationBreakdownMapper;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimSummaryMapper;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

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
  private final SubmissionMessagesBuilder submissionMessagesBuilder;

  /**
   * Gets the claim reference, stores it in the session and redirects to the view claim detail page.
   *
   * @param model the spring model
   * @param claimReference the claim reference
   * @return the redirect to view a claim detail
   */
  @GetMapping("/submission/claim/{claimReference}")
  public String getClaimDetail(
      Model model,
      @PathVariable("claimReference") UUID claimReference,
      @RequestParam(value = "page", defaultValue = "0") final int page,
      @RequestParam(value = "navTab", required = false, defaultValue = "CLAIM_DETAILS")
          final ViewSubmissionNavigationTab navigationTab) {

    String uri =
        UriComponentsBuilder.fromPath("/view-claim-detail")
            .queryParam("page", page)
            .queryParam("navTab", navigationTab.toString())
            .toUriString();

    return "redirect:" + uri;
  }

  /**
   * Views the submission detail page.
   *
   * @param model the spring model
   * @param submissionId the submission id in the session
   * @param claimId the claim id in the session
   * @return the view claim detail page
   */
  @GetMapping("/view-claim-detail")
  public String getClaimDetail(
      Model model,
      @ModelAttribute(SUBMISSION_ID) final UUID submissionId,
      @ModelAttribute(CLAIM_ID) final UUID claimId,
      @RequestParam(value = "page", defaultValue = "0") final int page,
      @RequestParam(value = "navTab", required = false, defaultValue = "CLAIM_DETAILS")
          final ViewSubmissionNavigationTab navigationTab) {

    model.addAttribute("page", page);
    model.addAttribute("navigationTab", navigationTab.toString());

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
    SubmissionResponse submissionResponse =
        dataClaimsRestClient.getSubmission(submissionId).block();
    String areaOfLaw = submissionResponse.getAreaOfLaw().getValue();
    model.addAttribute("claimSummary", claimSummaryMapper.toClaimSummary(claimResponse, areaOfLaw));

    final MessagesSummary messagesSummary =
        submissionMessagesBuilder.buildAllWarnings(submissionId, claimId);
    model.addAttribute("claimMessages", messagesSummary);

    return "pages/view-claim-detail";
  }
}
