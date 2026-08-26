package uk.gov.justice.laa.payments.submit.viewmodels.claimdetails;

import static uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.AREA_OF_LAW;
import static uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.CASE_START_DATE;
import static uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.DATE_OF_WORK_CONCLUDED;
import static uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.DATE_SUBMITTED;
import static uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.DISBURSEMENTS;
import static uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.DISBURSEMENTS_VAT;
import static uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.FEE_CODE;
import static uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.FEE_CODE_DESCRIPTION;
import static uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.FIXED_FEE;
import static uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.MATTER_TYPE;
import static uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.OFFICE_ACCOUNT_NUMBER;
import static uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.TOTAL_INCLUDING_VAT;
import static uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.TOTAL_VAT;
import static uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.VAT;
import static uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.ClaimViewField.asMediationField;
import static uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.CLIENT_1_NAME;
import static uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.CLIENT_1_UCN;
import static uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.CLIENT_2_NAME;
import static uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.CLIENT_2_UCN;

import java.util.LinkedHashMap;
import java.util.List;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.MediationClaimDetails;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield.ClaimViewField;

public record MediationClaimDetailsView(
    LinkedHashMap<ClaimViewField<MediationClaimDetails>, Object> summaryRows,
    LinkedHashMap<ClaimViewField<MediationClaimDetails>, Object> valueRows,
    LinkedHashMap<ClaimViewField<MediationClaimDetails>, Object> totalRows)
    implements ClaimDetailView<ClaimViewField<MediationClaimDetails>> {

  public MediationClaimDetailsView(MediationClaimDetails mediationClaimDetails) {
    this(
        ClaimViewField.toFieldMap(SUMMARY_ROWS.stream(), mediationClaimDetails),
        ClaimViewField.toFieldMap(VALUE_ROWS.stream(), mediationClaimDetails),
        ClaimViewField.toFieldMap(TOTAL_ROWS.stream(), mediationClaimDetails));
  }

  public static final List<ClaimViewField<MediationClaimDetails>> SUMMARY_ROWS =
      List.of(
          CLIENT_1_NAME,
          CLIENT_1_UCN,
          CLIENT_2_NAME,
          CLIENT_2_UCN,
          asMediationField(OFFICE_ACCOUNT_NUMBER),
          asMediationField(DATE_SUBMITTED),
          asMediationField(AREA_OF_LAW),
          asMediationField(FEE_CODE),
          asMediationField(FEE_CODE_DESCRIPTION),
          asMediationField(MATTER_TYPE),
          asMediationField(CASE_START_DATE),
          asMediationField(DATE_OF_WORK_CONCLUDED));

  public static final List<ClaimViewField<MediationClaimDetails>> VALUE_ROWS =
      List.of(
          asMediationField(FIXED_FEE),
          asMediationField(DISBURSEMENTS),
          asMediationField(DISBURSEMENTS_VAT),
          asMediationField(VAT));

  public static final List<ClaimViewField<MediationClaimDetails>> TOTAL_ROWS =
      List.of(asMediationField(TOTAL_VAT), asMediationField(TOTAL_INCLUDING_VAT));

  @Override
  public String pageTitle() {
    return summaryRows().get(CLIENT_1_NAME).toString();
  }

  public Object getClient1Name() {
    return summaryRows().get(CLIENT_1_NAME);
  }

  public Object getClient1Ucn() {
    return summaryRows().get(CLIENT_1_UCN);
  }

  public Object getClient2Name() {
    return summaryRows().get(CLIENT_2_NAME);
  }

  public Object getClient2Ucn() {
    return summaryRows().get(CLIENT_2_UCN);
  }
}
