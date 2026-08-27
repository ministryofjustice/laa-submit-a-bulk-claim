package uk.gov.justice.laa.payments.submit.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryEvent;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.DerivedClaimStatus;
import uk.gov.justice.laa.payments.submit.builder.AmendedFieldsBuilder;
import uk.gov.justice.laa.payments.submit.builder.ClaimStatusBannerBuilder;
import uk.gov.justice.laa.payments.submit.builder.LatestAssessmentResolver;
import uk.gov.justice.laa.payments.submit.client.DataClaimsRestClient;
import uk.gov.justice.laa.payments.submit.client.DataClaimsRestClientV2;
import uk.gov.justice.laa.payments.submit.util.OidcAttributeUtils;
import uk.gov.justice.laa.payments.submit.viewmodels.claimdetails.ClaimDetailPageData;
import uk.gov.justice.laa.payments.submit.viewmodels.claimdetails.ClaimDetailView;
import uk.gov.justice.laa.payments.submit.viewmodels.claimdetails.ClaimDetailViewFactory;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClaimService {

  private final DataClaimsRestClient dataClaimsRestClient;
  private final DataClaimsRestClientV2 dataClaimsRestClientV2;
  private final ClaimDetailViewFactory claimDetailViewFactory;
  private final LatestAssessmentResolver latestAssessmentResolver;
  private final ClaimStatusBannerBuilder claimStatusBannerBuilder;
  private final OidcAttributeUtils oidcAttributeUtils;

  public ClaimResponseV2 getClaimV2(UUID submissionId, UUID claimId, OidcUser user) {
    ClaimResponseV2 claim =
        dataClaimsRestClientV2
            .getSubmissionClaim(submissionId, claimId)
            .blockOptional()
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Claim %s does not exist for submission %s"
                            .formatted(claimId, submissionId)));

    oidcAttributeUtils.checkOfficeAccess(user, claim.getOfficeCode());
    return claim;
  }

  public ClaimDetailPageData getClaimDetailPageData(
      UUID submissionId, UUID claimId, OidcUser user) {
    ClaimResponseV2 claimResponse = getClaimV2(submissionId, claimId, user);

    DerivedClaimStatus derivedClaimStatus = claimResponse.getDerivedClaimStatus();
    boolean showCurrentCalculated =
        derivedClaimStatus == DerivedClaimStatus.AMENDED
            || derivedClaimStatus == DerivedClaimStatus.ASSESSED;

    AssessmentGet currentAssessment =
        showCurrentCalculated
            ? latestAssessmentResolver.resolveLatestNonVoid(claimId).orElse(null)
            : null;

    ClaimDetailView claimDetailView =
        claimDetailViewFactory.create(claimResponse, currentAssessment);

    List<ClaimHistoryEvent> historyEvents =
        dataClaimsRestClient
            .getClaimHistory(claimId)
            .map(ClaimHistoryResultSet::getEvents)
            .blockOptional()
            .orElseGet(List::of);
    return new ClaimDetailPageData(
        claimResponse.getAreaOfLaw(),
        showCurrentCalculated,
        claimDetailView,
        claimStatusBannerBuilder.build(derivedClaimStatus, historyEvents).orElse(null),
        AmendedFieldsBuilder.build(historyEvents));
  }
}
