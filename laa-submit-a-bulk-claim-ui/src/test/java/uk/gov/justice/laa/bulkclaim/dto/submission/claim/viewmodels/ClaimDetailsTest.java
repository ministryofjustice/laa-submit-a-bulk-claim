package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Claim details test")
class ClaimDetailsTest {

  private static final class TestClaimDetails extends ClaimDetails {}

  @Test
  @DisplayName("Should combine forename and surname into a client name")
  void shouldBuildClientName() {
    ClaimDetails claimDetails = new TestClaimDetails();
    claimDetails.setClientForename("client-forename");
    claimDetails.setClientSurname("client-surname");

    assertThat(claimDetails.clientName()).isEqualTo("client-forename client-surname");
  }
}
