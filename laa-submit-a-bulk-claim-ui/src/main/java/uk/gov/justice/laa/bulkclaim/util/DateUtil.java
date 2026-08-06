package uk.gov.justice.laa.bulkclaim.util;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public class DateUtil {
  private static final ZoneId LONDON_TIMEZONE = ZoneId.of("Europe/London");

  public static OffsetDateTime toLondonOffsetDateTime(OffsetDateTime value) {
    if (value == null) {
      return null;
    }
    return value.atZoneSameInstant(LONDON_TIMEZONE).toOffsetDateTime();
  }
}
