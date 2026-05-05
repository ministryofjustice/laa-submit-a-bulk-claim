package uk.gov.justice.laa.bulkclaim.config;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import uk.gov.justice.laa.bulkclaim.metrics.BulkClaimMetricService;
import uk.gov.justice.laa.bulkclaim.util.CurrencyUtil;

@TestConfiguration
public class ClaimsApiPactTestConfig {

  @Bean
  RestClient.Builder restClientBuilder() {
    return RestClient.builder();
  }

  /**
   * Creates a CurrencyUtil bean. Ensuring bean is named correctly due to its usage in thymeleaf.
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
