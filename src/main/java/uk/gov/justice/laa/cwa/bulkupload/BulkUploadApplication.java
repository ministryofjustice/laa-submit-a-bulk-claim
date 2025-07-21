package uk.gov.justice.laa.cwa.bulkupload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/** Entry point for the Bulk Upload application. */
@SpringBootApplication
@EnableCaching
public class BulkUploadApplication {

  /**
   * The application main method.
   *
   * @param args the application arguments.
   */
  public static void main(String[] args) {
    SpringApplication.run(BulkUploadApplication.class, args);
  }
}
