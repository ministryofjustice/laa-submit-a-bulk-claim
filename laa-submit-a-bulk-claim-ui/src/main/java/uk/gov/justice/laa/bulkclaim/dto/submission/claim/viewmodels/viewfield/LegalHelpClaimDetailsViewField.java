package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.LegalHelpClaimDetails;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;

@Getter
public enum LegalHelpClaimDetailsViewField implements ClaimViewField<LegalHelpClaimDetails> {

  // Page header / Summary
  CLIENT_FORENAME(LegalHelpClaimDetails::clientForename),
  CLIENT_SURNAME(LegalHelpClaimDetails::clientSurname),
  UNIQUE_FILE_NUMBER(LegalHelpClaimDetails::uniqueFileNumber),
  OFFICE_ACCOUNT_NUMBER(LegalHelpClaimDetails::officeCode),
  DATE_SUBMITTED(LegalHelpClaimDetails::dateSubmitted),
  AREA_OF_LAW(LegalHelpClaimDetails::areaOfLaw),
  CATEGORY_OF_LAW(LegalHelpClaimDetails::categoryOfLaw),
  FEE_CODE(LegalHelpClaimDetails::feeCode),
  FEE_CODE_DESCRIPTION(LegalHelpClaimDetails::feeCodeDescription),
  MATTER_TYPE(LegalHelpClaimDetails::matterTypeCode),
  CASE_START_DATE(LegalHelpClaimDetails::caseStartDate),
  DATE_OF_WORK_CONCLUDED(LegalHelpClaimDetails::caseConcludedDate),
  ESCAPE_CASE(LegalHelpClaimDetails::escapeCase),

  // Values
  FIXED_FEE(
      d -> new ClaimFieldRow(null, d.initialCalculatedFixedFee()),
      AssessmentGet::getFixedFeeAmount),
  PROFIT_COSTS(
      d -> new ClaimFieldRow(d.reportedProfitCosts(), d.initialCalculatedProfitCosts()),
      AssessmentGet::getNetProfitCostsAmount),
  DISBURSEMENTS(
      LegalHelpClaimDetailsViewField::disbursementsRow, AssessmentGet::getDisbursementAmount),
  DISBURSEMENTS_VAT(
      LegalHelpClaimDetailsViewField::disbursementsVatRow, AssessmentGet::getDisbursementVatAmount),
  COUNSELS_COSTS(
      d -> new ClaimFieldRow(null, d.initialCalculatedCounselsCosts()),
      AssessmentGet::getNetCostOfCounselAmount),
  TRAVEL_AND_WAITING_COSTS(
      LegalHelpClaimDetailsViewField::travelAndWaitingCostsRow,
      LegalHelpClaimDetailsViewField::travelAndWaitingCostsFromAssessment),
  DETENTION_TRAVEL_WAITING_COSTS(
      LegalHelpClaimDetailsViewField::detentionTravelWaitingCostsRow,
      AssessmentGet::getDetentionTravelAndWaitingCostsAmount),
  JR_FORM_FILLING(
      d -> new ClaimFieldRow(null, d.initialCalculatedJrFormFilling()),
      AssessmentGet::getJrFormFillingAmount),
  ADJOURNED_HEARING_FEE(
      d -> new ClaimFieldRow(null, d.initialCalculatedAdjournedHearingFee()),
      AssessmentGet::getBoltOnAdjournedHearingFee),
  CMRH_ORAL(
      d -> new ClaimFieldRow(null, d.initialCalculatedCmrhOral()),
      AssessmentGet::getBoltOnCmrhOralFee),
  CMRH_TELEPHONE(
      d -> new ClaimFieldRow(null, d.initialCalculatedCmrhTelephone()),
      AssessmentGet::getBoltOnCmrhTelephoneFee),
  // No accessor exists anywhere on AssessmentGet for London rate - see the "Not applicable" note
  // on VALUE_ROWS below, the same reasoning applies to Current Calculated.
  LONDON_RATE(d -> new ClaimFieldRow(null, d.initialLondonRateIndicator())),
  HOME_OFFICE_INTERVIEW(
      d -> new ClaimFieldRow(null, d.initialCalculatedHomeOfficeInterview()),
      AssessmentGet::getBoltOnHomeOfficeInterviewFee),
  SUBSTANTIVE_HEARING(
      d -> new ClaimFieldRow(null, d.initialCalculatedSubstantiveHearing()),
      AssessmentGet::getBoltOnSubstantiveHearingFee),
  VAT_INDICATOR(LegalHelpClaimDetailsViewField::vatIndicatorRow, AssessmentGet::getIsVatApplicable),

