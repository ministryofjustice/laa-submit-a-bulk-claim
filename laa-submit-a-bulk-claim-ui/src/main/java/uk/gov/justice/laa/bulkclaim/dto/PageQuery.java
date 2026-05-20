package uk.gov.justice.laa.bulkclaim.dto;

import static org.springframework.util.StringUtils.hasText;

import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.laa.bulkclaim.dto.sorting.Sort;
import uk.gov.justice.laa.bulkclaim.dto.sorting.SortDirection;
import uk.gov.justice.laa.bulkclaim.dto.sorting.SortField;

public interface PageQuery<T extends SortField, U extends Sort<T>> {

  int DEFAULT_PAGE = 0;
  int DEFAULT_PAGE_SIZE = 10;

  Integer getPage();

  U getSort();

  String getRedirectUrl(T sortField, SortDirection direction);

  String getRedirectUrl(int page, U sort);

  default String getRedirectUrl() {
    return getRedirectUrl(getPage(), getSort());
  }

  default Integer getSize() {
    return DEFAULT_PAGE_SIZE;
  }

  default void addQueryParam(UriComponentsBuilder builder, String key, Object value) {
    if (value instanceof String valueString && !hasText(valueString)) {
      return;
    }
    builder.queryParam(key, value);
  }
}
