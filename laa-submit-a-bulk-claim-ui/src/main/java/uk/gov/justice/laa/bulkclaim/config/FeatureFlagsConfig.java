package uk.gov.justice.laa.bulkclaim.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "feature-flags")
public class FeatureFlagsConfig {
  private Boolean isNilSubmissionEnabled;
}
