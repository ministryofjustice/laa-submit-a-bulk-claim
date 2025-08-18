package uk.gov.justice.laa.bulkclaim.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import org.springframework.stereotype.Service;

/**
 * Utility class for formatting currency values.
 *
 * @author Jamie Briggs
 */
@Service
public class CurrencyUtil {

  /**
   * Formats a BigDecimal value to a currency string.
   *
   * @param amount the amount to be formatted
   * @return the formatted currency string
   */
  public String toString(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
      return "£0.00";
    }
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.UK);
    return currencyFormat.format(amount);
  }
}
