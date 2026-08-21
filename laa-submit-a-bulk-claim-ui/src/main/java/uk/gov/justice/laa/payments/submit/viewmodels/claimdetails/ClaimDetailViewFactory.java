package uk.gov.justice.laa.payments.submit.viewmodels.claimdetails;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;
import uk.gov.justice.laa.payments.submit.mapper.ClaimDetailsMapper;

@Component
@RequiredArgsConstructor
public class ClaimDetailViewFactory {

  private final ClaimDetailsMapper claimDetailsMapper;

  public ClaimDetailView<?> create(ClaimResponseV2 claimResponse, AssessmentGet currentAssessment) {
    return switch (claimResponse.getAreaOfLaw()) {
      case CRIME_LOWER -> {
        var details = claimDetailsMapper.toCrimeLowerClaimDetails(claimResponse, currentAssessment);
        yield new CrimeClaimDetailsView(details);
      }
      case LEGAL_HELP -> {
        var details = claimDetailsMapper.toLegalHelpClaimDetails(claimResponse, currentAssessment);
        yield new LegalHelpClaimDetailsView(details);
      }
      case MEDIATION -> {
        var details = claimDetailsMapper.toMediationClaimDetails(claimResponse, currentAssessment);
        yield new MediationClaimDetailsView(details);
      }
    };
  }
}
