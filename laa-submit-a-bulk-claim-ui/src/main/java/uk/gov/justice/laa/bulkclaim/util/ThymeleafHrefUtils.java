package uk.gov.justice.laa.bulkclaim.util;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

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

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(baseUrl);

    for (int i = 0; i < params.length; i += 2) {
      String key = params[i];
      String value = params[i + 1];

      if (value != null && !value.isEmpty()) {
        uriComponentsBuilder.replaceQueryParam(key, value);
      }
    }

    return uriComponentsBuilder.build().toUriString();
  }

  /**
   * Removes a request parameter value from the provider servlet path.
   *
   * @param servletPath object containing servlet path and query params
   * @param key the request parameter key
   * @param valueToRemove the request parameter value to remove
   * @return a built URL
   */
  public String removeQueryParamValue(
      ViewRequestContext servletPath, String key, String valueToRemove) {
    UriBuilder uriBuilder = UriComponentsBuilder.fromUriString(servletPath.servletPath());
    servletPath
        .queryParams()
        .forEach(
            (k, v) -> {
              if (k.equals(key)) {
                List<String> list = v.stream().filter(s -> !s.equals(valueToRemove)).toList();
                if (!list.isEmpty()) {
                  uriBuilder.queryParam(k, list);
                }
              } else {
                uriBuilder.queryParam(k, v);
              }
            });
    return uriBuilder.toUriString();
  }

  /**
   * Represents the context of a view request, which includes the servlet path and query parameters
   * associated with the request.
   *
   * <p>This record is typically used to encapsulate essential data for processing or building URLs
   * for HTTP requests and responses within the application.
   */
  public record ViewRequestContext(String servletPath, MultiValueMap<String, String> queryParams) {}
}
