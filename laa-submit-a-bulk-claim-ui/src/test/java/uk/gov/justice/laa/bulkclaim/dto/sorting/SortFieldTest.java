package uk.gov.justice.laa.bulkclaim.dto.sorting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.bulkclaim.dto.sorting.SortDirection.NONE;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SortFieldTest {

  @Mock Sort<TestSortField> sort;

  @ParameterizedTest
  @EnumSource(names = {"ASCENDING", "DESCENDING", "NONE"})
  void getDirection(SortDirection direction) {
    var field = new TestSortField("foo");

    when(sort.getField()).thenReturn(field);
    when(sort.getDirection()).thenReturn(direction);

    var fieldDirection = field.getDirection(sort);

    assertThat(fieldDirection).isEqualTo(direction);
  }

  @Test
  void getDirectionDefaultsToNone() {
    var field = new TestSortField("foo");
    var otherField = new TestSortField("bar");
    when(sort.getField()).thenReturn(otherField);

    var direction = field.getDirection(sort);

    assertThat(direction).isEqualTo(NONE);
  }
}
