package uk.gov.justice.laa.bulkclaim.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class CurrencyUtil {

  public String toString(BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
      return "£0.00";
    }
    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.UK);
    return currencyFormat.format(amount);
  }
}
