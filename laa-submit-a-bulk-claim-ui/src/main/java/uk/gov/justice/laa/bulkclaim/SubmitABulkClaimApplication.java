package uk.gov.justice.laa.bulkclaim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class SubmitABulkClaimApplication {
  static void main(String[] args) {
    SpringApplication.run(SubmitABulkClaimApplication.class, args);
  }
}
