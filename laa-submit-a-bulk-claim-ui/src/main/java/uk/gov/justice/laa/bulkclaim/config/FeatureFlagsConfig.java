package uk.gov.justice.laa.bulkclaim.config;

import static java.lang.Boolean.TRUE;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Data
@Configuration
@ConfigurationProperties(prefix = "feature-flags")
public class FeatureFlagsConfig {
  private Boolean isNilSubmissionEnabled;

  public void checkNilSubmissionEnabled() {
    if (!TRUE.equals(getIsNilSubmissionEnabled())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "isNilSubmissionEnabled is false");
    }
  }
}