  // Total allowed value
  TOTAL_VAT(
      d -> new ClaimFieldRow(null, d.initialCalculatedTotalVat()),
      AssessmentGet::getAllowedTotalVat),
  TOTAL_INCLUDING_VAT(
      d -> new ClaimFieldRow(null, d.initialCalculatedTotalIncludingVat()),
      AssessmentGet::getAllowedTotalInclVat);

  // "London rate" is omitted entirely - BC-523 marks it "Not applicable" for both Reported and
  // Initial calculated, and no accessor exists anywhere in ClaimResponseV2/FeeCalculationPatch/
  // BoltOnPatch to source it from. See ticket Comments.
  public static final List<LegalHelpClaimDetailsViewField> VALUE_ROWS =
      List.of(
          FIXED_FEE,
          PROFIT_COSTS,
          DISBURSEMENTS,
          DISBURSEMENTS_VAT,
          COUNSELS_COSTS,
          TRAVEL_AND_WAITING_COSTS,
          DETENTION_TRAVEL_WAITING_COSTS,
          JR_FORM_FILLING,
          ADJOURNED_HEARING_FEE,
          CMRH_ORAL,
          CMRH_TELEPHONE,
          LONDON_RATE,
          HOME_OFFICE_INTERVIEW,
          SUBSTANTIVE_HEARING,
          VAT_INDICATOR);

  public static final List<LegalHelpClaimDetailsViewField> TOTAL_ROWS =
      List.of(TOTAL_VAT, TOTAL_INCLUDING_VAT);

  private final Function<LegalHelpClaimDetails, Object> accessor;
  private final Function<AssessmentGet, Object> assessmentAccessor;

  LegalHelpClaimDetailsViewField(Function<LegalHelpClaimDetails, Object> accessor) {
    this(accessor, null);
  }

  LegalHelpClaimDetailsViewField(
      Function<LegalHelpClaimDetails, Object> accessor,
      Function<AssessmentGet, Object> assessmentAccessor) {
    this.accessor = accessor;
    this.assessmentAccessor = assessmentAccessor;
  }

  private static ClaimFieldRow disbursementsRow(LegalHelpClaimDetails d) {
    return new ClaimFieldRow(d.reportedDisbursements(), d.initialCalculatedDisbursements());
  }

  private static ClaimFieldRow disbursementsVatRow(LegalHelpClaimDetails d) {
    return new ClaimFieldRow(d.reportedDisbursementsVat(), d.initialCalculatedDisbursementsVat());
  }

  private static ClaimFieldRow travelAndWaitingCostsRow(LegalHelpClaimDetails d) {
    return new ClaimFieldRow(
        d.reportedTravelAndWaitingCosts(), d.initialCalculatedTravelAndWaitingCosts());
  }

  private static ClaimFieldRow detentionTravelWaitingCostsRow(LegalHelpClaimDetails d) {
    return new ClaimFieldRow(null, d.initialCalculatedDetentionTravelWaitingCosts());
  }

  private static ClaimFieldRow vatIndicatorRow(LegalHelpClaimDetails d) {
    return new ClaimFieldRow(d.reportedVatApplicable(), d.initialCalculatedVatIndicator());
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
