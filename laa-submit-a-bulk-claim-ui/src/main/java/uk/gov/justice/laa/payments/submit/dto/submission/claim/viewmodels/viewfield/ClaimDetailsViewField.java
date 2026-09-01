package uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield;

import java.util.Set;
import java.util.function.Function;
import lombok.Getter;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.ClaimDetails;

@Getter
public enum ClaimDetailsViewField implements ClaimViewField<ClaimDetails> {

  // Page header / Summary
  CLIENT_NAME(ClaimDetails::clientName, "client.clientForename", "client.clientSurname"),
  UNIQUE_FILE_NUMBER(ClaimDetails::getUniqueFileNumber, "claim.uniqueFileNumber"),
  OFFICE_ACCOUNT_NUMBER(ClaimDetails::getOfficeCode),
  DATE_SUBMITTED(ClaimDetails::getDateSubmitted),
  AREA_OF_LAW(ClaimDetails::getAreaOfLaw),
  FEE_CODE(ClaimDetails::getFeeCode, "claim.feeCode"),
  FEE_CODE_DESCRIPTION(ClaimDetails::getFeeCodeDescription, "fee.feeCodeDescription"),
  MATTER_TYPE(ClaimDetails::getMatterTypeCode, "claim.matterTypeCode"),
  CASE_START_DATE(ClaimDetails::getCaseStartDate, "claim.caseStartDate"),
  DATE_OF_WORK_CONCLUDED(ClaimDetails::getCaseConcludedDate, "claim.caseConcludedDate"),
  ESCAPE_CASE(ClaimDetails::getEscapeCase, "fee.escapeCaseFlag"),

  // Values
  FIXED_FEE(ClaimDetails::getFixedFee, "fee.fixedFeeAmount"),
  PROFIT_COSTS(
      ClaimDetails::getProfitCosts,
      "claimSummaryFee.netProfitCostsAmount",
      "fee.netProfitCostsAmount"),
  DISBURSEMENTS(
      ClaimDetails::getDisbursements,
      "claimSummaryFee.netDisbursementAmount",
      "fee.disbursementAmount"),
  DISBURSEMENTS_VAT(
      ClaimDetails::getDisbursementsVat,
      "claimSummaryFee.disbursementsVatAmount",
      "fee.disbursementVatAmount"),
  VAT(ClaimDetails::getVat, "claimSummaryFee.isVatApplicable", "fee.vatIndicator"),

  // Total allowed value
  TOTAL_VAT(ClaimDetails::getTotalVat, "fee.calculatedVatAmount"),
  TOTAL_INCLUDING_VAT(ClaimDetails::getTotalIncludingVat, "fee.totalAmount");

  private final Function<ClaimDetails, Object> accessor;
  private final Set<String> claimsApiFieldNames;

  ClaimDetailsViewField(Function<ClaimDetails, Object> accessor, String... claimsApiFieldNames) {
    this.accessor = accessor;
    this.claimsApiFieldNames = Set.of(claimsApiFieldNames);
  }
}
