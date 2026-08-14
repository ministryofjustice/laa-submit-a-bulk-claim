package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield;

import java.util.function.Function;
import lombok.Getter;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.MediationClaimDetails;

@Getter
public enum MediationClaimDetailsViewField implements ClaimViewField<MediationClaimDetails> {

  // Page header / Summary
  CLIENT_1_NAME(MediationClaimDetails::clientName),
  CLIENT_1_UCN(MediationClaimDetails::getUniqueClientNumber),
  CLIENT_2_NAME(MediationClaimDetails::client2Name),
  CLIENT_2_UCN(MediationClaimDetails::getClient2UniqueClientNumber);

  private final Function<MediationClaimDetails, Object> accessor;

  MediationClaimDetailsViewField(Function<MediationClaimDetails, Object> accessor) {
    this.accessor = accessor;
  }
}
