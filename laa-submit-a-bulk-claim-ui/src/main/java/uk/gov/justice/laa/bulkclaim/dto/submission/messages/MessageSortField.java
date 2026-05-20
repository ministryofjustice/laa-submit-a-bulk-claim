package uk.gov.justice.laa.bulkclaim.dto.submission.messages;

import java.util.Arrays;
import lombok.Getter;
import uk.gov.justice.laa.bulkclaim.dto.sorting.SortField;

@Getter
public enum MessageSortField implements SortField {
  CLIENT_SURNAME("client_surname"),
  CLIENT_FORENAME("client_forename"),
  UNIQUE_FILE_NUMBER("unique_file_number"),
  UNIQUE_CLIENT_NUMBER("unique_client_number"),
  MESSAGES("display_message"),
  CLIENT_2_SURNAME("client_2_surname"),
  CLIENT_2_FORENAME("client_2_forename"),
  CLIENT_2_UCN("client_2_ucn");

  private final String value;

  MessageSortField(String value) {
    this.value = value;
  }

  public static MessageSortField fromValue(String value) {
    return Arrays.stream(values())
        .filter(sortField -> sortField.value.equals(value))
        .findFirst()
        .orElseThrow(IllegalArgumentException::new);
  }
}
