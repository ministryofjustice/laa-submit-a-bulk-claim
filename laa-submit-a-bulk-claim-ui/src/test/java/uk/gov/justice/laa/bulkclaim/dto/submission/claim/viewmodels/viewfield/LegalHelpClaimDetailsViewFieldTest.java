package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimReportedAndCalculatedValues;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.LegalHelpClaimDetails;
import uk.gov.justice.laa.bulkclaim.viewmodels.claimcase.LegalHelpClaimCaseView;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;

@DisplayName("Legal help claim details view field test")
class LegalHelpClaimDetailsViewFieldTest {

  @Test
  @DisplayName("Should read plain summary values via the accessor")
  void shouldReadSummaryValue() {
    LegalHelpClaimDetails details = LegalHelpClaimDetails.builder().clientForename("Jane").build();

    Object value =
        LegalHelpClaimDetailsViewField.CLIENT_FORENAME
            .getReportedAndCalculatedAccessor()
            .apply(details);

    assertThat(value).isEqualTo("Jane");
  }

  @Test
  @DisplayName("Should build a claim field row for a values-table field")
  void shouldBuildClaimFieldRow() {
    LegalHelpClaimDetails details =
        LegalHelpClaimDetails.builder()
            .reportedProfitCosts(new BigDecimal("100.00"))
            .initialCalculatedProfitCosts(new BigDecimal("110.00"))
            .build();

    Object value =
        LegalHelpClaimDetailsViewField.PROFIT_COSTS
            .getReportedAndCalculatedAccessor()
            .apply(details);

    assertThat(value).isInstanceOf(ClaimReportedAndCalculatedValues.class);
    ClaimReportedAndCalculatedValues row = (ClaimReportedAndCalculatedValues) value;
    assertThat(row.reported()).isEqualTo(new BigDecimal("100.00"));
    assertThat(row.initialCalculated()).isEqualTo(new BigDecimal("110.00"));
  }

  @Test
  @DisplayName("Should have no reported source for fixed fee, matching the BC-523 tab")
  void fixedFeeHasNoReportedSource() {
    LegalHelpClaimDetails details =
        LegalHelpClaimDetails.builder().initialCalculatedFixedFee(new BigDecimal("50.00")).build();

    ClaimReportedAndCalculatedValues row =
        (ClaimReportedAndCalculatedValues)
            LegalHelpClaimDetailsViewField.FIXED_FEE
                .getReportedAndCalculatedAccessor()
                .apply(details);

    assertThat(row.hasReportedValue()).isFalse();
    assertThat(row.initialCalculated()).isEqualTo(new BigDecimal("50.00"));

    ClaimFieldRow fieldRow = new ClaimFieldRow(row, null);
    assertThat(fieldRow.reported()).isNull();
  }

  @Test
  @DisplayName("Value rows list should contain every values-table field, in order")
  void valueRowsShouldBeOrdered() {
    assertThat(LegalHelpClaimCaseView.VALUE_ROWS)
        .containsExactly(
            LegalHelpClaimDetailsViewField.FIXED_FEE,
            LegalHelpClaimDetailsViewField.PROFIT_COSTS,
            LegalHelpClaimDetailsViewField.DISBURSEMENTS,
            LegalHelpClaimDetailsViewField.DISBURSEMENTS_VAT,
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
            LegalHelpClaimDetailsViewField.VAT_INDICATOR);
  }

  @Test
  @DisplayName("Total rows list should contain every total-table field, in order")
  void totalRowsShouldBeOrdered() {
    assertThat(LegalHelpClaimCaseView.TOTAL_ROWS)
        .containsExactly(
            LegalHelpClaimDetailsViewField.TOTAL_VAT,
            LegalHelpClaimDetailsViewField.TOTAL_INCLUDING_VAT);
  }

  @Test
  @DisplayName(
      "Travel and waiting costs' Current Calculated value sums the assessment's separate travel"
          + " and waiting fields")
  void travelAndWaitingCostsSumsAssessmentFields() {
    AssessmentGet assessment =
        new AssessmentGet()
            .netTravelCostsAmount(new BigDecimal("100.00"))
            .netWaitingCostsAmount(new BigDecimal("25.50"));

    Object value =
        LegalHelpClaimDetailsViewField.TRAVEL_AND_WAITING_COSTS
            .getCurrentCalculatedAccessor()
            .apply(assessment);

    assertThat(value).isEqualTo(new BigDecimal("125.50"));
  }

  @Test
  @DisplayName("Travel and waiting costs' Current Calculated value is absent when both are absent")
  void travelAndWaitingCostsIsNullWhenBothAssessmentFieldsAreAbsent() {
    Object value =
        LegalHelpClaimDetailsViewField.TRAVEL_AND_WAITING_COSTS
            .getCurrentCalculatedAccessor()
            .apply(new AssessmentGet());

    assertThat(value).isNull();
  }

  @Test
  @DisplayName("London rate has no assessment accessor - AssessmentGet has no equivalent field")
  void londonRateHasNoAssessmentAccessor() {
    assertThat(LegalHelpClaimDetailsViewField.LONDON_RATE.getCurrentCalculatedAccessor()).isNull();
  }
}
