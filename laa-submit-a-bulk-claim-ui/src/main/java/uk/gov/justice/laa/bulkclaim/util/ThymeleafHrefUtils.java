package uk.gov.justice.laa.bulkclaim.util;

import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class ThymeleafHrefUtils {

  public String build(String baseUrl, Object... params) {
    if (baseUrl == null || baseUrl.isEmpty()) {
      throw new IllegalArgumentException("Base URL cannot be null or empty");
    }
    if (params.length % 2 != 0) {
      throw new IllegalArgumentException("Parameters should be provided in key-value pairs");
    }

    UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(baseUrl);

    for (int i = 0; i < params.length; i += 2) {
      String key = params[i].toString();
      Object value = params[i + 1];

      if (value instanceof Iterable<?> values) {
        uriComponentsBuilder.replaceQueryParam(key);
        values.forEach(item -> addQueryParamValue(uriComponentsBuilder, key, item, true));
      } else {
        addQueryParamValue(uriComponentsBuilder, key, value, false);
      }
    }

    return uriComponentsBuilder.build().toUriString();
  }

  private void addQueryParamValue(
      UriComponentsBuilder uriComponentsBuilder, String key, Object value, boolean append) {
    if (value != null && !value.toString().isEmpty()) {
      if (append) {
        uriComponentsBuilder.queryParam(key, value);
      } else {
        uriComponentsBuilder.replaceQueryParam(key, value);
      }
    }
  }
}
