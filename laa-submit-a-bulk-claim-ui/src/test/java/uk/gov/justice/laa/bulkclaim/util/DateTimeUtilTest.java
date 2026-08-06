package uk.gov.justice.laa.bulkclaim.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Date time util tests")
class DateTimeUtilTest {

  @Test
  @DisplayName("Should convert UTC submitted time to London time during BST")
  void shouldConvertUtcToLondonLocalDateTime() {
    OffsetDateTime utcDateTime = OffsetDateTime.of(2025, 7, 1, 10, 10, 10, 0, ZoneOffset.UTC);

    LocalDateTime result = DateTimeUtil.toLondonLocalDateTime(utcDateTime);

    assertThat(result).isEqualTo(LocalDateTime.of(2025, 7, 1, 11, 10, 10));
  }

  @Test
  @DisplayName("Should convert UTC submitted time to London time during GMT")
  void shouldConvertUtcToLondonLocalDateTimeInWinter() {
    OffsetDateTime utcDateTime = OffsetDateTime.of(2025, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC);

    LocalDateTime result = DateTimeUtil.toLondonLocalDateTime(utcDateTime);

    assertThat(result).isEqualTo(LocalDateTime.of(2025, 1, 15, 10, 0, 0));
  }
}
