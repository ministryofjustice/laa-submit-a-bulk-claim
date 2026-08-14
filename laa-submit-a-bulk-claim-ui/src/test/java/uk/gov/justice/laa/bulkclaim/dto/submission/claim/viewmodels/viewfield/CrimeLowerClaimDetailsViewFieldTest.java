package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;
import uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails.CrimeClaimDetailsView;

@DisplayName("Crime lower claim details view field test")
class CrimeLowerClaimDetailsViewFieldTest {

  @Test
  @DisplayName("Should read the crime-specific matter type via the accessor")
  void shouldReadCrimeSpecificMatterType() {
    CrimeLowerClaimDetails details = new CrimeLowerClaimDetails();
    details.setCrimeMatterTypeCode("INVC");

    Object value = CrimeLowerClaimDetailsViewField.MATTER_TYPE.getAccessor().apply(details);

    assertThat(value).isEqualTo("INVC");
  }

  @Test
  @DisplayName("Should read a values-table field's ClaimFieldRow via the accessor")
  void shouldReadClaimField() {
    CrimeLowerClaimDetails details = new CrimeLowerClaimDetails();
    ClaimFieldRow travelCosts =
        new ClaimFieldRow(new BigDecimal("100.00"), new BigDecimal("110.00"), null);
    details.setTravelCosts(travelCosts);

    Object value = CrimeLowerClaimDetailsViewField.TRAVEL_COSTS.getAccessor().apply(details);

    assertThat(value).isEqualTo(travelCosts);
  }

  @Test
  @DisplayName("Value rows list should contain every values-table field, in order")
  void valueRowsShouldBeOrdered() {
    assertThat(CrimeClaimDetailsView.VALUE_ROWS)
        .containsExactly(
            ClaimViewField.asCrimeLowerField(ClaimDetailsViewField.FIXED_FEE),
            ClaimViewField.asCrimeLowerField(ClaimDetailsViewField.PROFIT_COSTS),
            ClaimViewField.asCrimeLowerField(ClaimDetailsViewField.DISBURSEMENTS),
            ClaimViewField.asCrimeLowerField(ClaimDetailsViewField.DISBURSEMENTS_VAT),
            CrimeLowerClaimDetailsViewField.TRAVEL_COSTS,
            CrimeLowerClaimDetailsViewField.WAITING_COSTS,
            ClaimViewField.asCrimeLowerField(ClaimDetailsViewField.VAT));
  }

  @Test
  @DisplayName("Total rows list should contain every total-table field, in order")
  void totalRowsShouldBeOrdered() {
    assertThat(CrimeClaimDetailsView.TOTAL_ROWS)
        .containsExactly(
            ClaimViewField.asCrimeLowerField(ClaimDetailsViewField.TOTAL_VAT),
            ClaimViewField.asCrimeLowerField(ClaimDetailsViewField.TOTAL_INCLUDING_VAT));
  }
}
