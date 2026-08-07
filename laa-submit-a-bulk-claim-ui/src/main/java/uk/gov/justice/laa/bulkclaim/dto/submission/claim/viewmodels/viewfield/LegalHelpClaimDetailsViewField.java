package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield;

import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.LegalHelpClaimDetails;

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
  FIXED_FEE(d -> new ClaimFieldRow(null, d.initialCalculatedFixedFee())),
  PROFIT_COSTS(d -> new ClaimFieldRow(d.reportedProfitCosts(), d.initialCalculatedProfitCosts())),
  DISBURSEMENTS(LegalHelpClaimDetailsViewField::disbursementsRow),
  DISBURSEMENTS_VAT(LegalHelpClaimDetailsViewField::disbursementsVatRow),
  COUNSELS_COSTS(d -> new ClaimFieldRow(null, d.initialCalculatedCounselsCosts())),
  TRAVEL_AND_WAITING_COSTS(LegalHelpClaimDetailsViewField::travelAndWaitingCostsRow),
  DETENTION_TRAVEL_WAITING_COSTS(LegalHelpClaimDetailsViewField::detentionTravelWaitingCostsRow),
  JR_FORM_FILLING(d -> new ClaimFieldRow(null, d.initialCalculatedJrFormFilling())),
  ADJOURNED_HEARING_FEE(d -> new ClaimFieldRow(null, d.initialCalculatedAdjournedHearingFee())),
  CMRH_ORAL(d -> new ClaimFieldRow(null, d.initialCalculatedCmrhOral())),
  CMRH_TELEPHONE(d -> new ClaimFieldRow(null, d.initialCalculatedCmrhTelephone())),
  HOME_OFFICE_INTERVIEW(d -> new ClaimFieldRow(null, d.initialCalculatedHomeOfficeInterview())),
  SUBSTANTIVE_HEARING(d -> new ClaimFieldRow(null, d.initialCalculatedSubstantiveHearing())),
  VAT_INDICATOR(LegalHelpClaimDetailsViewField::vatIndicatorRow),

  // Total allowed value
  TOTAL_VAT(d -> new ClaimFieldRow(null, d.initialCalculatedTotalVat())),
  TOTAL_INCLUDING_VAT(d -> new ClaimFieldRow(null, d.initialCalculatedTotalIncludingVat()));

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
          HOME_OFFICE_INTERVIEW,
          SUBSTANTIVE_HEARING,
          VAT_INDICATOR);

  public static final List<LegalHelpClaimDetailsViewField> TOTAL_ROWS =
      List.of(TOTAL_VAT, TOTAL_INCLUDING_VAT);

  private final Function<LegalHelpClaimDetails, Object> accessor;

  LegalHelpClaimDetailsViewField(Function<LegalHelpClaimDetails, Object> accessor) {
    this.accessor = accessor;
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
}
