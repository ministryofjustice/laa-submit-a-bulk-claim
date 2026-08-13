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
  CLIENT_FORENAME(LegalHelpClaimDetails::clientForename),
  CLIENT_SURNAME(LegalHelpClaimDetails::clientSurname),
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
      d -> new ClaimReportedAndCalculatedValues(null, d.initialCalculatedFixedFee()),
      AssessmentGet::getFixedFeeAmount),
  PROFIT_COSTS(
      d ->
          new ClaimReportedAndCalculatedValues(
              d.reportedProfitCosts(), d.initialCalculatedProfitCosts()),
      AssessmentGet::getNetProfitCostsAmount),
  DISBURSEMENTS(
      LegalHelpClaimDetailsViewField::disbursementsRow, AssessmentGet::getDisbursementAmount),
  DISBURSEMENTS_VAT(
      LegalHelpClaimDetailsViewField::disbursementsVatRow, AssessmentGet::getDisbursementVatAmount),
  COUNSELS_COSTS(
      d -> new ClaimReportedAndCalculatedValues(null, d.initialCalculatedCounselsCosts()),
      AssessmentGet::getNetCostOfCounselAmount),
  TRAVEL_AND_WAITING_COSTS(
      LegalHelpClaimDetailsViewField::travelAndWaitingCostsRow,
      LegalHelpClaimDetailsViewField::travelAndWaitingCostsFromAssessment),
  DETENTION_TRAVEL_WAITING_COSTS(
      LegalHelpClaimDetailsViewField::detentionTravelWaitingCostsRow,
      AssessmentGet::getDetentionTravelAndWaitingCostsAmount),
  JR_FORM_FILLING(
      d -> new ClaimReportedAndCalculatedValues(null, d.initialCalculatedJrFormFilling()),
      AssessmentGet::getJrFormFillingAmount),
  ADJOURNED_HEARING_FEE(
      d -> new ClaimReportedAndCalculatedValues(null, d.initialCalculatedAdjournedHearingFee()),
      AssessmentGet::getBoltOnAdjournedHearingFee),
  CMRH_ORAL(
      d -> new ClaimReportedAndCalculatedValues(null, d.initialCalculatedCmrhOral()),
      AssessmentGet::getBoltOnCmrhOralFee),
  CMRH_TELEPHONE(
      d -> new ClaimReportedAndCalculatedValues(null, d.initialCalculatedCmrhTelephone()),
      AssessmentGet::getBoltOnCmrhTelephoneFee),
  LONDON_RATE(d -> new ClaimReportedAndCalculatedValues(null, d.initialLondonRateIndicator())),
  HOME_OFFICE_INTERVIEW(
      d -> new ClaimReportedAndCalculatedValues(null, d.initialCalculatedHomeOfficeInterview()),
      AssessmentGet::getBoltOnHomeOfficeInterviewFee),
  SUBSTANTIVE_HEARING(
      d -> new ClaimReportedAndCalculatedValues(null, d.initialCalculatedSubstantiveHearing()),
      AssessmentGet::getBoltOnSubstantiveHearingFee),
  VAT_INDICATOR(LegalHelpClaimDetailsViewField::vatIndicatorRow, AssessmentGet::getIsVatApplicable),

  // Total allowed value
  TOTAL_VAT(
      d -> new ClaimReportedAndCalculatedValues(null, d.initialCalculatedTotalVat()),
      AssessmentGet::getAllowedTotalVat),
  TOTAL_INCLUDING_VAT(
      d -> new ClaimReportedAndCalculatedValues(null, d.initialCalculatedTotalIncludingVat()),
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

  private static ClaimReportedAndCalculatedValues disbursementsRow(LegalHelpClaimDetails d) {
    return new ClaimReportedAndCalculatedValues(
        d.reportedDisbursements(), d.initialCalculatedDisbursements());
  }

  private static ClaimReportedAndCalculatedValues disbursementsVatRow(LegalHelpClaimDetails d) {
    return new ClaimReportedAndCalculatedValues(
        d.reportedDisbursementsVat(), d.initialCalculatedDisbursementsVat());
  }

  private static ClaimReportedAndCalculatedValues travelAndWaitingCostsRow(
      LegalHelpClaimDetails d) {
    return new ClaimReportedAndCalculatedValues(
        d.reportedTravelAndWaitingCosts(), d.initialCalculatedTravelAndWaitingCosts());
  }

  private static ClaimReportedAndCalculatedValues detentionTravelWaitingCostsRow(
      LegalHelpClaimDetails d) {
    return new ClaimReportedAndCalculatedValues(
        null, d.initialCalculatedDetentionTravelWaitingCosts());
  }

  private static ClaimReportedAndCalculatedValues vatIndicatorRow(LegalHelpClaimDetails d) {
    return new ClaimReportedAndCalculatedValues(
        d.reportedVatApplicable(), d.initialCalculatedVatIndicator());
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
