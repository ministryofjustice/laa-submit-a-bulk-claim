package uk.gov.justice.laa.bulkclaim.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class DateTimeUtil {

  private static final String DEFAULT_DATE_FORMAT = "d MMMM yyyy";
  public static final String DEFAULT_TIME_FORMAT = "h:mma";

  private static final ZoneId LONDON_TIMEZONE = ZoneId.of("Europe/London");

  public LocalDateTime toLondonLocalDateTime(OffsetDateTime value) {
    return value.atZoneSameInstant(LONDON_TIMEZONE).toLocalDateTime();
  }

  public String displayDateTimeDateValue(OffsetDateTime value) {
    return value != null
        ? displayDateTimeValue(toLondonLocalDateTime(value), DEFAULT_DATE_FORMAT)
        : null;
  }

  public String displayDateTimeTimeValue(OffsetDateTime value) {
    return value != null
        ? displayDateTimeValue(toLondonLocalDateTime(value), DEFAULT_TIME_FORMAT).toLowerCase()
        : null;
  }

  private static String displayDateTimeValue(LocalDateTime value, String format) {
    return value != null ? value.format(DateTimeFormatter.ofPattern(format)) : null;
  }

  public String displayDateValue(LocalDate value) {
    return value != null ? value.format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)) : null;
  }
}
