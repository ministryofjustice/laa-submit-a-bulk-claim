package uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield;

import java.util.Set;
import java.util.function.Function;
import lombok.Getter;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;

@Getter
public enum CrimeLowerClaimDetailsViewField implements ClaimViewField<CrimeLowerClaimDetails> {

  // Page header / Summary
  MATTER_TYPE(CrimeLowerClaimDetails::getCrimeMatterTypeCode, "claim.crimeMatterTypeCode"),
  REPRESENTATION_ORDER_DATE(
      CrimeLowerClaimDetails::getRepresentationOrderDate, "claim.representationOrderDate"),
  STAGE_REACHED(CrimeLowerClaimDetails::getStageReachedCode, "claimCase.stageReachedCode"),
  OUTCOME_CODE(CrimeLowerClaimDetails::getOutcomeCode, "claimCase.outcomeCode"),

  // Values
  TRAVEL_COSTS(
      CrimeLowerClaimDetails::getTravelCosts,
      "claimSummaryFee.travelWaitingCostsAmount",
      "fee.netTravelCostsAmount"),
  WAITING_COSTS(
      CrimeLowerClaimDetails::getWaitingCosts,
      "claimSummaryFee.netWaitingCostsAmount",
      "fee.netWaitingCostsAmount");

  private final Function<CrimeLowerClaimDetails, Object> accessor;
  private final Set<String> claimsApiFieldNames;

  CrimeLowerClaimDetailsViewField(
      Function<CrimeLowerClaimDetails, Object> accessor, String... claimsApiFieldNames) {
    this.accessor = accessor;
    this.claimsApiFieldNames = Set.of(claimsApiFieldNames);
  }
}
