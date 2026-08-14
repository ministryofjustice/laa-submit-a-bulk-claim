package uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails;

import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimViewField.asCrimeLowerField;

import java.util.LinkedHashMap;
import java.util.List;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimViewField;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField;

public record CrimeClaimDetailsView(
    LinkedHashMap<ClaimViewField<CrimeLowerClaimDetails>, Object> summaryRows,
    LinkedHashMap<ClaimViewField<CrimeLowerClaimDetails>, Object> valueRows,
    LinkedHashMap<ClaimViewField<CrimeLowerClaimDetails>, Object> totalRows)
    implements ClaimDetailView<ClaimViewField<CrimeLowerClaimDetails>> {

  public CrimeClaimDetailsView(CrimeLowerClaimDetails crimeLowerClaimDetails) {
    this(
        ClaimViewField.toFieldMap(SUMMARY_ROWS.stream(), crimeLowerClaimDetails),
        ClaimViewField.toFieldMap(VALUE_ROWS.stream(), crimeLowerClaimDetails),
        ClaimViewField.toFieldMap(TOTAL_ROWS.stream(), crimeLowerClaimDetails));
  }

  public static final List<ClaimViewField<CrimeLowerClaimDetails>> SUMMARY_ROWS =
      List.of(
          asCrimeLowerField(ClaimDetailsViewField.CLIENT_NAME),
          asCrimeLowerField(ClaimDetailsViewField.UNIQUE_FILE_NUMBER),
          asCrimeLowerField(ClaimDetailsViewField.OFFICE_ACCOUNT_NUMBER),
          asCrimeLowerField(ClaimDetailsViewField.DATE_SUBMITTED),
          asCrimeLowerField(ClaimDetailsViewField.AREA_OF_LAW),
          asCrimeLowerField(ClaimDetailsViewField.FEE_CODE),
          asCrimeLowerField(ClaimDetailsViewField.FEE_CODE_DESCRIPTION),
          CrimeLowerClaimDetailsViewField.MATTER_TYPE,
          CrimeLowerClaimDetailsViewField.REPRESENTATION_ORDER_DATE,
          CrimeLowerClaimDetailsViewField.STAGE_REACHED,
          CrimeLowerClaimDetailsViewField.OUTCOME_CODE,
          asCrimeLowerField(ClaimDetailsViewField.DATE_OF_WORK_CONCLUDED),
          asCrimeLowerField(ClaimDetailsViewField.ESCAPE_CASE));

  public static final List<ClaimViewField<CrimeLowerClaimDetails>> VALUE_ROWS =
      List.of(
          asCrimeLowerField(ClaimDetailsViewField.FIXED_FEE),
          asCrimeLowerField(ClaimDetailsViewField.PROFIT_COSTS),
          asCrimeLowerField(ClaimDetailsViewField.DISBURSEMENTS),
          asCrimeLowerField(ClaimDetailsViewField.DISBURSEMENTS_VAT),
          CrimeLowerClaimDetailsViewField.TRAVEL_COSTS,
          CrimeLowerClaimDetailsViewField.WAITING_COSTS,
          asCrimeLowerField(ClaimDetailsViewField.VAT));

  public static final List<ClaimViewField<CrimeLowerClaimDetails>> TOTAL_ROWS =
      List.of(
          asCrimeLowerField(ClaimDetailsViewField.TOTAL_VAT),
          asCrimeLowerField(ClaimDetailsViewField.TOTAL_INCLUDING_VAT));

  @Override
  public String pageTitle() {
    return summaryRows().get(ClaimDetailsViewField.CLIENT_NAME).toString();
  }
}
