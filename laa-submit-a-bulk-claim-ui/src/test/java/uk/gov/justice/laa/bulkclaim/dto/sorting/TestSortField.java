package uk.gov.justice.laa.bulkclaim.dto.sorting;

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
