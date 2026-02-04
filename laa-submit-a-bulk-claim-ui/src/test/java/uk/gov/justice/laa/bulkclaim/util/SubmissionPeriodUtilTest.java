package uk.gov.justice.laa.bulkclaim.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionBase;

@DisplayName("Submission period util tests")
class SubmissionPeriodUtilTest {

  SubmissionPeriodUtil submissionPeriodUtil = new SubmissionPeriodUtil();

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
}
