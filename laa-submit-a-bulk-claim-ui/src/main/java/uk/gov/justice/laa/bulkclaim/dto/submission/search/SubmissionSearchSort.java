package uk.gov.justice.laa.bulkclaim.dto.submission.search;

import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.justice.laa.bulkclaim.dto.sorting.Sort;
import uk.gov.justice.laa.bulkclaim.dto.sorting.SortDirection;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SubmissionSearchSort extends Sort<SubmissionSearchSortField> {

  public SubmissionSearchSort(String sortString) {
    super(sortString);
  }

  public static SubmissionSearchSort defaults() {
    return builder()
        .field(SubmissionSearchSortField.CREATED_ON)
        .direction(SortDirection.DESCENDING)
        .build();
  }

  @Override
  public SubmissionSearchSortField createField(String value) {
    return SubmissionSearchSortField.fromValue(value);
  }
}
