package uk.gov.justice.laa.bulkclaim.config.rest;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class WebClientConfigurationTest {
  private ClaimsApiProperties claimsApiProperties;
  private WebClientConfiguration webClientConfiguration;

  @BeforeEach
  public void initialize() {
    claimsApiProperties = new ClaimsApiProperties("http://localhost", "dsd-dsd");
    webClientConfiguration = new WebClientConfiguration();
  }

  @Test
  @DisplayName("should create claims api V2 proxy")
  public void claimsApiClientV2() {

    var actualResults = webClientConfiguration.claimsApiClientV2(claimsApiProperties);
    assertFalse(Objects.isNull(actualResults));
  }

  @Test
  @DisplayName("should create claims api proxy")
  public void claimsApiClient() {
    var actualResults = webClientConfiguration.claimsApiClient(claimsApiProperties);
    assertFalse(Objects.isNull(actualResults));
  }
}
