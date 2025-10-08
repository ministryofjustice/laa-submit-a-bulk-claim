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
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

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
  @Mapping(target = "status", source = "status", qualifiedByName = "mapStatus")
  @Mapping(target = "submitted", source = "submitted")
  @Mapping(target = "submissionValue", constant = "50.52")
  SubmissionSummary toSubmissionSummary(SubmissionResponse submissionResponse);

  /**
   * Maps the {@link SubmissionStatus} to string.
   *
   * @param status The status to map.
   * @return The mapped status string.
   */
  @Named("mapStatus")
  default String mapStatus(SubmissionStatus status) {
    if (status == null) {
      return null;
    }
    switch (status) {
      case VALIDATION_SUCCEEDED:
        return "Submitted";
      case VALIDATION_FAILED:
      case REPLACED:
        return "Invalid";
      case CREATED:
      case READY_FOR_VALIDATION:
      case VALIDATION_IN_PROGRESS:
        return "In progress";
      default:
        throw new IllegalArgumentException("Unexpected status: " + status);
    }
  }

  /**
   * Returns a {@link LocalDate} from a submission period string.
   *
   * @param submissionPeriod The submission period string (Should be in YYYY-MM format).
   * @return The {@link LocalDate} representation of the submission period.
   */
  @Named("toSubmissionPeriod")
  default LocalDate toSubmissionPeriod(final String submissionPeriod) {
    // Assumes that API returns MMM-yyyy format.
    DateTimeFormatter dateFormat =
        new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("MMM-yyyy")
            .toFormatter(Locale.ENGLISH);

    YearMonth yearMonth = YearMonth.parse(submissionPeriod, dateFormat);
    return yearMonth.atDay(1);
  }
}
