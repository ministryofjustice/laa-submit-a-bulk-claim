package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield;

import java.util.function.Function;
import lombok.Getter;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimReportedAndCalculatedValues;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;

@Getter
public enum CrimeLowerClaimDetailsViewField implements ClaimViewField<CrimeLowerClaimDetails> {

  // Page header / Summary
  CLIENT_NAME(CrimeLowerClaimDetails::clientName),
  UNIQUE_FILE_NUMBER(CrimeLowerClaimDetails::uniqueFileNumber),
  OFFICE_ACCOUNT_NUMBER(CrimeLowerClaimDetails::officeCode),
  DATE_SUBMITTED(CrimeLowerClaimDetails::dateSubmitted),
  AREA_OF_LAW(CrimeLowerClaimDetails::areaOfLaw),
  FEE_CODE(CrimeLowerClaimDetails::feeCode),
  FEE_CODE_DESCRIPTION(CrimeLowerClaimDetails::feeCodeDescription),
  MATTER_TYPE(CrimeLowerClaimDetails::matterTypeCode),
  REPRESENTATION_ORDER_DATE(CrimeLowerClaimDetails::representationOrderDate),
  STAGE_REACHED(CrimeLowerClaimDetails::stageReachedCode),
  OUTCOME_CODE(CrimeLowerClaimDetails::outcomeCode),
  DATE_OF_WORK_CONCLUDED(CrimeLowerClaimDetails::caseConcludedDate),
  ESCAPE_CASE(CrimeLowerClaimDetails::escapeCase),

  // Values
  FIXED_FEE(
      d -> new ClaimReportedAndCalculatedValues(null, d.initialCalculatedFixedFee()),
      AssessmentGet::getFixedFeeAmount),
  PROFIT_COSTS(
      d ->
          new ClaimReportedAndCalculatedValues(
              d.reportedProfitCosts(), d.initialCalculatedProfitCosts()),
      AssessmentGet::getNetProfitCostsAmount),
  DISBURSEMENTS(
      d ->
          new ClaimReportedAndCalculatedValues(
              d.reportedDisbursements(), d.initialCalculatedDisbursements()),
      AssessmentGet::getDisbursementAmount),
  DISBURSEMENTS_VAT(
      d ->
          new ClaimReportedAndCalculatedValues(
              d.reportedDisbursementsVat(), d.initialCalculatedDisbursementsVat()),
      AssessmentGet::getDisbursementVatAmount),
  TRAVEL_COSTS(
      d ->
          new ClaimReportedAndCalculatedValues(
              d.reportedTravelCosts(), d.initialCalculatedTravelCosts()),
      AssessmentGet::getNetTravelCostsAmount),
  WAITING_COSTS(
      d ->
          new ClaimReportedAndCalculatedValues(
              d.reportedWaitingCosts(), d.initialCalculatedWaitingCosts()),
      AssessmentGet::getNetWaitingCostsAmount),
  VAT(
      d ->
          new ClaimReportedAndCalculatedValues(
              d.reportedVatApplicable(), d.initialCalculatedVatIndicator()),
      AssessmentGet::getIsVatApplicable),

  // Total allowed value
  TOTAL_VAT(
      d -> new ClaimReportedAndCalculatedValues(null, d.initialCalculatedTotalVat()),
      AssessmentGet::getAllowedTotalVat),
  TOTAL_INCLUDING_VAT(
      d -> new ClaimReportedAndCalculatedValues(null, d.initialCalculatedTotalIncludingVat()),
      AssessmentGet::getAllowedTotalInclVat);

  private final Function<CrimeLowerClaimDetails, Object> reportedAndCalculatedAccessor;
  private final Function<AssessmentGet, Object> assessedAccessor;

  CrimeLowerClaimDetailsViewField(Function<CrimeLowerClaimDetails, Object> accessor) {
    this(accessor, null);
  }

  CrimeLowerClaimDetailsViewField(
      Function<CrimeLowerClaimDetails, Object> reportedAndCalculatedAccessor,
      Function<AssessmentGet, Object> assessedAccessor) {
    this.reportedAndCalculatedAccessor = reportedAndCalculatedAccessor;
    this.assessedAccessor = assessedAccessor;
  }

  @Override
  public Function<AssessmentGet, Object> getCurrentCalculatedAccessor() {
    return assessedAccessor;
  }
}
