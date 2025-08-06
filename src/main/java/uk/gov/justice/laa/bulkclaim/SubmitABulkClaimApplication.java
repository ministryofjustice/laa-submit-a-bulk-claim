package uk.gov.justice.laa.bulkclaim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/** Entry point for the Submit a Bulk Claim application. */
@SpringBootApplication
@EnableCaching
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class SubmitABulkClaimApplication {

  /**
   * The application main method.
   *
   * @param args the application arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(SubmitABulkClaimApplication.class, args);
  }
}
