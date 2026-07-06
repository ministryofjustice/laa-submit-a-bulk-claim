package uk.gov.justice.laa.bulkclaim.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import org.springframework.stereotype.Component;

@Component
public class DateWrapperUtil {

  public LocalDate now() {
    return LocalDate.now();
  }

  public LocalDateTime timeNow() {
    return LocalDateTime.now();
  }

  public YearMonth nowYearMonth() {
    return YearMonth.now();
  }
}
