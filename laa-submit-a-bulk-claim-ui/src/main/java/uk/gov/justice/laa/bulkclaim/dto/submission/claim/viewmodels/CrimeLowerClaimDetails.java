package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CrimeLowerClaimDetails extends ClaimDetails {

  private String crimeMatterTypeCode;
  private String representationOrderDate;
  private String stageReachedCode;
  private String outcomeCode;

  private ClaimFieldRow travelCosts;
  private ClaimFieldRow waitingCosts;
}
