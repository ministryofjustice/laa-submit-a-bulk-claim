package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LegalHelpClaimDetails extends ClaimDetails {

  private String categoryOfLaw;
  private String matterTypeCodeOne;
  private String matterTypeCodeTwo;
  private Boolean reportedLondonRateIndicator;

  private ClaimFieldRow counselsCosts;
  private ClaimFieldRow travelAndWaitingCosts;
  private ClaimFieldRow detentionTravelWaitingCosts;
  private ClaimFieldRow jrFormFilling;
  private ClaimFieldRow adjournedHearingFee;
  private ClaimFieldRow cmrhOral;
  private ClaimFieldRow cmrhTelephone;
  private ClaimFieldRow homeOfficeInterview;
  private ClaimFieldRow substantiveHearing;
}
