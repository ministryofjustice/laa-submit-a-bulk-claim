package uk.gov.justice.laa.bulkclaim.dto.sorting;

import lombok.Getter;

@Getter
public enum SortDirection {
  ASCENDING("asc", "ascending"),
  DESCENDING("desc", "descending"),
  NONE(null, "none");

  private final String value;
  private final String aria;

  SortDirection(String value, String aria) {
    this.value = value;
    this.aria = aria;
  }

  public SortDirection toggle() {
    return switch (this) {
      case ASCENDING -> DESCENDING;
      case DESCENDING, NONE -> ASCENDING;
    };
  }

  public static SortDirection fromValue(String value) {
    return switch (value) {
      case "asc" -> SortDirection.ASCENDING;
      case "desc" -> SortDirection.DESCENDING;
      default -> throw new IllegalArgumentException("Could not parse sort direction: " + value);
    };
  }
}
