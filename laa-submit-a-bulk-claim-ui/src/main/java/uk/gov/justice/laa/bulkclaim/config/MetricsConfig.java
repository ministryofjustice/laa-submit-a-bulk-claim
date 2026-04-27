package uk.gov.justice.laa.bulkclaim.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.justice.laa.bulkclaim.service.SessionService;

@Configuration
public class MetricsConfig {

  @Bean
  public Gauge activeSessionsGauge(MeterRegistry registry, SessionService sessionService) {
    return Gauge.builder(
            "active.sessions.count", sessionService, SessionService::getActiveSessionCount)
        .description("Number of active sessions")
        .register(registry);
  }
}
