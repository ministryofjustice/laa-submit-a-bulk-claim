package uk.gov.justice.laa.bulkclaim.config.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.claims-api")
public class ClaimsApiProperties extends ApiProperties {

  public ClaimsApiProperties(String url, String accessToken) {
    super(url, accessToken);
  }
}
