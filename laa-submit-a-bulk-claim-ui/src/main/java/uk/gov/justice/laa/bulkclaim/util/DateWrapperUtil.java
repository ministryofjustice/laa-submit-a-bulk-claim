package uk.gov.justice.laa.bulkclaim.util;

import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class DateWrapperUtil {
  public LocalDate now() {
    return LocalDate.now();
  }
}
