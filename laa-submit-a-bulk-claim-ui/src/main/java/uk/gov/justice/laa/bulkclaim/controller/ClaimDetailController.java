package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.CLAIM_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import java.util.List;
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
import uk.gov.justice.laa.bulkclaim.builder.ClaimStatusBannerBuilder;
import uk.gov.justice.laa.bulkclaim.builder.LatestAssessmentResolver;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMessagesBuilder;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClientV2;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.constants.ViewSubmissionNavigationTab;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimFeeCalculationBreakdownMapper;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimSummaryMapper;
import uk.gov.justice.laa.bulkclaim.service.claimdetail.ClaimDetailView;
import uk.gov.justice.laa.bulkclaim.service.claimdetail.ClaimDetailViewFactory;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryEvent;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.DerivedClaimStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@Slf4j
@Controller
@RequiredArgsConstructor
@SessionAttributes({SUBMISSION_ID, CLAIM_ID})
public final class ClaimDetailController {

  private final DataClaimsRestClient dataClaimsRestClient;
  private final DataClaimsRestClientV2 dataClaimsRestClientV2;
  private final ClaimSummaryMapper claimSummaryMapper;
  private final ClaimFeeCalculationBreakdownMapper claimFeeCalculationBreakdownMapper;
  private final SubmissionMessagesBuilder submissionMessagesBuilder;
  private final FeatureFlagsConfig featureFlagsConfig;
  private final ClaimDetailViewFactory claimDetailViewFactory;
  private final ClaimStatusBannerBuilder claimStatusBannerBuilder;
  private final LatestAssessmentResolver latestAssessmentResolver;

  @GetMapping("/submission/claim/{claimReference}")
  public String getClaimDetail(
      Model model,
      @PathVariable("claimReference") UUID claimReference,
      @RequestParam(value = "page", defaultValue = "0") final int page,
      @RequestParam(value = "messagesPage", defaultValue = "0") final int messagesPage,
      @RequestParam(value = "navTab", required = false, defaultValue = "CLAIM_DETAILS")
          final ViewSubmissionNavigationTab navigationTab) {

    model.addAttribute(CLAIM_ID, claimReference);
    String path =
        Boolean.TRUE.equals(featureFlagsConfig.getIsAlternativeClaimViewEnabled())
            ? "/view-claim-detail"
            : "/view-claim-detail-old";
    String uri =
        UriComponentsBuilder.fromPath(path)
            .queryParam("page", page)
            .queryParam("messagesPage", messagesPage)
            .queryParam("navTab", navigationTab.toString())
            .toUriString();

    return "redirect:" + uri;
  }

  @GetMapping("/view-claim-detail-old")
  public String getClaimDetailOld(
      Model model,
      @ModelAttribute(SUBMISSION_ID) final UUID submissionId,
      @ModelAttribute(CLAIM_ID) final UUID claimId,
      @RequestParam(value = "page", defaultValue = "0") final int page,
      @RequestParam(value = "messagesPage", defaultValue = "0") final int messagesPage,
      @RequestParam(value = "navTab", required = false, defaultValue = "CLAIM_DETAILS")
          final ViewSubmissionNavigationTab navigationTab) {

    model.addAttribute("page", page);
    model.addAttribute("messagesPage", messagesPage);
    model.addAttribute("navigationTab", navigationTab.toString());
    model.addAttribute(
        "viewSubmissionBackLink",
        UriComponentsBuilder.fromPath("/submission/{submissionId}")
            .queryParam("page", page)
            .queryParam("navTab", navigationTab.toString())
            .queryParam("messagesPage", messagesPage)
            .buildAndExpand(submissionId)
            .toUriString());

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
    model.addAttribute(
        "claimStatus",
        claimResponse.getStatus() == null ? null : claimResponse.getStatus().getValue());

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

    return "pages/view-claim-detail-old";
  }

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
    model.addAttribute(
        "viewSubmissionBackLink",
        UriComponentsBuilder.fromPath("/submission/{submissionId}")
            .queryParam("page", page)
            .queryParam("navTab", navigationTab.toString())
            .buildAndExpand(submissionId)
            .toUriString());

    ClaimResponseV2 claimResponse =
        dataClaimsRestClientV2
            .getSubmissionClaim(submissionId, claimId)
            .blockOptional()
            .orElseThrow(
                () ->
                    new SubmitBulkClaimException(
                        "Claim %s does not exist for submission %s"
                            .formatted(claimId.toString(), submissionId.toString())));

    model.addAttribute("ufn", claimResponse.getUniqueFileNumber());

    DerivedClaimStatus derivedClaimStatus = claimResponse.getDerivedClaimStatus();
    boolean showCurrentCalculated =
        derivedClaimStatus == DerivedClaimStatus.AMENDED
            || derivedClaimStatus == DerivedClaimStatus.ASSESSED;
    model.addAttribute("showCurrentCalculated", showCurrentCalculated);

    AssessmentGet currentAssessment =
        showCurrentCalculated
            ? latestAssessmentResolver.resolveLatestNonVoid(claimId).orElse(null)
            : null;

    ClaimDetailView claimDetailView =
        claimDetailViewFactory.build(claimResponse, currentAssessment);
    model.addAttribute("claimDetailView", claimDetailView);

    List<ClaimHistoryEvent> historyEvents =
        dataClaimsRestClient
            .getClaimHistory(claimId, null)
            .map(ClaimHistoryResultSet::getEvents)
            .blockOptional()
            .orElseGet(List::of);
    model.addAttribute(
        "banner", claimStatusBannerBuilder.build(derivedClaimStatus, historyEvents).orElse(null));

    return claimDetailView.template();
  }
}
