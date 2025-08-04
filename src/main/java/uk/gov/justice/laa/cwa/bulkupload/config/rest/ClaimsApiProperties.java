package uk.gov.justice.laa.cwa.bulkupload.config.rest;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties specific to the Provider Details API.
 *
 * @author Jamie Briggs
 */
@ConfigurationProperties(prefix = "claims-api")
public class ClaimsApiProperties extends ApiProperties {

  public ClaimsApiProperties(String url, String accessToken) {
    super(url, accessToken);
  }
}
