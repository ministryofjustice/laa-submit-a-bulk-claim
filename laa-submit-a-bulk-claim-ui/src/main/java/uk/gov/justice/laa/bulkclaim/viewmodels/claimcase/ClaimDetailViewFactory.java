package uk.gov.justice.laa.bulkclaim.viewmodels.claimcase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.mapper.CrimeLowerClaimDetailsMapper;
import uk.gov.justice.laa.bulkclaim.mapper.LegalHelpClaimDetailsMapper;
import uk.gov.justice.laa.bulkclaim.mapper.MediationClaimDetailsMapper;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;

@Component
@RequiredArgsConstructor
public class ClaimDetailViewFactory {

  private final CrimeLowerClaimDetailsMapper crimeLowerClaimDetailsMapper;
  private final LegalHelpClaimDetailsMapper legalHelpClaimDetailsMapper;
  private final MediationClaimDetailsMapper mediationClaimDetailsMapper;

  public ClaimDetailView create(ClaimResponseV2 claimResponse, AssessmentGet currentAssessment) {
    return switch (claimResponse.getAreaOfLaw()) {
      case CRIME_LOWER -> {
        var details = crimeLowerClaimDetailsMapper.toCrimeLowerClaimDetails(claimResponse);
        yield new CrimeClaimCaseView(details, currentAssessment);
      }
      case LEGAL_HELP -> {
        var details = legalHelpClaimDetailsMapper.toLegalHelpClaimDetails(claimResponse);
        yield new LegalHelpClaimCaseView(details, currentAssessment);
      }
      case MEDIATION -> {
        var details = mediationClaimDetailsMapper.toMediationClaimDetails(claimResponse);
        yield new MediationClaimCaseView(details, currentAssessment);
      }
    };
  }
}
