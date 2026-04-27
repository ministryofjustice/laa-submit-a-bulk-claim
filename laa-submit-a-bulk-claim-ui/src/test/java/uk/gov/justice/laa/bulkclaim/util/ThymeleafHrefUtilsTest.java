package uk.gov.justice.laa.bulkclaim.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Thymeleaf Href Utils Test")
class ThymeleafHrefUtilsTest {

  ThymeleafHrefUtils thymeleafHrefUtils = new ThymeleafHrefUtils();

  @ParameterizedTest
  @ValueSource(strings = {"/", "/test", "/test/test"})
  @DisplayName("Should add no request params")
  void shouldAddNoRequestParams(String href) {
    // Given
    // When
    String result = thymeleafHrefUtils.build(href);
    // Then
    assertThat(result).isEqualTo(href);
  }

  @ParameterizedTest
  @ValueSource(strings = {"/", "/test", "/test/test"})
  @DisplayName("Should add no request params")
  void shouldAddNoRequestParamsWhenValuesEmpty(String href) {
    // Given
    // When
    String result = thymeleafHrefUtils.build(href, "param", "", "paramTwo", "", "paramThree", "");
    // Then
    assertThat(result).isEqualTo(href);
  }

  @ParameterizedTest
  @ValueSource(strings = {"/", "/test", "/test/test"})
  @DisplayName("Should add only one request param")
  void shouldAddOnlyOneRequestParam(String href) {
    // Given
    // When
    String result =
        thymeleafHrefUtils.build(href, "param", "", "paramTwo", "has-a-value", "paramThree", "");
    // Then
    assertThat(result).isEqualTo(href + "?paramTwo=has-a-value");
  }

  @ParameterizedTest
  @CsvSource({"/,?", "/test?param1=value,&", "/test/test,?"})
  @DisplayName("Should add only one request param")
  void shouldAddMultipleRequestParams(String href, String appendSymbol) {
    // Given
    // When
    String result =
        thymeleafHrefUtils.build(
            href, "param", "value1", "paramTwo", "value2", "paramThree", "value3");
    // Then
    assertThat(result)
        .isEqualTo(href + appendSymbol + "param=value1&paramTwo=value2&paramThree=value3");
  }

  @Test
  @DisplayName("Should add non-string and repeated request params")
  void shouldAddNonStringAndRepeatedRequestParams() {
    // Given
    String href = "/test";

    // When
    String result =
        thymeleafHrefUtils.build(href, "page", 2, "offices", List.of("ABC123", "DEF456"));

    // Then
    assertThat(result).isEqualTo("/test?page=2&offices=ABC123&offices=DEF456");
  }
}
