package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield;

import java.util.function.Function;
import lombok.Getter;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimReportedAndCalculatedValues;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.MediationClaimDetails;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;

@Getter
public enum MediationClaimDetailsViewField implements ClaimViewField<MediationClaimDetails> {

  // Page header / Summary
  CLIENT_1_FORENAME(MediationClaimDetails::client1Forename),
  CLIENT_1_SURNAME(MediationClaimDetails::client1Surname),
  CLIENT_1_NAME(MediationClaimDetails::client1Name),
  CLIENT_1_UCN(MediationClaimDetails::client1UniqueClientNumber),
  CLIENT_2_FORENAME(MediationClaimDetails::client2Forename),
  CLIENT_2_SURNAME(MediationClaimDetails::client2Surname),
  CLIENT_2_NAME(MediationClaimDetails::client2Name),
  CLIENT_2_UCN(MediationClaimDetails::client2UniqueClientNumber),
  FEE_CODE(MediationClaimDetails::feeCode),
  FEE_CODE_DESCRIPTION(MediationClaimDetails::feeCodeDescription),
  OFFICE_ACCOUNT_NUMBER(MediationClaimDetails::officeCode),
  DATE_SUBMITTED(MediationClaimDetails::dateSubmitted),
  AREA_OF_LAW(MediationClaimDetails::areaOfLaw),
  MATTER_TYPE(MediationClaimDetails::matterTypeCode),
  CASE_START_DATE(MediationClaimDetails::caseStartDate),
  DATE_OF_WORK_CONCLUDED(MediationClaimDetails::caseConcludedDate),

  // Values
  FIXED_FEE(
      d -> new ClaimReportedAndCalculatedValues(null, d.initialCalculatedFixedFee()),
      AssessmentGet::getFixedFeeAmount),
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
  VAT_INDICATOR(
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

  private final Function<MediationClaimDetails, Object> reportedAndCalculatedAccessor;
  private final Function<AssessmentGet, Object> assessedAccessor;

  MediationClaimDetailsViewField(Function<MediationClaimDetails, Object> accessor) {
    this(accessor, null);
  }

  MediationClaimDetailsViewField(
      Function<MediationClaimDetails, Object> reportedAndCalculatedAccessor,
      Function<AssessmentGet, Object> assessedAccessor) {
    this.reportedAndCalculatedAccessor = reportedAndCalculatedAccessor;
    this.assessedAccessor = assessedAccessor;
  }

  @Override
  public Function<AssessmentGet, Object> getCurrentCalculatedAccessor() {
    return assessedAccessor;
  }
}
