package uk.gov.justice.laa.payments.submit.dto.sorting;

public interface SortField {
  String getValue();

  default SortDirection getDirection(Sort<?> sort) {
    if (sort != null && equals(sort.getField())) {
      return sort.getDirection();
    }
    return SortDirection.NONE;
  }
}
