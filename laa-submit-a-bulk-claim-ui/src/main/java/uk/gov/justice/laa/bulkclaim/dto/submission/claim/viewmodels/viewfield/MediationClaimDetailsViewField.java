package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield;

import java.util.Set;
import java.util.function.Function;
import lombok.Getter;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.MediationClaimDetails;

@Getter
public enum MediationClaimDetailsViewField implements ClaimViewField<MediationClaimDetails> {

  // Page header / Summary
  CLIENT_1_NAME(MediationClaimDetails::clientName, "client.clientForename", "client.clientSurname"),
  CLIENT_1_UCN(MediationClaimDetails::getUniqueClientNumber, "client.uniqueClientNumber"),
  CLIENT_2_NAME(
      MediationClaimDetails::client2Name, "client.client2Forename", "client.client2Surname"),
  CLIENT_2_UCN(MediationClaimDetails::getClient2UniqueClientNumber, "client.client2Ucn");

  private final Function<MediationClaimDetails, Object> accessor;
  private final Set<String> claimsApiFieldNames;

  MediationClaimDetailsViewField(
      Function<MediationClaimDetails, Object> accessor, String... claimsApiFieldNames) {
    this.accessor = accessor;
    this.claimsApiFieldNames = Set.of(claimsApiFieldNames);
  }
}
