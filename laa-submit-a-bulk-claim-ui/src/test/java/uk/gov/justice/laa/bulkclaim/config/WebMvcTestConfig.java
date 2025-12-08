package uk.gov.justice.laa.bulkclaim.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import uk.gov.justice.laa.bulkclaim.util.CurrencyUtil;

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

  /**
   * Creates a CurrencyUtil bean. Ensuring bean is called correct due to it's usage in thymeleaf.
   *
   * @return a CurrencyUtil bean.
   */
  @Bean(name = "currencyUtil")
  CurrencyUtil currencyUtil() {
    return new CurrencyUtil();
  }
}
