package uk.gov.justice.laa.bulkclaim.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.builder.ClaimStatusBannerBuilder;
import uk.gov.justice.laa.bulkclaim.builder.LatestAssessmentResolver;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClientV2;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.bulkclaim.service.claimdetail.ClaimDetailPageData;
import uk.gov.justice.laa.bulkclaim.service.claimdetail.ClaimDetailView;
import uk.gov.justice.laa.bulkclaim.service.claimdetail.ClaimDetailViewFactory;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryEvent;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.DerivedClaimStatus;

@Component
@RequiredArgsConstructor
public class ClaimService {

  private final DataClaimsRestClient dataClaimsRestClient;
  private final DataClaimsRestClientV2 dataClaimsRestClientV2;
  private final ClaimDetailViewFactory claimDetailViewFactory;
  private final LatestAssessmentResolver latestAssessmentResolver;

  public ClaimDetailPageData getClaimDetailPageData(UUID submissionId, UUID claimId) {

    ClaimResponseV2 claimResponse =
        dataClaimsRestClientV2
            .getSubmissionClaim(submissionId, claimId)
            .blockOptional()
            .orElseThrow(
                () ->
                    new SubmitBulkClaimException(
                        "Claim %s does not exist for submission %s"
                            .formatted(claimId.toString(), submissionId.toString())));

    DerivedClaimStatus derivedClaimStatus = claimResponse.getDerivedClaimStatus();
    boolean showCurrentCalculated =
        derivedClaimStatus == DerivedClaimStatus.AMENDED
            || derivedClaimStatus == DerivedClaimStatus.ASSESSED;

    AssessmentGet currentAssessment =
        showCurrentCalculated
            ? latestAssessmentResolver.resolveLatestNonVoid(claimId).orElse(null)
            : null;

    ClaimDetailView claimDetailView =
        claimDetailViewFactory.build(claimResponse, currentAssessment);

    List<ClaimHistoryEvent> historyEvents =
        dataClaimsRestClient
            .getClaimHistory(claimId, null)
            .map(ClaimHistoryResultSet::getEvents)
            .blockOptional()
            .orElseGet(List::of);
    return new ClaimDetailPageData(
        claimResponse.getUniqueFileNumber(),
        showCurrentCalculated,
        claimDetailView,
        ClaimStatusBannerBuilder.build(derivedClaimStatus, historyEvents).orElse(null));
  }
}
