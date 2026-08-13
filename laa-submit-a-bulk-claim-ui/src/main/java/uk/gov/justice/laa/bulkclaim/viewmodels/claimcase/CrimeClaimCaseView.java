package uk.gov.justice.laa.bulkclaim.viewmodels.claimcase;

import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.AREA_OF_LAW;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.CLIENT_NAME;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.DATE_OF_WORK_CONCLUDED;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.DATE_SUBMITTED;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.DISBURSEMENTS;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.DISBURSEMENTS_VAT;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.ESCAPE_CASE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.FEE_CODE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.FEE_CODE_DESCRIPTION;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.FIXED_FEE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.MATTER_TYPE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.OFFICE_ACCOUNT_NUMBER;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.OUTCOME_CODE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.PROFIT_COSTS;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.REPRESENTATION_ORDER_DATE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.STAGE_REACHED;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.TOTAL_INCLUDING_VAT;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.TOTAL_VAT;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.TRAVEL_COSTS;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.UNIQUE_FILE_NUMBER;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.VAT;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.WAITING_COSTS;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Stream;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimReportedAndCalculatedValues;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;

public record CrimeClaimCaseView(
    LinkedHashMap<CrimeLowerClaimDetailsViewField, Object> summaryRows,
    LinkedHashMap<CrimeLowerClaimDetailsViewField, ClaimFieldRow> valueRows,
    LinkedHashMap<CrimeLowerClaimDetailsViewField, ClaimFieldRow> totalRows)
    implements ClaimDetailView<CrimeLowerClaimDetailsViewField> {

  public CrimeClaimCaseView(
      CrimeLowerClaimDetails crimeLowerClaimDetails, AssessmentGet currentAssessment) {
    this(
        createSummaryRows(crimeLowerClaimDetails),
        createValueRows(crimeLowerClaimDetails, currentAssessment),
        createTotalRows(crimeLowerClaimDetails, currentAssessment));
  }

  public static final List<CrimeLowerClaimDetailsViewField> SUMMARY_ROWS =
      List.of(
          CLIENT_NAME,
          UNIQUE_FILE_NUMBER,
          OFFICE_ACCOUNT_NUMBER,
          DATE_SUBMITTED,
          AREA_OF_LAW,
          FEE_CODE,
          FEE_CODE_DESCRIPTION,
          MATTER_TYPE,
          REPRESENTATION_ORDER_DATE,
          STAGE_REACHED,
          OUTCOME_CODE,
          DATE_OF_WORK_CONCLUDED,
          ESCAPE_CASE);

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

  private static LinkedHashMap<CrimeLowerClaimDetailsViewField, Object> createSummaryRows(
      CrimeLowerClaimDetails claim) {
    LinkedHashMap<CrimeLowerClaimDetailsViewField, Object> summaryMap = new LinkedHashMap<>();
    SUMMARY_ROWS.forEach(
        field -> summaryMap.put(field, field.getReportedAndCalculatedAccessor().apply(claim)));
    return summaryMap;
  }

  private static LinkedHashMap<CrimeLowerClaimDetailsViewField, ClaimFieldRow> createTotalRows(
      CrimeLowerClaimDetails claim, AssessmentGet currentAssessment) {
    return toFieldMap(TOTAL_ROWS.stream(), claim, currentAssessment);
  }

  private static LinkedHashMap<CrimeLowerClaimDetailsViewField, ClaimFieldRow> createValueRows(
      CrimeLowerClaimDetails claim, AssessmentGet currentAssessment) {
    return toFieldMap(VALUE_ROWS.stream(), claim, currentAssessment);
  }

  static LinkedHashMap<CrimeLowerClaimDetailsViewField, ClaimFieldRow> toFieldMap(
      Stream<CrimeLowerClaimDetailsViewField> fields,
      CrimeLowerClaimDetails claim,
      AssessmentGet currentAssessment) {
    LinkedHashMap<CrimeLowerClaimDetailsViewField, ClaimFieldRow> fieldMap = new LinkedHashMap<>();
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
