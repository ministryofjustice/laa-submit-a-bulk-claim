package uk.gov.justice.laa.bulkclaim.dto.sorting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class SortTest {

  @Nested
  class ConstructorTests {
    @Test
    void shouldConvertStringToSortWhenAscendingOrder() {
      String str = "foo,asc";
      var result = new TestSort(str);
      assertThat(result.getField()).isEqualTo(new TestSortField("foo"));
      assertThat(result.getDirection()).isEqualTo(SortDirection.ASCENDING);
    }

    @Test
    void shouldConvertStringToSortWhenDescendingOrder() {
      String str = "foo,desc";
      var result = new TestSort(str);
      assertThat(result.getField()).isEqualTo(new TestSortField("foo"));
      assertThat(result.getDirection()).isEqualTo(SortDirection.DESCENDING);
    }

    @Test
    void shouldThrowExceptionForInvalidDirection() {
      String str = "foo,foo";
      assertThatThrownBy(() -> new TestSort(str)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionForNonCommaSeparatedInput() {
      String str = "foo";
      assertThatThrownBy(() -> new TestSort(str)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionForInvalidInput() {
      String str = "foo,desc,foo";
      assertThatThrownBy(() -> new TestSort(str)).isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  class ToStringTests {

    @Test
    void shouldConvertToString() {
      var sort =
          TestSort.builder()
              .field(new TestSortField("foo"))
              .direction(SortDirection.ASCENDING)
              .build();

      assertThat(sort.toString()).isEqualTo("foo,asc");
    }

    @Test
    void shouldConvertSortToStringWhenNoOrder() {
      var sort =
          TestSort.builder().field(new TestSortField("foo")).direction(SortDirection.NONE).build();
      assertThat(sort.toString()).isNull();
    }
  }
}
