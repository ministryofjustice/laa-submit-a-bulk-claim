package uk.gov.justice.laa.bulkclaim.config;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;
import uk.gov.justice.laa.bulkclaim.metrics.BulkClaimMetricService;
import uk.gov.justice.laa.bulkclaim.util.CurrencyUtil;
import uk.gov.justice.laa.bulkclaim.util.DateTimeUtil;
import uk.gov.justice.laa.bulkclaim.util.DateWrapperUtil;
import uk.gov.justice.laa.bulkclaim.util.ThymeleafHrefUtils;
import uk.gov.justice.laa.bulkclaim.util.ThymeleafUtils;

@TestConfiguration
public class WebMvcTestConfig {

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

  @Bean
  ThymeleafHrefUtils thymeleafHrefUtils() {
    return new ThymeleafHrefUtils();
  }

  @Bean
  DateWrapperUtil dateWrapperUtil() {
    return new DateWrapperUtil();
  }

  @Bean
  CurrencyUtil currencyUtil() {
    return new CurrencyUtil();
  }

  @Bean
  DateTimeUtil dateTimeUtil() {
    return new DateTimeUtil();
  }

  @Bean
  ThymeleafUtils thymeleafUtils(CurrencyUtil currencyUtil, DateTimeUtil dateTimeUtil) {
    return new ThymeleafUtils(currencyUtil, dateTimeUtil);
  }

  /** This disables the host header handling filter for tests. */
  @Bean
  public HostValidationFilter hostValidationFilter() {
    return new HostValidationFilter(null) {
      @Override
      protected void doFilterInternal(
          jakarta.servlet.http.HttpServletRequest request,
          jakarta.servlet.http.HttpServletResponse response,
          jakarta.servlet.FilterChain filterChain)
          throws jakarta.servlet.ServletException, java.io.IOException {
        filterChain.doFilter(request, response);
      }
    };
  }
}
