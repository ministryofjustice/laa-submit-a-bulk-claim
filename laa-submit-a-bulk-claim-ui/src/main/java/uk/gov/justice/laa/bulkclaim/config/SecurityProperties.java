package uk.gov.justice.laa.bulkclaim.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Security properties for the application.
 *
 * @author Jamie Briggs
 */
@Configuration
@ConfigurationProperties(prefix = "security")
@Getter
@Setter
public class SecurityProperties {
  private List<String> allowedHosts;
}
