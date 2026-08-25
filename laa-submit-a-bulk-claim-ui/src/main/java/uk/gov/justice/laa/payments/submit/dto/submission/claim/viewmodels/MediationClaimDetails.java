package uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MediationClaimDetails extends ClaimDetails {

  private String uniqueClientNumber;
  private String client2Forename;
  private String client2Surname;
  private String client2UniqueClientNumber;

  public String client2Name() {
    return client2Forename == null && client2Surname == null
        ? null
        : client2Forename + " " + client2Surname;
  }
}
