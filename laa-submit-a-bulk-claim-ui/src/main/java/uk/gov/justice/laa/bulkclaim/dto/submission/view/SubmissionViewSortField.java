package uk.gov.justice.laa.bulkclaim.dto.submission.view;

import java.util.Arrays;
import lombok.Getter;
import uk.gov.justice.laa.bulkclaim.dto.sorting.SortField;

@Getter
public enum SubmissionViewSortField implements SortField {
  CLIENT_SURNAME("client_surname"),
  CLIENT_FORENAME("client_forename"),
  UNIQUE_FILE_NUMBER("unique_file_number"),
  FEE_CODE("fee_code"),
  CASE_CONCLUDED_DATE("case_concluded_date"),
  TOTAL_AMOUNT("total_amount"),
  ESCAPE_CASE_FLAG("escape_case_flag"),
  TOTAL_WARNINGS("total_warnings"),
  UNIQUE_CLIENT_NUMBER("unique_client_number"),
  CLIENT_2_SURNAME("client_2_surname"),
  CLIENT_2_FORENAME("client_2_forename"),
  CLIENT_2_UCN("client_2_ucn"),
  LINE_NUMBER("line_number");

  private final String value;

  SubmissionViewSortField(String value) {
    this.value = value;
  }

  public static SubmissionViewSortField fromValue(String value) {
    return Arrays.stream(values())
        .filter(sortField -> sortField.value.equals(value))
        .findFirst()
        .orElseThrow(IllegalArgumentException::new);
  }
}
