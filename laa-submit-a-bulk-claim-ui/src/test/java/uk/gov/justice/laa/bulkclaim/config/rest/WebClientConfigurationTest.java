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
  @DisplayName("should return WebClientHttpServiceGroupConfigurer")
  void groupConfigurer() {
    var actualResults = webClientConfiguration.groupConfigurer(claimsApiProperties);
    assertFalse(Objects.isNull(actualResults));
  }
}
