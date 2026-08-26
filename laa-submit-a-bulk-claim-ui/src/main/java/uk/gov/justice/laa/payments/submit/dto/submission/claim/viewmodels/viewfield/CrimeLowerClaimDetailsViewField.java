package uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield;

import java.util.function.Function;
import lombok.Getter;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;

@Getter
public enum CrimeLowerClaimDetailsViewField implements ClaimViewField<CrimeLowerClaimDetails> {

  // Page header / Summary
  MATTER_TYPE(CrimeLowerClaimDetails::getCrimeMatterTypeCode),
  REPRESENTATION_ORDER_DATE(CrimeLowerClaimDetails::getRepresentationOrderDate),
  STAGE_REACHED(CrimeLowerClaimDetails::getStageReachedCode),
  OUTCOME_CODE(CrimeLowerClaimDetails::getOutcomeCode),

  // Values
  TRAVEL_COSTS(CrimeLowerClaimDetails::getTravelCosts),
  WAITING_COSTS(CrimeLowerClaimDetails::getWaitingCosts);

  private final Function<CrimeLowerClaimDetails, Object> accessor;

  CrimeLowerClaimDetailsViewField(Function<CrimeLowerClaimDetails, Object> accessor) {
    this.accessor = accessor;
  }
}
