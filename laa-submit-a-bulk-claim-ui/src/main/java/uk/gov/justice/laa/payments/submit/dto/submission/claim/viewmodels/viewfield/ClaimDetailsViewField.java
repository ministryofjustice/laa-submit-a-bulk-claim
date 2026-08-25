package uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield;

import java.util.function.Function;
import lombok.Getter;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.ClaimDetails;

@Getter
public enum ClaimDetailsViewField implements ClaimViewField<ClaimDetails> {

  // Page header / Summary
  CLIENT_NAME(ClaimDetails::clientName),
  UNIQUE_FILE_NUMBER(ClaimDetails::getUniqueFileNumber),
  OFFICE_ACCOUNT_NUMBER(ClaimDetails::getOfficeCode),
  DATE_SUBMITTED(ClaimDetails::getDateSubmitted),
  AREA_OF_LAW(ClaimDetails::getAreaOfLaw),
  FEE_CODE(ClaimDetails::getFeeCode),
  FEE_CODE_DESCRIPTION(ClaimDetails::getFeeCodeDescription),
  MATTER_TYPE(ClaimDetails::getMatterTypeCode),
  CASE_START_DATE(ClaimDetails::getCaseStartDate),
  DATE_OF_WORK_CONCLUDED(ClaimDetails::getCaseConcludedDate),
  ESCAPE_CASE(ClaimDetails::getEscapeCase),

  // Values
  FIXED_FEE(ClaimDetails::getFixedFee),
  PROFIT_COSTS(ClaimDetails::getProfitCosts),
  DISBURSEMENTS(ClaimDetails::getDisbursements),
  DISBURSEMENTS_VAT(ClaimDetails::getDisbursementsVat),
  VAT(ClaimDetails::getVat),

  // Total allowed value
  TOTAL_VAT(ClaimDetails::getTotalVat),
  TOTAL_INCLUDING_VAT(ClaimDetails::getTotalIncludingVat);

  private final Function<ClaimDetails, Object> accessor;

  ClaimDetailsViewField(Function<ClaimDetails, Object> accessor) {
    this.accessor = accessor;
  }
}
