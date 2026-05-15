package uk.gov.justice.laa.bulkclaim.dto.sorting;

public interface SortField {
  String getValue();

  default SortDirection getDirection(Sort<?> sort) {
    if (sort != null && sort.getField().equals(this)) {
      return sort.getDirection();
    }
    return SortDirection.NONE;
  }
}
