package uk.gov.justice.laa.bulkclaim.viewmodels.claimcase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimDetailsMapper;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;

@Component
@RequiredArgsConstructor
public class ClaimDetailViewFactory {

  private final ClaimDetailsMapper claimDetailsMapper;

  public ClaimDetailView<?> create(ClaimResponseV2 claimResponse, AssessmentGet currentAssessment) {
    return switch (claimResponse.getAreaOfLaw()) {
      case CRIME_LOWER -> {
        var details = claimDetailsMapper.toCrimeLowerClaimDetails(claimResponse, currentAssessment);
        yield new CrimeClaimCaseView(details, currentAssessment);
      }
      case LEGAL_HELP -> {
        var details = claimDetailsMapper.toLegalHelpClaimDetails(claimResponse, currentAssessment);
        yield new LegalHelpClaimCaseView(details, currentAssessment);
      }
      case MEDIATION -> {
        var details = claimDetailsMapper.toMediationClaimDetails(claimResponse, currentAssessment);
        yield new MediationClaimCaseView(details, currentAssessment);
      }
    };
  }
}
