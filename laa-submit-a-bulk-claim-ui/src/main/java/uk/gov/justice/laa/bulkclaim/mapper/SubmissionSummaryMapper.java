package uk.gov.justice.laa.bulkclaim.mapper;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

/**
 * Maps between {@link SubmissionResponse} and {@link SubmissionSummary}.
 *
 * @author Jamie Briggs
 */
@Mapper(componentModel = "spring")
public interface SubmissionSummaryMapper {

  /**
   * Maps a {@link SubmissionResponse} to a {@link SubmissionSummary}.
   *
   * @param submissionResponse The response to map.
   * @return The mapped {@link SubmissionSummary}.
   */
  @Mapping(target = "submissionReference", source = "submissionId")
  @Mapping(target = "areaOfLaw", source = "areaOfLaw")
  @Mapping(target = "officeAccount", source = "officeAccountNumber")
  @Mapping(
      target = "submissionPeriod",
      source = "submissionPeriod",
      qualifiedByName = "toSubmissionPeriod")
  @Mapping(target = "status", constant = "Submitted")
  @Mapping(target = "submitted", source = "submitted")
  @Mapping(target = "submissionValue", constant = "50.52")
  SubmissionSummary toSubmissionSummary(SubmissionResponse submissionResponse);

  /**
   * Returns a {@link LocalDate} from a submission period string.
   *
   * @param submissionPeriod The submission period string (Should be in YYYY-MM format).
   * @return The {@link LocalDate} representation of the submission period.
   */
  @Named("toSubmissionPeriod")
  default LocalDate toSubmissionPeriod(final String submissionPeriod) {
    // Assumes that API returns MMM-yyyy format.
    DateTimeFormatter mmmYYYY =
        new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("MMM-yyyy")
            .toFormatter(Locale.ENGLISH);

    YearMonth yearMonth = YearMonth.parse(submissionPeriod, mmmYYYY);
    return yearMonth.atDay(1);
  }
}
