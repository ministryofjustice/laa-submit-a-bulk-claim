package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield;

import java.util.List;
import java.util.function.Function;
import lombok.Getter;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.MediationClaimDetails;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;

@Getter
public enum MediationClaimDetailsViewField implements ClaimViewField<MediationClaimDetails> {

  // Page header / Summary
  CLIENT_1_FORENAME(MediationClaimDetails::client1Forename),
  CLIENT_1_SURNAME(MediationClaimDetails::client1Surname),
  CLIENT_1_UCN(MediationClaimDetails::client1UniqueClientNumber),
  CLIENT_2_FORENAME(MediationClaimDetails::client2Forename),
  CLIENT_2_SURNAME(MediationClaimDetails::client2Surname),
  CLIENT_2_UCN(MediationClaimDetails::client2UniqueClientNumber),
  FEE_CODE(MediationClaimDetails::feeCode),
  OFFICE_ACCOUNT_NUMBER(MediationClaimDetails::officeCode),
  DATE_SUBMITTED(MediationClaimDetails::dateSubmitted),
  AREA_OF_LAW(MediationClaimDetails::areaOfLaw),

  // Values
  FIXED_FEE(
      d -> new ClaimFieldRow(null, d.initialCalculatedFixedFee()),
      AssessmentGet::getFixedFeeAmount),
  DISBURSEMENTS(
      MediationClaimDetailsViewField::disbursementsRow, AssessmentGet::getDisbursementAmount),
  DISBURSEMENTS_VAT(
      MediationClaimDetailsViewField::disbursementsVatRow, AssessmentGet::getDisbursementVatAmount),
  VAT_INDICATOR(MediationClaimDetailsViewField::vatIndicatorRow, AssessmentGet::getIsVatApplicable),

  // Total allowed value
  TOTAL_VAT(
      d -> new ClaimFieldRow(null, d.initialCalculatedTotalVat()),
      AssessmentGet::getAllowedTotalVat),
  TOTAL_INCLUDING_VAT(
      d -> new ClaimFieldRow(null, d.initialCalculatedTotalIncludingVat()),
      AssessmentGet::getAllowedTotalInclVat);

  public static final List<MediationClaimDetailsViewField> VALUE_ROWS =
      List.of(FIXED_FEE, DISBURSEMENTS, DISBURSEMENTS_VAT, VAT_INDICATOR);

  public static final List<MediationClaimDetailsViewField> TOTAL_ROWS =
      List.of(TOTAL_VAT, TOTAL_INCLUDING_VAT);

  private final Function<MediationClaimDetails, Object> accessor;
  private final Function<AssessmentGet, Object> assessmentAccessor;

  MediationClaimDetailsViewField(Function<MediationClaimDetails, Object> accessor) {
    this(accessor, null);
  }

  MediationClaimDetailsViewField(
      Function<MediationClaimDetails, Object> accessor,
      Function<AssessmentGet, Object> assessmentAccessor) {
    this.accessor = accessor;
    this.assessmentAccessor = assessmentAccessor;
  }

  private static ClaimFieldRow disbursementsRow(MediationClaimDetails d) {
    return new ClaimFieldRow(d.reportedDisbursements(), d.initialCalculatedDisbursements());
  }

  private static ClaimFieldRow disbursementsVatRow(MediationClaimDetails d) {
    return new ClaimFieldRow(d.reportedDisbursementsVat(), d.initialCalculatedDisbursementsVat());
  }

  private static ClaimFieldRow vatIndicatorRow(MediationClaimDetails d) {
    return new ClaimFieldRow(d.reportedVatApplicable(), d.initialCalculatedVatIndicator());
  }
}
