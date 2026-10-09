package uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Claim field test")
class ClaimFieldRowTest {

  @Test
  @DisplayName("Should report whether each value is present")
  void shouldReportPresenceOfEachValue() {
    ClaimFieldRow claimFieldRow =
        new ClaimFieldRow("reported", "initial-calculated", "currentCalculated");

    assertThat(claimFieldRow.hasReportedValue()).isTrue();
    assertThat(claimFieldRow.hasInitialCalculatedValue()).isTrue();
    assertThat(claimFieldRow.hasCurrentCalculatedValue()).isTrue();
  }

  @Test
  @DisplayName("Should report absence when a value is null")
  void shouldReportAbsenceOfNullValues() {
    ClaimFieldRow claimFieldRow = new ClaimFieldRow(null, null, null);

    assertThat(claimFieldRow.hasReportedValue()).isFalse();
    assertThat(claimFieldRow.hasInitialCalculatedValue()).isFalse();
    assertThat(claimFieldRow.hasCurrentCalculatedValue()).isFalse();
  }

  @Test
  @DisplayName("Should expose the reported, initial calculated and currentCalculated values")
  void shouldExposeValues() {
    ClaimFieldRow claimFieldRow =
        new ClaimFieldRow("reported", "initial-calculated", "currentCalculated");

    assertThat(claimFieldRow.reported()).isEqualTo("reported");
    assertThat(claimFieldRow.initialCalculated()).isEqualTo("initial-calculated");
    assertThat(claimFieldRow.currentCalculated()).isEqualTo("currentCalculated");
  }
}
