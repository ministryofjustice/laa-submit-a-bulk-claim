package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield;

import java.math.BigDecimal;
import java.util.function.Function;
import lombok.Getter;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimReportedAndCalculatedValues;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.LegalHelpClaimDetails;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;

@Getter
public enum LegalHelpClaimDetailsViewField implements ClaimViewField<LegalHelpClaimDetails> {

  // Page header / Summary
  CLIENT_NAME(LegalHelpClaimDetails::clientName),
  UNIQUE_FILE_NUMBER(LegalHelpClaimDetails::uniqueFileNumber),
  OFFICE_ACCOUNT_NUMBER(LegalHelpClaimDetails::officeCode),
  DATE_SUBMITTED(LegalHelpClaimDetails::dateSubmitted),
  AREA_OF_LAW(LegalHelpClaimDetails::areaOfLaw),
  CATEGORY_OF_LAW(LegalHelpClaimDetails::categoryOfLaw),
  FEE_CODE(LegalHelpClaimDetails::feeCode),
  FEE_CODE_DESCRIPTION(LegalHelpClaimDetails::feeCodeDescription),
  MATTER_TYPE_1(LegalHelpClaimDetails::matterTypeCodeOne),
  MATTER_TYPE_2(LegalHelpClaimDetails::matterTypeCodeTwo),
  CASE_START_DATE(LegalHelpClaimDetails::caseStartDate),
  DATE_OF_WORK_CONCLUDED(LegalHelpClaimDetails::caseConcludedDate),
  ESCAPE_CASE(LegalHelpClaimDetails::escapeCase),

  // Values
  FIXED_FEE(
      claim -> new ClaimReportedAndCalculatedValues(claim.initialCalculatedFixedFee()),
      AssessmentGet::getFixedFeeAmount),
  // Profit costs doesn't show an initial calculated value, so null here is intentional.
  PROFIT_COSTS(
      claim -> new ClaimReportedAndCalculatedValues(claim.reportedProfitCosts(), null),
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
  COUNSELS_COSTS(
      claim -> new ClaimReportedAndCalculatedValues(claim.initialCalculatedCounselsCosts()),
      AssessmentGet::getNetCostOfCounselAmount),
  TRAVEL_AND_WAITING_COSTS(
      claim ->
          new ClaimReportedAndCalculatedValues(
              claim.reportedTravelAndWaitingCosts(),
              claim.initialCalculatedTravelAndWaitingCosts()),
      LegalHelpClaimDetailsViewField::travelAndWaitingCostsFromAssessment),
  DETENTION_TRAVEL_WAITING_COSTS(
      claim ->
          new ClaimReportedAndCalculatedValues(
              claim.initialCalculatedDetentionTravelWaitingCosts()),
      AssessmentGet::getDetentionTravelAndWaitingCostsAmount),
  JR_FORM_FILLING(
      claim -> new ClaimReportedAndCalculatedValues(claim.initialCalculatedJrFormFilling()),
      AssessmentGet::getJrFormFillingAmount),
  ADJOURNED_HEARING_FEE(
      claim -> new ClaimReportedAndCalculatedValues(claim.initialCalculatedAdjournedHearingFee()),
      AssessmentGet::getBoltOnAdjournedHearingFee),
  CMRH_ORAL(
      claim -> new ClaimReportedAndCalculatedValues(claim.initialCalculatedCmrhOral()),
      AssessmentGet::getBoltOnCmrhOralFee),
  CMRH_TELEPHONE(
      claim -> new ClaimReportedAndCalculatedValues(claim.initialCalculatedCmrhTelephone()),
      AssessmentGet::getBoltOnCmrhTelephoneFee),
  // London rate not stored from fee calculation, reusing users entered value as it cannot be
  // modified anyways.
  LONDON_RATE(claim -> new ClaimReportedAndCalculatedValues(claim.reportedLondonRateIndicator())),
  HOME_OFFICE_INTERVIEW(
      claim -> new ClaimReportedAndCalculatedValues(claim.initialCalculatedHomeOfficeInterview()),
      AssessmentGet::getBoltOnHomeOfficeInterviewFee),
  SUBSTANTIVE_HEARING(
      claim -> new ClaimReportedAndCalculatedValues(claim.initialCalculatedSubstantiveHearing()),
      AssessmentGet::getBoltOnSubstantiveHearingFee),
  VAT_INDICATOR(
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

  private final Function<LegalHelpClaimDetails, Object> reportedAndCalculatedAccessor;
  private final Function<AssessmentGet, Object> assessedAccessor;

  LegalHelpClaimDetailsViewField(
      Function<LegalHelpClaimDetails, Object> reportedAndCalculatedAccessor) {
    this(reportedAndCalculatedAccessor, null);
  }

  LegalHelpClaimDetailsViewField(
      Function<LegalHelpClaimDetails, Object> reportedAndCalculatedAccessor,
      Function<AssessmentGet, Object> latestAssessedAccessor) {
    this.reportedAndCalculatedAccessor = reportedAndCalculatedAccessor;
    this.assessedAccessor = latestAssessedAccessor;
  }

  @Override
  public Function<AssessmentGet, Object> getCurrentCalculatedAccessor() {
    return assessedAccessor;
  }

  /**
   * AssessmentGet has no combined travel-and-waiting field (unlike FeeCalculationPatch) - sum its
   * separate net_travel_costs_amount and net_waiting_costs_amount to match this row's shape.
   */
  private static Object travelAndWaitingCostsFromAssessment(AssessmentGet assessment) {
    BigDecimal travel = assessment.getNetTravelCostsAmount();
    BigDecimal waiting = assessment.getNetWaitingCostsAmount();
    if (travel == null && waiting == null) {
      return null;
    }
    return (travel == null ? BigDecimal.ZERO : travel)
        .add(waiting == null ? BigDecimal.ZERO : waiting);
  }
}
