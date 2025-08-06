package uk.gov.justice.laa.cwa.bulkupload.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

/**
 * Test configuration for Spring MVC tests. Handles creating beans which otherwise would not be
 * automatically created.
 */
@TestConfiguration
public class WebMvcTestConfig {

  /**
   * Creates a RestClient.Builder bean.
   *
   * @return a RestClient.Builder bean.
   */
  @Bean
  RestClient.Builder restClientBuilder() {
    return RestClient.builder();
  }
}
