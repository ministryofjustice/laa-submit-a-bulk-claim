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

  private ClaimField counselsCosts;
  private ClaimField travelAndWaitingCosts;
  private ClaimField detentionTravelWaitingCosts;
  private ClaimField jrFormFilling;
  private ClaimField adjournedHearingFee;
  private ClaimField cmrhOral;
  private ClaimField cmrhTelephone;
  private ClaimField homeOfficeInterview;
  private ClaimField substantiveHearing;
}
