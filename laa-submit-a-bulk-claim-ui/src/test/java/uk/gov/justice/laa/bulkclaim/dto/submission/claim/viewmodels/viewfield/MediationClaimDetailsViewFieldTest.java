package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.MediationClaimDetails;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;

@DisplayName("Mediation claim details view field test")
class MediationClaimDetailsViewFieldTest {

  @Test
  @DisplayName("Should read plain summary values via the accessor")
  void shouldReadSummaryValue() {
    MediationClaimDetails details = MediationClaimDetails.builder().client1Forename("Jane").build();

    Object value = MediationClaimDetailsViewField.CLIENT_1_FORENAME.getAccessor().apply(details);

    assertThat(value).isEqualTo("Jane");
  }

  @Test
  @DisplayName("Should build a claim field row for a values-table field")
  void shouldBuildClaimFieldRow() {
    MediationClaimDetails details =
        MediationClaimDetails.builder()
            .reportedDisbursements(new BigDecimal("100.00"))
            .initialCalculatedDisbursements(new BigDecimal("110.00"))
            .build();

    Object value = MediationClaimDetailsViewField.DISBURSEMENTS.getAccessor().apply(details);

    assertThat(value).isInstanceOf(ClaimFieldRow.class);
    ClaimFieldRow row = (ClaimFieldRow) value;
    assertThat(row.reported()).isEqualTo(new BigDecimal("100.00"));
    assertThat(row.initialCalculated()).isEqualTo(new BigDecimal("110.00"));
    assertThat(row.currentCalculated()).isNull();
  }

  @Test
  @DisplayName("Should have no reported source for fixed fee, matching the BC-523 tab")
  void fixedFeeHasNoReportedSource() {
    MediationClaimDetails details =
        MediationClaimDetails.builder().initialCalculatedFixedFee(new BigDecimal("50.00")).build();

    ClaimFieldRow row =
        (ClaimFieldRow) MediationClaimDetailsViewField.FIXED_FEE.getAccessor().apply(details);

    assertThat(row.hasReportedValue()).isFalse();
    assertThat(row.getReportedDisplay()).isEqualTo(ClaimFieldRow.NOT_APPLICABLE);
    assertThat(row.initialCalculated()).isEqualTo(new BigDecimal("50.00"));
  }

  @Test
  @DisplayName("Value rows list should contain every values-table field, in order")
  void valueRowsShouldBeOrdered() {
    assertThat(MediationClaimDetailsViewField.VALUE_ROWS)
        .containsExactly(
            MediationClaimDetailsViewField.FIXED_FEE,
            MediationClaimDetailsViewField.DISBURSEMENTS,
            MediationClaimDetailsViewField.DISBURSEMENTS_VAT,
            MediationClaimDetailsViewField.VAT_INDICATOR);
  }

  @Test
  @DisplayName("Total rows list should contain every total-table field, in order")
  void totalRowsShouldBeOrdered() {
    assertThat(MediationClaimDetailsViewField.TOTAL_ROWS)
        .containsExactly(
            MediationClaimDetailsViewField.TOTAL_VAT,
            MediationClaimDetailsViewField.TOTAL_INCLUDING_VAT);
  }

  @Test
  @DisplayName(
      "Should read the Current Calculated value for a values-table field from the assessment")
  void shouldReadCurrentCalculatedFromAssessment() {
    AssessmentGet assessment = new AssessmentGet().disbursementAmount(new BigDecimal("220.20"));

    Object value =
        MediationClaimDetailsViewField.DISBURSEMENTS.getAssessmentAccessor().apply(assessment);

    assertThat(value).isEqualTo(new BigDecimal("220.20"));
  }

  @Test
  @DisplayName("A header field has no assessment accessor")
  void headerFieldHasNoAssessmentAccessor() {
    assertThat(MediationClaimDetailsViewField.CLIENT_1_FORENAME.getAssessmentAccessor()).isNull();
  }
}
