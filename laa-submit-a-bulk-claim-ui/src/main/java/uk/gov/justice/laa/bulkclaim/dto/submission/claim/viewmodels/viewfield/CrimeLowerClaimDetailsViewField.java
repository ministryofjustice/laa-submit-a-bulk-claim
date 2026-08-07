package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield;

import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;

@Getter
public enum CrimeLowerClaimDetailsViewField implements ClaimViewField<CrimeLowerClaimDetails> {

  // Page header / Summary
  CLIENT_FORENAME(CrimeLowerClaimDetails::clientForename),
  CLIENT_SURNAME(CrimeLowerClaimDetails::clientSurname),
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
  FIXED_FEE(d -> new ClaimFieldRow(null, d.initialCalculatedFixedFee())),
  PROFIT_COSTS(d -> new ClaimFieldRow(d.reportedProfitCosts(), d.initialCalculatedProfitCosts())),
  DISBURSEMENTS(CrimeLowerClaimDetailsViewField::disbursementsRow),
  DISBURSEMENTS_VAT(CrimeLowerClaimDetailsViewField::disbursementsVatRow),
  TRAVEL_COSTS(d -> new ClaimFieldRow(d.reportedTravelCosts(), d.initialCalculatedTravelCosts())),
  WAITING_COSTS(CrimeLowerClaimDetailsViewField::waitingCostsRow),
  VAT(d -> new ClaimFieldRow(d.reportedVatApplicable(), d.initialCalculatedVatIndicator())),

  // Total allowed value
  TOTAL_VAT(d -> new ClaimFieldRow(null, d.initialCalculatedTotalVat())),
  TOTAL_INCLUDING_VAT(d -> new ClaimFieldRow(null, d.initialCalculatedTotalIncludingVat()));

  public static final List<CrimeLowerClaimDetailsViewField> VALUE_ROWS =
      List.of(
          FIXED_FEE,
          PROFIT_COSTS,
          DISBURSEMENTS,
          DISBURSEMENTS_VAT,
          TRAVEL_COSTS,
          WAITING_COSTS,
          VAT);

  public static final List<CrimeLowerClaimDetailsViewField> TOTAL_ROWS =
      List.of(TOTAL_VAT, TOTAL_INCLUDING_VAT);

  private final Function<CrimeLowerClaimDetails, Object> accessor;

  CrimeLowerClaimDetailsViewField(Function<CrimeLowerClaimDetails, Object> accessor) {
    this.accessor = accessor;
  }

  private static ClaimFieldRow disbursementsRow(CrimeLowerClaimDetails d) {
    return new ClaimFieldRow(d.reportedDisbursements(), d.initialCalculatedDisbursements());
  }

  private static ClaimFieldRow disbursementsVatRow(CrimeLowerClaimDetails d) {
    return new ClaimFieldRow(d.reportedDisbursementsVat(), d.initialCalculatedDisbursementsVat());
  }

  private static ClaimFieldRow waitingCostsRow(CrimeLowerClaimDetails d) {
    return new ClaimFieldRow(d.reportedWaitingCosts(), d.initialCalculatedWaitingCosts());
  }
}
