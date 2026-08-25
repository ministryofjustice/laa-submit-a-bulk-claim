package uk.gov.justice.laa.payments.submit.dto.sorting;

public record TestSortField(String value) implements SortField {
  @Override
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
}
