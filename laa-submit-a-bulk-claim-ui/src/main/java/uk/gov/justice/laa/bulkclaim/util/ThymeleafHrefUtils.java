package uk.gov.justice.laa.bulkclaim.util;

import java.util.StringJoiner;
import org.springframework.stereotype.Component;

/**
 * Utility class for building URLs with query parameters.
 *
 * @author Jamie Briggs
 */
@Component
public class ThymeleafHrefUtils {

  /**
   * Builds a URL with query parameters. Will not add the query parameter if the parameter value is
   * null or empty.
   *
   * @param baseUrl The base URL to append to.
   * @param params The key-value pairs of query parameters.
   * @return The built URL.
   */
  public String build(String baseUrl, String... params) {
    if (baseUrl == null || baseUrl.isEmpty()) {
      throw new IllegalArgumentException("Base URL cannot be null or empty");
    }
    if (params.length % 2 != 0) {
      throw new IllegalArgumentException("Parameters should be provided in key-value pairs");
    }

    StringJoiner queryJoiner = new StringJoiner("&");
    for (int i = 0; i < params.length; i += 2) {
      String key = params[i];
      String value = params[i + 1];

      if (value != null && !value.isEmpty()) {
        queryJoiner.add(key + "=" + value);
      }
    }

    String queryString = queryJoiner.toString();
    String appendSymbol = baseUrl.contains("?") ? "&" : "?";

    return queryString.isEmpty() ? baseUrl : baseUrl + appendSymbol + queryString;
  }
}
