package uk.gov.justice.laa.bulkclaim.config;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;
import uk.gov.justice.laa.bulkclaim.metrics.BulkClaimMetricService;

@TestConfiguration
public class ClaimsApiPactTestConfig {

  @Bean
  RestClient.Builder restClientBuilder() {
    return RestClient.builder();
  }

  @Bean
  PrometheusRegistry prometheusRegistry() {
    return Mockito.mock(PrometheusRegistry.class);
  }

  @Bean
  BulkClaimMetricService bulkClaimMetricService(PrometheusRegistry prometheusRegistry) {
    return new BulkClaimMetricService(prometheusRegistry);
  }

  @Primary
  @Bean
  CacheManager cacheManager() {
    return Mockito.mock(CacheManager.class);
  }
}
