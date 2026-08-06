package uk.gov.justice.laa.bulkclaim.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Date util tests")
class DateUtilTest {

  @Test
  @DisplayName("Should return null for null input")
  void shouldReturnNullForNullInput() {
    assertThat(DateUtil.toLondonOffsetDateTime(null)).isNull();
  }

  @Test
  @DisplayName("Should keep GMT offset in winter")
  void shouldKeepGmtOffsetInWinter() {
    OffsetDateTime utcInput = OffsetDateTime.of(2025, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC);

    OffsetDateTime result = DateUtil.toLondonOffsetDateTime(utcInput);

    assertThat(result).isEqualTo(OffsetDateTime.of(2025, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC));
  }

  @Test
  @DisplayName("Should apply BST offset in summer")
  void shouldApplyBstOffsetInSummer() {
    OffsetDateTime utcInput = OffsetDateTime.of(2025, 7, 15, 10, 0, 0, 0, ZoneOffset.UTC);

    OffsetDateTime result = DateUtil.toLondonOffsetDateTime(utcInput);

    assertThat(result)
        .isEqualTo(OffsetDateTime.of(2025, 7, 15, 11, 0, 0, 0, ZoneOffset.ofHours(1)));
  }
}
