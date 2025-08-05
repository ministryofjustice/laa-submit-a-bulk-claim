package uk.gov.justice.laa.cwa.bulkupload.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@TestConfiguration
public class WebMvcTestConfig {

  // Needed for RestClient to work in tests
  @Bean
  RestClient.Builder restClientBuilder() {
    return RestClient.builder();
  }
}
