package uk.gov.justice.laa.bulkclaim.viewmodels.claimcase;

import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.AREA_OF_LAW;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.CASE_START_DATE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.CLIENT_1_NAME;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.CLIENT_1_UCN;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.CLIENT_2_NAME;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.CLIENT_2_UCN;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.DATE_OF_WORK_CONCLUDED;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.DATE_SUBMITTED;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.DISBURSEMENTS;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.DISBURSEMENTS_VAT;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.FEE_CODE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.FEE_CODE_DESCRIPTION;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.FIXED_FEE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.MATTER_TYPE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.OFFICE_ACCOUNT_NUMBER;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.TOTAL_INCLUDING_VAT;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.TOTAL_VAT;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField.VAT_INDICATOR;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimReportedAndCalculatedValues;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.MediationClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.MediationClaimDetailsViewField;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;

public record MediationClaimCaseView(
    LinkedHashMap<MediationClaimDetailsViewField, Object> summaryRows,
    LinkedHashMap<MediationClaimDetailsViewField, ClaimFieldRow> valueRows,
    LinkedHashMap<MediationClaimDetailsViewField, ClaimFieldRow> totalRows)
    implements ClaimDetailView<MediationClaimDetailsViewField> {

  public MediationClaimCaseView(
      MediationClaimDetails mediationClaimDetails, AssessmentGet currentAssessment) {
    this(
        createSummaryRows(mediationClaimDetails),
        createValueRows(mediationClaimDetails, currentAssessment),
        createTotalRows(mediationClaimDetails, currentAssessment));
  }

  public static final List<MediationClaimDetailsViewField> SUMMARY_ROWS =
      List.of(
          CLIENT_1_NAME,
          CLIENT_1_UCN,
          CLIENT_2_NAME,
          CLIENT_2_UCN,
          OFFICE_ACCOUNT_NUMBER,
          DATE_SUBMITTED,
          AREA_OF_LAW,
          FEE_CODE,
          FEE_CODE_DESCRIPTION,
          DATE_SUBMITTED,
          MATTER_TYPE,
          CASE_START_DATE,
          DATE_OF_WORK_CONCLUDED);

  public static final List<MediationClaimDetailsViewField> VALUE_ROWS =
      List.of(FIXED_FEE, DISBURSEMENTS, DISBURSEMENTS_VAT, VAT_INDICATOR);

  public static final List<MediationClaimDetailsViewField> TOTAL_ROWS =
      List.of(TOTAL_VAT, TOTAL_INCLUDING_VAT);

  private static LinkedHashMap<MediationClaimDetailsViewField, Object> createSummaryRows(
      MediationClaimDetails claim) {
    LinkedHashMap<MediationClaimDetailsViewField, Object> summaryMap = new LinkedHashMap<>();
    SUMMARY_ROWS.forEach(
        field -> summaryMap.put(field, field.getReportedAndCalculatedAccessor().apply(claim)));
    return summaryMap;
  }

  private static LinkedHashMap<MediationClaimDetailsViewField, ClaimFieldRow> createTotalRows(
      MediationClaimDetails mediationClaimDetails, AssessmentGet currentAssessment) {
    return toFieldMap(TOTAL_ROWS.stream(), mediationClaimDetails, currentAssessment);
  }

  private static LinkedHashMap<MediationClaimDetailsViewField, ClaimFieldRow> createValueRows(
      MediationClaimDetails mediationClaimDetails, AssessmentGet currentAssessment) {
    return toFieldMap(VALUE_ROWS.stream(), mediationClaimDetails, currentAssessment);
  }

  static LinkedHashMap<MediationClaimDetailsViewField, ClaimFieldRow> toFieldMap(
      Stream<MediationClaimDetailsViewField> fields,
      MediationClaimDetails claim,
      AssessmentGet currentAssessment) {
    LinkedHashMap<MediationClaimDetailsViewField, ClaimFieldRow> fieldMap = new LinkedHashMap<>();
    fields.forEach(
        field ->
            fieldMap.put(
                field,
                new ClaimFieldRow(
                    (ClaimReportedAndCalculatedValues)
                        field.getReportedAndCalculatedAccessor().apply(claim),
                    ClaimDetailView.getCurrentCalculatedAccessor(currentAssessment, field))));
    return fieldMap;
  }

  @Override
  public String pageTitle() {
    return summaryRows().get(CLIENT_1_NAME).toString();
  }
}
