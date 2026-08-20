package uk.gov.justice.laa.bulkclaim.controller;

import static java.lang.Boolean.TRUE;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.CLAIM_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMessagesBuilder;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClientV2;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.constants.ViewSubmissionNavigationTab;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimFeeCalculationBreakdownMapper;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimSummaryMapper;
import uk.gov.justice.laa.bulkclaim.service.ClaimService;
import uk.gov.justice.laa.bulkclaim.service.SubmissionService;
import uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails.ClaimDetailPageData;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@Slf4j
@Controller
@RequiredArgsConstructor
public final class ClaimDetailController {

  private final ClaimSummaryMapper claimSummaryMapper;
  private final ClaimFeeCalculationBreakdownMapper claimFeeCalculationBreakdownMapper;
  private final SubmissionMessagesBuilder submissionMessagesBuilder;
  private final FeatureFlagsConfig featureFlagsConfig;
  private final ClaimService claimService;
  private final SubmissionService submissionService;

  @GetMapping("/submissions/{submissionId}/claims/{claimId}")
  public String getClaimDetail(
      Model model,
      @PathVariable UUID submissionId,
      @PathVariable UUID claimId,
      @RequestParam(value = "page", defaultValue = "0") final int page,
      @RequestParam(value = "messagesPage", defaultValue = "0") final int messagesPage,
      @RequestParam(value = "navTab", required = false, defaultValue = "CLAIM_DETAILS")
      ViewSubmissionNavigationTab navigationTab,
      @AuthenticationPrincipal OidcUser user) {

    model.addAttribute(SUBMISSION_ID, submissionId);
    model.addAttribute(CLAIM_ID, claimId);

    if (TRUE.equals(featureFlagsConfig.getIsAlternativeClaimViewEnabled())) {
      return getClaimDetail(model, submissionId, claimId, page, navigationTab, user);
    }
    return getClaimDetailOld(model, submissionId, claimId, page, messagesPage, navigationTab, user);
  }

  private String getClaimDetail(
      Model model,
      UUID submissionId,
      UUID claimId,
      int page,
      ViewSubmissionNavigationTab navigationTab,
      OidcUser user) {

    model.addAttribute("page", page);
    model.addAttribute("navigationTab", navigationTab.toString());
    model.addAttribute(
        "viewSubmissionBackLink",
        UriComponentsBuilder.fromPath("/submissions/{submissionId}")
            .queryParam("page", page)
            .queryParam("navTab", navigationTab.toString())
            .buildAndExpand(submissionId)
            .toUriString());

    final ClaimDetailPageData pageData =
        claimService.getClaimDetailPageData(submissionId, claimId, user);
    model.addAttribute("areaOfLaw", pageData.areaOfLaw().getValue());
    model.addAttribute("showCurrentCalculated", pageData.showCurrentCalculated());
    model.addAttribute("claimDetailView", pageData.claimDetailView());
    model.addAttribute("banner", pageData.banner());

    model.addAttribute("isAssessedColumnEnabled", featureFlagsConfig.getIsAssessedColumnEnabled());

    final MessagesSummary messagesSummary =
        submissionMessagesBuilder.buildAllWarnings(user, submissionId, claimId);
    model.addAttribute("claimMessages", messagesSummary);

    return "pages/view-claim-detail";
  }

  private String getClaimDetailOld(
      Model model,
      UUID submissionId,
      UUID claimId,
      int page,
      int messagesPage,
      ViewSubmissionNavigationTab navigationTab,
      OidcUser user) {

    model.addAttribute("page", page);
    model.addAttribute("messagesPage", messagesPage);
    model.addAttribute("navigationTab", navigationTab.toString());
    model.addAttribute(
        "viewSubmissionBackLink",
        UriComponentsBuilder.fromPath("/submissions/{submissionId}")
            .queryParam("page", page)
            .queryParam("navTab", navigationTab.toString())
            .queryParam("messagesPage", messagesPage)
            .buildAndExpand(submissionId)
            .toUriString());

    ClaimResponseV2 claimResponse =
        claimService
            .getClaimV2(submissionId, claimId, user);

    model.addAttribute("ufn", claimResponse.getUniqueFileNumber());
    model.addAttribute(
        "claimStatus",
        claimResponse.getStatus() == null ? null : claimResponse.getStatus().getValue());

    Assert.notNull(claimResponse.getFeeCalculationResponse(), "Fee calculation response is null");
    model.addAttribute(
        "feeDetails",
        claimFeeCalculationBreakdownMapper.toClaimFeeCalculationBreakdown(claimResponse));
    SubmissionResponse submissionResponse = submissionService.getSubmission(submissionId, user);
    String areaOfLaw = submissionResponse.getAreaOfLaw().getValue();
    model.addAttribute("claimSummary", claimSummaryMapper.toClaimSummary(claimResponse, areaOfLaw));

    final MessagesSummary messagesSummary =
        submissionMessagesBuilder.buildAllWarnings(user, submissionId, claimId);
    model.addAttribute("claimMessages", messagesSummary);

    return "pages/view-claim-detail-old";
  }
}
