package uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails;

import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.AREA_OF_LAW;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.CLIENT_NAME;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.DATE_OF_WORK_CONCLUDED;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.DATE_SUBMITTED;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.DISBURSEMENTS;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.DISBURSEMENTS_VAT;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.ESCAPE_CASE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.FEE_CODE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.FEE_CODE_DESCRIPTION;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.FIXED_FEE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.OFFICE_ACCOUNT_NUMBER;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.PROFIT_COSTS;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.TOTAL_INCLUDING_VAT;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.TOTAL_VAT;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.UNIQUE_FILE_NUMBER;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.VAT;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimViewField.asCrimeLowerField;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.MATTER_TYPE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.OUTCOME_CODE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.REPRESENTATION_ORDER_DATE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.STAGE_REACHED;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.TRAVEL_COSTS;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.CrimeLowerClaimDetailsViewField.WAITING_COSTS;

import java.util.LinkedHashMap;
import java.util.List;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimViewField;

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
          asCrimeLowerField(CLIENT_NAME),
          asCrimeLowerField(UNIQUE_FILE_NUMBER),
          asCrimeLowerField(OFFICE_ACCOUNT_NUMBER),
          asCrimeLowerField(DATE_SUBMITTED),
          asCrimeLowerField(AREA_OF_LAW),
          asCrimeLowerField(FEE_CODE),
          asCrimeLowerField(FEE_CODE_DESCRIPTION),
          MATTER_TYPE,
          REPRESENTATION_ORDER_DATE,
          STAGE_REACHED,
          OUTCOME_CODE,
          asCrimeLowerField(DATE_OF_WORK_CONCLUDED),
          asCrimeLowerField(ESCAPE_CASE));

  public static final List<ClaimViewField<CrimeLowerClaimDetails>> VALUE_ROWS =
      List.of(
          asCrimeLowerField(FIXED_FEE),
          asCrimeLowerField(PROFIT_COSTS),
          asCrimeLowerField(DISBURSEMENTS),
          asCrimeLowerField(DISBURSEMENTS_VAT),
          TRAVEL_COSTS,
          WAITING_COSTS,
          asCrimeLowerField(VAT));

  public static final List<ClaimViewField<CrimeLowerClaimDetails>> TOTAL_ROWS =
      List.of(asCrimeLowerField(TOTAL_VAT), asCrimeLowerField(TOTAL_INCLUDING_VAT));

  @Override
  public String pageTitle() {
    return summaryRows().get(CLIENT_NAME).toString();
  }
}
