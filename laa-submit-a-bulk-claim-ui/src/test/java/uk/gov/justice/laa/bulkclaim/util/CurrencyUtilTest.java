package uk.gov.justice.laa.bulkclaim.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Currency util tests")
class CurrencyUtilTest {

  private final CurrencyUtil currencyUtil = new CurrencyUtil();

  @Test
  @DisplayName("Should return zero")
  void shouldReturnZero() {
    // Given
    BigDecimal zero = BigDecimal.ZERO;
    // Then
    assertThat(currencyUtil.toString(zero)).isEqualTo("£0.00");
  }

  @Test
  @DisplayName("Should return 5p")
  void shouldReturn5pence() {
    // Given
    BigDecimal input = new BigDecimal("0.05");
    // Then
    assertThat(currencyUtil.toString(input)).isEqualTo("£0.05");
  }

  @Test
  @DisplayName("Should return £1,000.50")
  void shouldReturn1000_05() {
    // Given
    BigDecimal input = new BigDecimal("1000.50");
    // Then
    assertThat(currencyUtil.toString(input)).isEqualTo("£1,000.50");
  }

  @Test
  @DisplayName("Should return £9,876,543,210.99")
  void shouldReturnMillions() {
    // Given
    BigDecimal input = new BigDecimal("9876543210.99");
    // Then
    assertThat(currencyUtil.toString(input)).isEqualTo("£9,876,543,210.99");
  }
}
