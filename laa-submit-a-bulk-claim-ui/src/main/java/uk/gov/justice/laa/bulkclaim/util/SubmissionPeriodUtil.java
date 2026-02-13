package uk.gov.justice.laa.bulkclaim.util;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionBase;

/**
 * Utility class for processing and formatting submission periods.
 *
 * @author Jamie Briggs
 */
@Component
public class SubmissionPeriodUtil {

  private final DateWrapperUtil dateWrapperUtil;
  private final YearMonth earliestSubmissionPeriod;

  private static final DateTimeFormatter IN_FMT =
      new DateTimeFormatterBuilder()
          .parseCaseInsensitive()
          .appendPattern("MMM-uuuu")
          .toFormatter(Locale.ENGLISH);

  private static final DateTimeFormatter OUT_FMT =
      DateTimeFormatter.ofPattern("MMMM uuuu", Locale.ENGLISH);

  public SubmissionPeriodUtil(
      DateWrapperUtil dateWrapperUtil,
      @Value("${app.submission.minimum-period}") String minimumSubmissionPeriod) {
    this.dateWrapperUtil = dateWrapperUtil;
    this.earliestSubmissionPeriod = YearMonth.parse(minimumSubmissionPeriod, IN_FMT);
  }

  /**
   * Generates all possible submission periods starting from the earliest allowed submission period
   * up to and including the month prior to the current date. Each period is represented as a
   * key-value pair, where the key is formatted in uppercase "MMM-uuuu" (e.g., "JAN-2023"), and the
   * value is formatted in "MMMM uuuu" (e.g., "January 2023").
   *
   * <p>The calculation is based on the current date provided by {@code dateWrapperUtil.now()} and
   * the {@code earliestSubmissionPeriod} field. The method iterates backward month by month.
   *
   * @return a {@code Map<String, String>} containing all possible submission periods. The map uses
   *     keys in uppercase "MMM-uuuu" format and values in "MMMM uuuu" format, ordered from the
   *     latest available period to the earliest.
   */
  public Map<String, String> getAllPossibleSubmissionPeriods() {
    Map<String, String> periods = new LinkedHashMap<>();
    YearMonth previousMonth = YearMonth.from(dateWrapperUtil.now().minusMonths(1));

    for (YearMonth month = previousMonth;
        month.isAfter(earliestSubmissionPeriod) || month.equals(earliestSubmissionPeriod);
        month = month.minusMonths(1)) {
      periods.put(month.format(IN_FMT).toUpperCase(), month.format(OUT_FMT));
    }
    return periods;
  }

  /**
   * Retrieves and formats the submission period from the provided {@code SubmissionBase} instance.
   * The submission period is expected to be in the format "MMM-uuuu" (e.g., "Jan-2023") and is
   * parsed and reformatted to "MMMM uuuu" (e.g., "January 2023").
   *
   * @param submissionBase the {@code SubmissionBase} instance containing the submission period. It
   *     may return {@code null} or a blank value for the period.
   * @return the formatted submission period string, or {@code null} if the input period is {@code
   *     null} or blank.
   */
  public String getSubmissionPeriod(SubmissionBase submissionBase) {
    String submissionPeriod = submissionBase.getSubmissionPeriod();
    if (submissionPeriod == null || submissionPeriod.isBlank()) {
      return null;
    }
    YearMonth ym = YearMonth.parse(submissionPeriod.trim(), IN_FMT);
    return ym.format(OUT_FMT);
  }

  /**
   * Converts a submission period string from the format "MMM-uuuu" (e.g., "Jan-2023") to the format
   * "MMMM uuuu" (e.g., "January 2023").
   *
   * @param submissionPeriod the submission period string to be converted.
   * @return the formatted submission period string, or {@code null} if the input period is {@code
   *     null} or blank.
   */
  public String toLongFormat(String submissionPeriod) {
    if (submissionPeriod == null || submissionPeriod.isBlank()) {
      return null;
    }
    return YearMonth.parse(submissionPeriod.trim(), IN_FMT).format(OUT_FMT);
  }

  /**
   * Computes a numeric sort order value from the submission period of the provided {@code
   * SubmissionBase} instance. The submission period is expected to be in the format "MMM-uuuu"
   * (e.g., "Jan-2023"), and a sortable integer value is derived based on the year and month of the
   * period.
   *
   * @param submissionBase the {@code SubmissionBase} instance containing the submission period. The
   *     period must be a valid string in the format "MMM-uuuu".
   * @return an integer representing the sortable order for the submission period, where the value
   *     is calculated as {@code year * 100 + month}.
   */
  public Integer getSortOrderFromSubmissionPeriod(SubmissionBase submissionBase) {
    String submissionPeriod = submissionBase.getSubmissionPeriod();
    YearMonth yearMonth = YearMonth.parse(submissionPeriod, IN_FMT);
    // Uses year than month to create a number to sort values by.
    // December 2015 = 201512
    // January 2020 = 202001
    // February 2020 = 202002
    return (yearMonth.getYear() * 100) + yearMonth.getMonthValue();
  }
}
