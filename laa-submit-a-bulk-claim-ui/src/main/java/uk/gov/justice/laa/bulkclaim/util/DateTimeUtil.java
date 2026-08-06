package uk.gov.justice.laa.bulkclaim.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Service;

@Service
public class DateTimeUtil {

  private static final ZoneId LONDON_TIMEZONE = ZoneId.of("Europe/London");

  public LocalDateTime toLondonLocalDateTime(OffsetDateTime value) {
    return value.atZoneSameInstant(LONDON_TIMEZONE).toLocalDateTime();
  }
}
