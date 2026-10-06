package uk.gov.justice.laa.payments.submit.config;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;
import uk.gov.justice.laa.payments.submit.metrics.BulkClaimMetricService;
import uk.gov.justice.laa.payments.submit.util.BulkLoadSpreadsheetFilenameUtil;

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
  BulkLoadSpreadsheetFilenameUtil bulkLoadSpreadsheetFilenameUtil() {
    return new BulkLoadSpreadsheetFilenameUtil();
  }

  @Bean
  BulkClaimMetricService bulkClaimMetricService(
      PrometheusRegistry prometheusRegistry,
      BulkLoadSpreadsheetFilenameUtil bulkLoadSpreadsheetFilenameUtil) {
    return new BulkClaimMetricService(prometheusRegistry, bulkLoadSpreadsheetFilenameUtil);
  }

  @Primary
  @Bean
  CacheManager cacheManager() {
    return Mockito.mock(CacheManager.class);
  }
}
