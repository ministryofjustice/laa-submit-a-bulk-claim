package uk.gov.justice.laa.bulkclaim.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("URL Service Tests")
class UrlServiceTest {

  public static final String URL = "https://localhost:8082";
  UrlService urlService;

  @BeforeEach
  void beforeEach() {
    urlService = new UrlService(URL);
  }

  @Test
  @DisplayName("Should build url")
  void shouldBuildUrl() {
    // Given
    String path = "hello-page";
    // When
    String result = urlService.buildAbsoluteUrl(path);
    // Then
    assertThat(result).isEqualTo(URL + "/hello-page");
  }

  @Test
  @DisplayName("Should build url whilst cleaning additional forward slash")
  void shouldBuildUrlWhilstCleaningAdditionalForwardSlash() {
    // Given
    String path = "/hello-page";
    // When
    String result = urlService.buildAbsoluteUrl(path);
    // Then
    assertThat(result).isEqualTo(URL + "/hello-page");
  }
}
