package uk.gov.justice.laa.bulkclaim.dto.submission.search;

import java.util.Arrays;
import lombok.Getter;
import uk.gov.justice.laa.bulkclaim.dto.sorting.SortField;

@Getter
public enum SubmissionSearchSortField implements SortField {
  CREATED_ON("createdOn"),
  OFFICE_ACCOUNT_NUMBER("officeAccountNumber"),
  AREA_OF_LAW("areaOfLaw"),
  SUBMISSION_PERIOD("submissionPeriod"),
  STATUS("status");

  private final String value;

  SubmissionSearchSortField(String value) {
    this.value = value;
  }

  public static SubmissionSearchSortField fromValue(String value) {
    return Arrays.stream(values())
        .filter(sortField -> sortField.value.equals(value))
        .findFirst()
        .orElseThrow(IllegalArgumentException::new);
  }
}
