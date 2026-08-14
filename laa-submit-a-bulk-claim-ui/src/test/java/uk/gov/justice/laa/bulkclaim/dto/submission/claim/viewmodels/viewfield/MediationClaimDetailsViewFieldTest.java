package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.viewfield;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.MediationClaimDetails;
import uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails.MediationClaimDetailsView;

@DisplayName("Mediation claim details view field test")
class MediationClaimDetailsViewFieldTest {

  @Test
  @DisplayName("Should read client 1's name via the shared clientName() accessor")
  void shouldReadClient1Name() {
    MediationClaimDetails details = new MediationClaimDetails();
    details.setClientForename("Jane");
    details.setClientSurname("Doe");

    Object value = MediationClaimDetailsViewField.CLIENT_1_NAME.getAccessor().apply(details);

    assertThat(value).isEqualTo("Jane Doe");
  }

  @Test
  @DisplayName("Should read client 1's unique client number, which is mediation-specific")
  void shouldReadClient1Ucn() {
    MediationClaimDetails details = new MediationClaimDetails();
    details.setUniqueClientNumber("02122002/S/JENK");

    Object value = MediationClaimDetailsViewField.CLIENT_1_UCN.getAccessor().apply(details);

    assertThat(value).isEqualTo("02122002/S/JENK");
  }

  @Test
  @DisplayName("Should return null for client 2's name when neither forename nor surname is set")
  void shouldReturnNullClient2NameWhenAbsent() {
    MediationClaimDetails details = new MediationClaimDetails();

    Object value = MediationClaimDetailsViewField.CLIENT_2_NAME.getAccessor().apply(details);

    assertThat(value).isNull();
  }

  @Test
  @DisplayName("Value rows list should contain every values-table field, in order")
  void valueRowsShouldBeOrdered() {
    assertThat(MediationClaimDetailsView.VALUE_ROWS)
        .containsExactly(
            ClaimViewField.asMediationField(ClaimDetailsViewField.FIXED_FEE),
            ClaimViewField.asMediationField(ClaimDetailsViewField.DISBURSEMENTS),
            ClaimViewField.asMediationField(ClaimDetailsViewField.DISBURSEMENTS_VAT),
            ClaimViewField.asMediationField(ClaimDetailsViewField.VAT));
  }

  @Test
  @DisplayName("Total rows list should contain every total-table field, in order")
  void totalRowsShouldBeOrdered() {
    assertThat(MediationClaimDetailsView.TOTAL_ROWS)
        .containsExactly(
            ClaimViewField.asMediationField(ClaimDetailsViewField.TOTAL_VAT),
            ClaimViewField.asMediationField(ClaimDetailsViewField.TOTAL_INCLUDING_VAT));
  }
}
