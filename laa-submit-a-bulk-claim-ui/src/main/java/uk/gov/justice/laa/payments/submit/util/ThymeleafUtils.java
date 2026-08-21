package uk.gov.justice.laa.payments.submit.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.payments.submit.viewmodels.ThymeleafLiteralString;
import uk.gov.justice.laa.payments.submit.viewmodels.ThymeleafMessage;
import uk.gov.justice.laa.payments.submit.viewmodels.ThymeleafString;

@Component
@AllArgsConstructor
public class ThymeleafUtils {

  private final CurrencyUtil currencyUtil;
  private final DateTimeUtil dateTimeUtil;

  public ThymeleafString getFormattedValue(Object value) {
    return switch (value) {
      case null -> new ThymeleafMessage("common.noData");
      case BigDecimal bigDecimal ->
          new ThymeleafLiteralString(currencyUtil.formatCurrency(bigDecimal));
      case Integer i -> new ThymeleafLiteralString(i.toString());
      case Boolean b -> getFormattedBoolean(b);
      case String s -> new ThymeleafLiteralString(s);
      case ThymeleafMessage s -> s;
      case OffsetDateTime o ->
          new ThymeleafMessage(
              "common.fulldate.format",
              dateTimeUtil.displayDateTimeDateValue(o),
              dateTimeUtil.displayDateTimeTimeValue(o));
      case LocalDate d -> new ThymeleafLiteralString(dateTimeUtil.displayDateValue(d));
      default -> new ThymeleafLiteralString(value.toString());
    };
  }

  public ThymeleafString getFormattedBoolean(Boolean value) {
    String key = (value != null && value) ? "common.yes" : "common.no";
    return new ThymeleafMessage(key);
  }
}
