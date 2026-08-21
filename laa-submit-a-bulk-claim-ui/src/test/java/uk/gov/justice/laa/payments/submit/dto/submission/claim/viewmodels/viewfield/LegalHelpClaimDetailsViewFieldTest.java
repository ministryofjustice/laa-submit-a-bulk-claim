package uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.viewfield;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.LegalHelpClaimDetails;
import uk.gov.justice.laa.payments.submit.viewmodels.claimdetails.LegalHelpClaimDetailsView;

@DisplayName("Legal help claim details view field test")
class LegalHelpClaimDetailsViewFieldTest {

  @Test
  @DisplayName(
      "Should wrap the legal-help-specific London rate scalar as a reported-only ClaimFieldRow")
  void shouldReadLondonRate() {
    LegalHelpClaimDetails details = new LegalHelpClaimDetails();
    details.setReportedLondonRateIndicator(true);

    Object value = LegalHelpClaimDetailsViewField.LONDON_RATE.getAccessor().apply(details);

    assertThat(value).isEqualTo(new ClaimFieldRow(true, null, null));
  }

  @Test
  @DisplayName("Should read a values-table field's ClaimFieldRow via the accessor")
  void shouldReadClaimField() {
    LegalHelpClaimDetails details = new LegalHelpClaimDetails();
    ClaimFieldRow counselsCosts =
        new ClaimFieldRow(new BigDecimal("100.00"), new BigDecimal("110.00"), null);
    details.setCounselsCosts(counselsCosts);

    Object value = LegalHelpClaimDetailsViewField.COUNSELS_COSTS.getAccessor().apply(details);

    assertThat(value).isEqualTo(counselsCosts);
  }

  @Test
  @DisplayName("Value rows list should contain every values-table field, in order")
  void valueRowsShouldBeOrdered() {
    assertThat(LegalHelpClaimDetailsView.VALUE_ROWS)
        .containsExactly(
            ClaimViewField.asLegalHelpField(ClaimDetailsViewField.FIXED_FEE),
            ClaimViewField.asLegalHelpField(ClaimDetailsViewField.PROFIT_COSTS),
            ClaimViewField.asLegalHelpField(ClaimDetailsViewField.DISBURSEMENTS),
            ClaimViewField.asLegalHelpField(ClaimDetailsViewField.DISBURSEMENTS_VAT),
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
            ClaimViewField.asLegalHelpField(ClaimDetailsViewField.VAT));
  }

  @Test
  @DisplayName("Total rows list should contain every total-table field, in order")
  void totalRowsShouldBeOrdered() {
    assertThat(LegalHelpClaimDetailsView.TOTAL_ROWS)
        .containsExactly(
            ClaimViewField.asLegalHelpField(ClaimDetailsViewField.TOTAL_VAT),
            ClaimViewField.asLegalHelpField(ClaimDetailsViewField.TOTAL_INCLUDING_VAT));
  }
}
