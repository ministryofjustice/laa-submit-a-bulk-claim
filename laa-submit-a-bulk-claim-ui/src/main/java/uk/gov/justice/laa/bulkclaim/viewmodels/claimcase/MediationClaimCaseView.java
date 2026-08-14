package uk.gov.justice.laa.bulkclaim.viewmodels.claimcase;

import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimViewField.asMediationField;

import java.util.LinkedHashMap;
import java.util.List;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.MediationClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimViewField;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;

public record MediationClaimCaseView(
    LinkedHashMap<ClaimViewField<MediationClaimDetails>, Object> summaryRows,
    LinkedHashMap<ClaimViewField<MediationClaimDetails>, Object> valueRows,
    LinkedHashMap<ClaimViewField<MediationClaimDetails>, Object> totalRows)
    implements ClaimDetailView<ClaimViewField<MediationClaimDetails>> {

  public MediationClaimCaseView(
      MediationClaimDetails mediationClaimDetails, AssessmentGet currentAssessment) {
    this(
        ClaimViewField.toFieldMap(SUMMARY_ROWS.stream(), mediationClaimDetails),
        ClaimViewField.toFieldMap(VALUE_ROWS.stream(), mediationClaimDetails),
        ClaimViewField.toFieldMap(TOTAL_ROWS.stream(), mediationClaimDetails));
  }

  public static final List<ClaimViewField<MediationClaimDetails>> SUMMARY_ROWS =
      List.of(
          MediationClaimDetailsViewField.CLIENT_1_NAME,
          MediationClaimDetailsViewField.CLIENT_1_UCN,
          MediationClaimDetailsViewField.CLIENT_2_NAME,
          MediationClaimDetailsViewField.CLIENT_2_UCN,
          asMediationField(ClaimDetailsViewField.OFFICE_ACCOUNT_NUMBER),
          asMediationField(ClaimDetailsViewField.DATE_SUBMITTED),
          asMediationField(ClaimDetailsViewField.AREA_OF_LAW),
          asMediationField(ClaimDetailsViewField.FEE_CODE),
          asMediationField(ClaimDetailsViewField.FEE_CODE_DESCRIPTION),
          asMediationField(ClaimDetailsViewField.MATTER_TYPE),
          asMediationField(ClaimDetailsViewField.CASE_START_DATE),
          asMediationField(ClaimDetailsViewField.DATE_OF_WORK_CONCLUDED));

  public static final List<ClaimViewField<MediationClaimDetails>> VALUE_ROWS =
      List.of(
          asMediationField(ClaimDetailsViewField.FIXED_FEE),
          asMediationField(ClaimDetailsViewField.DISBURSEMENTS),
          asMediationField(ClaimDetailsViewField.DISBURSEMENTS_VAT),
          asMediationField(ClaimDetailsViewField.VAT));

  public static final List<ClaimViewField<MediationClaimDetails>> TOTAL_ROWS =
      List.of(
          asMediationField(ClaimDetailsViewField.TOTAL_VAT),
          asMediationField(ClaimDetailsViewField.TOTAL_INCLUDING_VAT));

  @Override
  public String pageTitle() {
    return summaryRows().get(MediationClaimDetailsViewField.CLIENT_1_NAME).toString();
  }

  public Object getClient1Name() {
    return summaryRows().get(MediationClaimDetailsViewField.CLIENT_1_NAME);
  }

  public Object getClient1UCN() {
    return summaryRows().get(MediationClaimDetailsViewField.CLIENT_1_UCN);
  }

  public Object getClient2Name() {
    return summaryRows().get(MediationClaimDetailsViewField.CLIENT_2_NAME);
  }

  public Object getClient2UCN() {
    return summaryRows().get(MediationClaimDetailsViewField.CLIENT_2_UCN);
  }
}
