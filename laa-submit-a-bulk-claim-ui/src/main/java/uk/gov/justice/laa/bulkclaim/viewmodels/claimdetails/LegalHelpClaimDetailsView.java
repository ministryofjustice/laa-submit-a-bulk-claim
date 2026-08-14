package uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails;

import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimViewField.asLegalHelpField;

import java.util.LinkedHashMap;
import java.util.List;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.LegalHelpClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimViewField;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField;

public record LegalHelpClaimDetailsView(
    LinkedHashMap<ClaimViewField<LegalHelpClaimDetails>, Object> summaryRows,
    LinkedHashMap<ClaimViewField<LegalHelpClaimDetails>, Object> valueRows,
    LinkedHashMap<ClaimViewField<LegalHelpClaimDetails>, Object> totalRows)
    implements ClaimDetailView<ClaimViewField<LegalHelpClaimDetails>> {

  public LegalHelpClaimDetailsView(LegalHelpClaimDetails legalHelpClaimDetails) {
    this(
        ClaimViewField.toFieldMap(SUMMARY_ROWS.stream(), legalHelpClaimDetails),
        ClaimViewField.toFieldMap(VALUE_ROWS.stream(), legalHelpClaimDetails),
        ClaimViewField.toFieldMap(TOTAL_ROWS.stream(), legalHelpClaimDetails));
  }

  public static final List<ClaimViewField<LegalHelpClaimDetails>> SUMMARY_ROWS =
      List.of(
          asLegalHelpField(ClaimDetailsViewField.CLIENT_NAME),
          asLegalHelpField(ClaimDetailsViewField.UNIQUE_FILE_NUMBER),
          asLegalHelpField(ClaimDetailsViewField.OFFICE_ACCOUNT_NUMBER),
          asLegalHelpField(ClaimDetailsViewField.DATE_SUBMITTED),
          asLegalHelpField(ClaimDetailsViewField.AREA_OF_LAW),
          LegalHelpClaimDetailsViewField.CATEGORY_OF_LAW,
          asLegalHelpField(ClaimDetailsViewField.FEE_CODE),
          asLegalHelpField(ClaimDetailsViewField.FEE_CODE_DESCRIPTION),
          LegalHelpClaimDetailsViewField.MATTER_TYPE_1,
          LegalHelpClaimDetailsViewField.MATTER_TYPE_2,
          asLegalHelpField(ClaimDetailsViewField.CASE_START_DATE),
          asLegalHelpField(ClaimDetailsViewField.DATE_OF_WORK_CONCLUDED),
          asLegalHelpField(ClaimDetailsViewField.ESCAPE_CASE));

  public static final List<ClaimViewField<LegalHelpClaimDetails>> VALUE_ROWS =
      List.of(
          asLegalHelpField(ClaimDetailsViewField.FIXED_FEE),
          asLegalHelpField(ClaimDetailsViewField.PROFIT_COSTS),
          asLegalHelpField(ClaimDetailsViewField.DISBURSEMENTS),
          asLegalHelpField(ClaimDetailsViewField.DISBURSEMENTS_VAT),
          LegalHelpClaimDetailsViewField.COUNSELS_COSTS,
          LegalHelpClaimDetailsViewField.TRAVEL_AND_WAITING_COSTS,
          LegalHelpClaimDetailsViewField.DETENTION_TRAVEL_WAITING_COSTS,
          LegalHelpClaimDetailsViewField.JR_FORM_FILLING,
          LegalHelpClaimDetailsViewField.ADJOURNED_HEARING_FEE,
          LegalHelpClaimDetailsViewField.CMRH_ORAL,
          LegalHelpClaimDetailsViewField.CMRH_TELEPHONE,
          LegalHelpClaimDetailsViewField.LONDON_RATE,
          LegalHelpClaimDetailsViewField.HOME_OFFICE_INTERVIEW,
          LegalHelpClaimDetailsViewField.SUBSTANTIVE_HEARING,
          asLegalHelpField(ClaimDetailsViewField.VAT));

  public static final List<ClaimViewField<LegalHelpClaimDetails>> TOTAL_ROWS =
      List.of(
          asLegalHelpField(ClaimDetailsViewField.TOTAL_VAT),
          asLegalHelpField(ClaimDetailsViewField.TOTAL_INCLUDING_VAT));

  @Override
  public String pageTitle() {
    return summaryRows().get(ClaimDetailsViewField.CLIENT_NAME).toString();
  }
}
