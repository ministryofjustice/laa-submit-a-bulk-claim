package uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.MediationClaimDetails;

@DisplayName("Mediation claim case view test")
class MediationClaimDetailsViewTest {

  @Test
  @DisplayName("getClient1Name()/getClient1Ucn() should read client 1's details")
  void shouldReadClient1Details() {
    MediationClaimDetails details = new MediationClaimDetails();
    details.setClientForename("Sally");
    details.setClientSurname("Jenkins");
    details.setUniqueClientNumber("02122002/S/JENK");

    MediationClaimDetailsView view = new MediationClaimDetailsView(details);

    assertThat(view.getClient1Name()).isEqualTo("Sally Jenkins");
    assertThat(view.getClient1Ucn()).isEqualTo("02122002/S/JENK");
  }

  @Test
  @DisplayName("getClient2Name()/getClient2Ucn() should read client 2's details when present")
  void shouldReadClient2DetailsWhenPresent() {
    MediationClaimDetails details = new MediationClaimDetails();
    details.setClientForename("Sally");
    details.setClientSurname("Jenkins");
    details.setUniqueClientNumber("02122002/S/JENK");
    details.setClient2Forename("John");
    details.setClient2Surname("Smith");
    details.setClient2UniqueClientNumber("02122002/J/SMIT");

    MediationClaimDetailsView view = new MediationClaimDetailsView(details);

    assertThat(view.getClient2Name()).isEqualTo("John Smith");
    assertThat(view.getClient2Ucn()).isEqualTo("02122002/J/SMIT");
  }

  @Test
  @DisplayName(
      "getClient2Name()/getClient2Ucn() should return null rather than throw when"
          + " client 2 is absent")
  void shouldReturnNullClient2DetailsWhenAbsent() {
    MediationClaimDetails details = new MediationClaimDetails();
    details.setClientForename("Sally");
    details.setClientSurname("Jenkins");
    details.setUniqueClientNumber("02122002/S/JENK");

    MediationClaimDetailsView view = new MediationClaimDetailsView(details);

    assertThat(view.getClient2Name()).isNull();
    assertThat(view.getClient2Ucn()).isNull();
  }
}
