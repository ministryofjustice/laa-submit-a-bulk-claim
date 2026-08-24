package uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails;

import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.AREA_OF_LAW;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimDetailsViewField.CASE_START_DATE;
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
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimViewField.asLegalHelpField;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.ADJOURNED_HEARING_FEE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.CATEGORY_OF_LAW;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.CMRH_ORAL;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.CMRH_TELEPHONE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.COUNSELS_COSTS;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.DETENTION_TRAVEL_WAITING_COSTS;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.HOME_OFFICE_INTERVIEW;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.JR_FORM_FILLING;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.LONDON_RATE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.MATTER_TYPE_1;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.MATTER_TYPE_2;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.SUBSTANTIVE_HEARING;
import static uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.LegalHelpClaimDetailsViewField.TRAVEL_AND_WAITING_COSTS;

import java.util.LinkedHashMap;
import java.util.List;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.LegalHelpClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield.ClaimViewField;

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
          asLegalHelpField(CLIENT_NAME),
          asLegalHelpField(UNIQUE_FILE_NUMBER),
          asLegalHelpField(OFFICE_ACCOUNT_NUMBER),
          asLegalHelpField(DATE_SUBMITTED),
          asLegalHelpField(AREA_OF_LAW),
          CATEGORY_OF_LAW,
          asLegalHelpField(FEE_CODE),
          asLegalHelpField(FEE_CODE_DESCRIPTION),
          MATTER_TYPE_1,
          MATTER_TYPE_2,
          asLegalHelpField(CASE_START_DATE),
          asLegalHelpField(DATE_OF_WORK_CONCLUDED),
          asLegalHelpField(ESCAPE_CASE));

  public static final List<ClaimViewField<LegalHelpClaimDetails>> VALUE_ROWS =
      List.of(
          asLegalHelpField(FIXED_FEE),
          asLegalHelpField(PROFIT_COSTS),
          asLegalHelpField(DISBURSEMENTS),
          asLegalHelpField(DISBURSEMENTS_VAT),
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
          asLegalHelpField(VAT));

  public static final List<ClaimViewField<LegalHelpClaimDetails>> TOTAL_ROWS =
      List.of(asLegalHelpField(TOTAL_VAT), asLegalHelpField(TOTAL_INCLUDING_VAT));

  @Override
  public String pageTitle() {
    return summaryRows().get(CLIENT_NAME).toString();
  }
}
