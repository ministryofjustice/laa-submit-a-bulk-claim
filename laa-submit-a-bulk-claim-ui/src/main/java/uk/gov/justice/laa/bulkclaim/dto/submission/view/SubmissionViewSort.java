package uk.gov.justice.laa.bulkclaim.dto.submission.view;

import static uk.gov.justice.laa.bulkclaim.dto.sorting.SortDirection.ASCENDING;
import static uk.gov.justice.laa.bulkclaim.dto.submission.view.SubmissionViewSortField.LINE_NUMBER;

import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import uk.gov.justice.laa.bulkclaim.dto.sorting.Sort;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SubmissionViewSort extends Sort<SubmissionViewSortField> {

  public SubmissionViewSort(String sortString) {
    super(sortString);
  }

  public static SubmissionViewSort defaults() {
    return builder().field(LINE_NUMBER).direction(ASCENDING).build();
  }

  @Override
  public SubmissionViewSortField createField(String value) {
    return SubmissionViewSortField.fromValue(value);
  }
}
