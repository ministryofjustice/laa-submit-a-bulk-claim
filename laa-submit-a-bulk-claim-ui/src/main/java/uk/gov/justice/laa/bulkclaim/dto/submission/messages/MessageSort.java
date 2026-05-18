package uk.gov.justice.laa.bulkclaim.dto.submission.messages;

import static uk.gov.justice.laa.bulkclaim.dto.sorting.SortDirection.NONE;
import static uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessageSortField.MESSAGES;

import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.justice.laa.bulkclaim.dto.sorting.Sort;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class MessageSort extends Sort<MessageSortField> {

  public MessageSort(String sortString) {
    super(sortString);
  }

  public static MessageSort defaults() {
    return MessageSort.builder().field(MESSAGES).direction(NONE).build();
  }

  @Override
  public MessageSortField createField(String value) {
    return MessageSortField.fromValue(value);
  }
}
