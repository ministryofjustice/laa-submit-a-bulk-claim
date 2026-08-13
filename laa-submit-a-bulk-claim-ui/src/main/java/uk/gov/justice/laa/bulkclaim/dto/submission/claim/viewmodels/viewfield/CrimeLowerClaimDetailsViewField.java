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
      claim -> new ClaimReportedAndCalculatedValues(null, claim.initialCalculatedFixedFee()),
      AssessmentGet::getFixedFeeAmount),
  PROFIT_COSTS(
      claim ->
          new ClaimReportedAndCalculatedValues(
              claim.reportedProfitCosts(), claim.initialCalculatedProfitCosts()),
      AssessmentGet::getNetProfitCostsAmount),
  DISBURSEMENTS(
      claim ->
          new ClaimReportedAndCalculatedValues(
              claim.reportedDisbursements(), claim.initialCalculatedDisbursements()),
      AssessmentGet::getDisbursementAmount),
  DISBURSEMENTS_VAT(
      claim ->
          new ClaimReportedAndCalculatedValues(
              claim.reportedDisbursementsVat(), claim.initialCalculatedDisbursementsVat()),
      AssessmentGet::getDisbursementVatAmount),
  TRAVEL_COSTS(
      claim ->
          new ClaimReportedAndCalculatedValues(
              claim.reportedTravelCosts(), claim.initialCalculatedTravelCosts()),
      AssessmentGet::getNetTravelCostsAmount),
  WAITING_COSTS(
      claim ->
          new ClaimReportedAndCalculatedValues(
              claim.reportedWaitingCosts(), claim.initialCalculatedWaitingCosts()),
      AssessmentGet::getNetWaitingCostsAmount),
  VAT(
      claim ->
          new ClaimReportedAndCalculatedValues(
              claim.reportedVatApplicable(), claim.initialCalculatedVatIndicator()),
      AssessmentGet::getIsVatApplicable),

  // Total allowed value
  TOTAL_VAT(
      claim -> new ClaimReportedAndCalculatedValues(claim.initialCalculatedTotalVat()),
      AssessmentGet::getAllowedTotalVat),
  TOTAL_INCLUDING_VAT(
      claim -> new ClaimReportedAndCalculatedValues(claim.initialCalculatedTotalIncludingVat()),
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
