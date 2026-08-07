package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;

@DisplayName("Crime lower claim details view field test")
class CrimeLowerClaimDetailsViewFieldTest {

  @Test
  @DisplayName("Should read plain summary values via the accessor")
  void shouldReadSummaryValue() {
    CrimeLowerClaimDetails details =
        CrimeLowerClaimDetails.builder().clientForename("Jane").build();

    Object value = CrimeLowerClaimDetailsViewField.CLIENT_FORENAME.getAccessor().apply(details);

    assertThat(value).isEqualTo("Jane");
  }

  @Test
  @DisplayName("Should build a claim field row for a values-table field")
  void shouldBuildClaimFieldRow() {
    CrimeLowerClaimDetails details =
        CrimeLowerClaimDetails.builder()
            .reportedProfitCosts(new BigDecimal("100.00"))
            .initialCalculatedProfitCosts(new BigDecimal("110.00"))
            .build();

    Object value = CrimeLowerClaimDetailsViewField.PROFIT_COSTS.getAccessor().apply(details);

    assertThat(value).isInstanceOf(ClaimFieldRow.class);
    ClaimFieldRow row = (ClaimFieldRow) value;
    assertThat(row.reported()).isEqualTo(new BigDecimal("100.00"));
    assertThat(row.initialCalculated()).isEqualTo(new BigDecimal("110.00"));
    assertThat(row.currentCalculated()).isNull();
  }

  @Test
  @DisplayName("Should have no reported source for fixed fee, matching the BC-523 tab")
  void fixedFeeHasNoReportedSource() {
    CrimeLowerClaimDetails details =
        CrimeLowerClaimDetails.builder().initialCalculatedFixedFee(new BigDecimal("50.00")).build();

    ClaimFieldRow row =
        (ClaimFieldRow) CrimeLowerClaimDetailsViewField.FIXED_FEE.getAccessor().apply(details);

    assertThat(row.hasReportedValue()).isFalse();
    assertThat(row.getReportedDisplay()).isEqualTo(ClaimFieldRow.NOT_APPLICABLE);
    assertThat(row.initialCalculated()).isEqualTo(new BigDecimal("50.00"));
  }

  @Test
  @DisplayName("Value rows list should contain every values-table field, in order")
  void valueRowsShouldBeOrdered() {
    assertThat(CrimeLowerClaimDetailsViewField.VALUE_ROWS)
        .containsExactly(
            CrimeLowerClaimDetailsViewField.FIXED_FEE,
            CrimeLowerClaimDetailsViewField.PROFIT_COSTS,
            CrimeLowerClaimDetailsViewField.DISBURSEMENTS,
            CrimeLowerClaimDetailsViewField.DISBURSEMENTS_VAT,
            CrimeLowerClaimDetailsViewField.TRAVEL_COSTS,
            CrimeLowerClaimDetailsViewField.WAITING_COSTS,
            CrimeLowerClaimDetailsViewField.VAT);
  }

  @Test
  @DisplayName("Total rows list should contain every total-table field, in order")
  void totalRowsShouldBeOrdered() {
    assertThat(CrimeLowerClaimDetailsViewField.TOTAL_ROWS)
        .containsExactly(
            CrimeLowerClaimDetailsViewField.TOTAL_VAT,
            CrimeLowerClaimDetailsViewField.TOTAL_INCLUDING_VAT);
  }
}
