package uk.gov.justice.laa.bulkclaim.config;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;
import uk.gov.justice.laa.bulkclaim.metrics.BulkClaimMetricService;
import uk.gov.justice.laa.bulkclaim.util.CurrencyUtil;
import uk.gov.justice.laa.bulkclaim.validation.BulkImportFileValidator;

/**
 * Test configuration for Pact tests. Handles creating beans which otherwise would not be
 * automatically created.
 */
@TestConfiguration
public class ClaimsApiPactTestConfig {

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

  @Bean
  PrometheusRegistry prometheusRegistry() {
    return Mockito.mock(PrometheusRegistry.class);
  }

  @Bean
  BulkClaimMetricService bulkClaimMetricService(PrometheusRegistry prometheusRegistry) {
    return new BulkClaimMetricService(prometheusRegistry);
  }

  @Bean
  CacheManager cacheManager() {
    return Mockito.mock(CacheManager.class);
  }


}
