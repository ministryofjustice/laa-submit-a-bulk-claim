package uk.gov.justice.laa.bulkclaim.viewmodels.claimcase;

import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.ADJOURNED_HEARING_FEE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.AREA_OF_LAW;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.CASE_START_DATE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.CATEGORY_OF_LAW;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.CLIENT_NAME;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.CMRH_ORAL;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.CMRH_TELEPHONE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.COUNSELS_COSTS;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.DATE_OF_WORK_CONCLUDED;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.DATE_SUBMITTED;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.DETENTION_TRAVEL_WAITING_COSTS;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.DISBURSEMENTS;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.DISBURSEMENTS_VAT;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.ESCAPE_CASE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.FEE_CODE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.FEE_CODE_DESCRIPTION;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.FIXED_FEE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.HOME_OFFICE_INTERVIEW;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.JR_FORM_FILLING;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.LONDON_RATE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.MATTER_TYPE_1;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.MATTER_TYPE_2;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.OFFICE_ACCOUNT_NUMBER;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.PROFIT_COSTS;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.SUBSTANTIVE_HEARING;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.TOTAL_INCLUDING_VAT;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.TOTAL_VAT;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.TRAVEL_AND_WAITING_COSTS;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.UNIQUE_FILE_NUMBER;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.VAT_INDICATOR;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimReportedAndCalculatedValues;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.LegalHelpClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;

public record LegalHelpClaimCaseView(
    LinkedHashMap<LegalHelpClaimDetailsViewField, Object> summaryRows,
    LinkedHashMap<LegalHelpClaimDetailsViewField, ClaimFieldRow> valueRows,
    LinkedHashMap<LegalHelpClaimDetailsViewField, ClaimFieldRow> totalRows)
    implements ClaimDetailView<LegalHelpClaimDetailsViewField> {

  public LegalHelpClaimCaseView(
      LegalHelpClaimDetails legalHelpClaimDetails, AssessmentGet currentAssessment) {
    this(
        createSummaryRows(legalHelpClaimDetails),
        createValueRows(legalHelpClaimDetails, currentAssessment),
        createTotalRows(legalHelpClaimDetails, currentAssessment));
  }

  public static final List<LegalHelpClaimDetailsViewField> SUMMARY_ROWS =
      List.of(
          CLIENT_NAME,
          UNIQUE_FILE_NUMBER,
          OFFICE_ACCOUNT_NUMBER,
          DATE_SUBMITTED,
          AREA_OF_LAW,
          CATEGORY_OF_LAW,
          FEE_CODE,
          FEE_CODE_DESCRIPTION,
          MATTER_TYPE_1,
          MATTER_TYPE_2,
          CASE_START_DATE,
          DATE_OF_WORK_CONCLUDED,
          ESCAPE_CASE);

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

  private static LinkedHashMap<LegalHelpClaimDetailsViewField, Object> createSummaryRows(
      LegalHelpClaimDetails claim) {
    LinkedHashMap<LegalHelpClaimDetailsViewField, Object> summaryMap = new LinkedHashMap<>();
    SUMMARY_ROWS.forEach(
        field -> summaryMap.put(field, field.getReportedAndCalculatedAccessor().apply(claim)));
    return summaryMap;
  }

  private static LinkedHashMap<LegalHelpClaimDetailsViewField, ClaimFieldRow> createTotalRows(
      LegalHelpClaimDetails legalHelpClaimDetails, AssessmentGet currentAssessment) {
    return toFieldMap(TOTAL_ROWS.stream(), legalHelpClaimDetails, currentAssessment);
  }

  private static LinkedHashMap<LegalHelpClaimDetailsViewField, ClaimFieldRow> createValueRows(
      LegalHelpClaimDetails legalHelpClaimDetails, AssessmentGet currentAssessment) {
    return toFieldMap(VALUE_ROWS.stream(), legalHelpClaimDetails, currentAssessment);
  }

  static LinkedHashMap<LegalHelpClaimDetailsViewField, ClaimFieldRow> toFieldMap(
      Stream<LegalHelpClaimDetailsViewField> fields,
      LegalHelpClaimDetails claim,
      AssessmentGet currentAssessment) {
    LinkedHashMap<LegalHelpClaimDetailsViewField, ClaimFieldRow> fieldMap = new LinkedHashMap<>();
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
    return summaryRows().get(CLIENT_NAME).toString();
  }
}
