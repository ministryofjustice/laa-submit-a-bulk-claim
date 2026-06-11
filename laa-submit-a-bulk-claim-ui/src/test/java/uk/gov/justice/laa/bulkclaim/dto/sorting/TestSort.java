package uk.gov.justice.laa.bulkclaim.dto.sorting;

import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TestSort extends Sort<TestSortField> {

  public TestSort(String sortString) {
    super(sortString);
  }

  @Override
  public TestSortField createField(String value) {
    return new TestSortField(value);
  }
}
