package uk.gov.justice.laa.bulkclaim.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionBase;

@ExtendWith(MockitoExtension.class)
@DisplayName("Submission period util tests")
class SubmissionPeriodUtilTest {

  @Mock DateWrapperUtil dateWrapperUtil;

  SubmissionPeriodUtil submissionPeriodUtil;

  @BeforeEach
  void beforeEach() {
    submissionPeriodUtil = new SubmissionPeriodUtil(dateWrapperUtil, "JAN-2025");
  }

  @ParameterizedTest
  @CsvSource({
    "JAN-2010, January 2010",
    "FEB-2011, February 2011",
    "MAR-2012, March 2012",
    "APR-2013, April 2013",
    "MAY-2014, May 2014",
    "JUN-2015, June 2015",
    "JUL-2016, July 2016",
    "AUG-2017, August 2017",
    "SEP-2018, September 2018",
    "OCT-2019, October 2019",
    "NOV-2020, November 2020",
    "DEC-2021, December 2021"
  })
  @DisplayName("Should get submission period")
  void shouldGetSubmissionPeriod(String input, String expected) {
    // Given
    SubmissionBase submissionBase = SubmissionBase.builder().submissionPeriod(input).build();
    // When
    String result = submissionPeriodUtil.getSubmissionPeriod(submissionBase);
    // Then
    assertThat(result).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({
    "JAN-2010, 201001",
    "FEB-2011, 201102",
    "MAR-2012, 201203",
    "APR-2013, 201304",
    "MAY-2014, 201405",
    "JUN-2015, 201506",
    "JUL-2016, 201607",
    "AUG-2017, 201708",
    "SEP-2018, 201809",
    "OCT-2019, 201910",
    "NOV-2020, 202011",
    "DEC-2021, 202112"
  })
  @DisplayName("Should get submission period sort value")
  void shouldGetSubmissionPeriod(String input, int expected) {
    // Given
    SubmissionBase submissionBase = SubmissionBase.builder().submissionPeriod(input).build();
    // When
    Integer result = submissionPeriodUtil.getSortOrderFromSubmissionPeriod(submissionBase);
    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Nested
  @DisplayName("Get all possible submission periods")
  class GetAllPossibleSubmissionPeriods {

    @Test
    @DisplayName(
        "Should get all possible submission period values up but not including current " + "month")
    void shouldGetAllSubmissionPeriods() {
      // Given
      when(dateWrapperUtil.now()).thenReturn(LocalDate.of(2025, 12, 1));
      // When
      Map<String, String> result = submissionPeriodUtil.getAllPossibleSubmissionPeriods();
      // Then
      assertThat(result.size()).isEqualTo(11);
      assertThat(result.get("JAN-2025")).isEqualTo("January 2025");
      assertThat(result.get("FEB-2025")).isEqualTo("February 2025");
      assertThat(result.get("MAR-2025")).isEqualTo("March 2025");
      assertThat(result.get("APR-2025")).isEqualTo("April 2025");
      assertThat(result.get("MAY-2025")).isEqualTo("May 2025");
      assertThat(result.get("JUN-2025")).isEqualTo("June 2025");
      assertThat(result.get("JUL-2025")).isEqualTo("July 2025");
      assertThat(result.get("AUG-2025")).isEqualTo("August 2025");
      assertThat(result.get("SEP-2025")).isEqualTo("September 2025");
      assertThat(result.get("OCT-2025")).isEqualTo("October 2025");
      assertThat(result.get("NOV-2025")).isEqualTo("November 2025");
      assertThat(result.containsKey("DEC-2025")).isFalse();
    }
  }
}
