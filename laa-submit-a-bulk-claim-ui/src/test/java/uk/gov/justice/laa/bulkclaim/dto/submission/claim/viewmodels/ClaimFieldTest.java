package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Claim field test")
class ClaimFieldTest {

  @Test
  @DisplayName("Should report whether each value is present")
  void shouldReportPresenceOfEachValue() {
    ClaimField claimField = new ClaimField("reported", "initial-calculated", "assessed");

    assertThat(claimField.hasReportedValue()).isTrue();
    assertThat(claimField.hasInitialCalculatedValue()).isTrue();
    assertThat(claimField.hasAssessedValue()).isTrue();
  }

  @Test
  @DisplayName("Should report absence when a value is null")
  void shouldReportAbsenceOfNullValues() {
    ClaimField claimField = new ClaimField(null, null, null);

    assertThat(claimField.hasReportedValue()).isFalse();
    assertThat(claimField.hasInitialCalculatedValue()).isFalse();
    assertThat(claimField.hasAssessedValue()).isFalse();
  }

  @Test
  @DisplayName("Should expose the reported, initial calculated and assessed values")
  void shouldExposeValues() {
    ClaimField claimField = new ClaimField("reported", "initial-calculated", "assessed");

    assertThat(claimField.reported()).isEqualTo("reported");
    assertThat(claimField.initialCalculated()).isEqualTo("initial-calculated");
    assertThat(claimField.assessed()).isEqualTo("assessed");
  }
}
