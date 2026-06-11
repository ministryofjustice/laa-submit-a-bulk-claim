package uk.gov.justice.laa.bulkclaim.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.justice.laa.bulkclaim.service.SessionService;

/** Config class for metrics. */
@Configuration
public class MetricsConfig {

  /**
   * Creates a gauge that tracks the number of active sessions.
   *
   * @param registry for the meter registry used to register the gauge
   * @param sessionService the service providing session count
   * @return a gauge representing active sessions
   */
  @Bean
  public Gauge activeSessionsGauge(MeterRegistry registry, SessionService sessionService) {
    return Gauge.builder(
            "active.sessions.count", sessionService, SessionService::getActiveSessionCount)
        .description("Number of active sessions")
        .register(registry);
  }
}
