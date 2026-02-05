package uk.gov.justice.laa.bulkclaim.util;

import java.time.LocalDate;
import org.springframework.stereotype.Component;

/**
 * Wrapper class for getting the current date. Helps decouple date logic from business logic, which
 * helps with testing.
 *
 * @author Jamie Briggs
 */
@Component
public class DateWrapperUtil {

  /**
   * Returns the current date.
   *
   * @return the current date
   */
  public LocalDate now() {
    return LocalDate.now();
  }
}
