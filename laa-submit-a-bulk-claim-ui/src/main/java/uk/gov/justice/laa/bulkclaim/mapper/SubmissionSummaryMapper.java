package uk.gov.justice.laa.bulkclaim.mapper;

import java.time.LocalDate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;

/**
 * Maps between {@link GetSubmission200Response} and {@link SubmissionSummary}.
 *
 * @author Jamie Briggs
 */
@Mapper(componentModel = "spring")
public interface SubmissionSummaryMapper {

  /**
   * Maps a {@link GetSubmission200Response} to a {@link SubmissionSummary}.
   *
   * @param submissionResponse The response to map.
   * @return The mapped {@link SubmissionSummary}.
   */
  @Mapping(target = "submissionReference", source = "submission.submissionId")
  @Mapping(target = "areaOfLaw", source = "submission.areaOfLaw")
  @Mapping(target = "officeAccount", source = "submission.officeAccountNumber")
  @Mapping(
      target = "submissionPeriod",
      source = "submission.submissionPeriod",
      qualifiedByName = "toSubmissionPeriod")
  @Mapping(target = "status", constant = "Submitted")
  @Mapping(target = "submitted", source = "submission.submitted")
  @Mapping(target = "submissionValue", constant = "50.52")
  SubmissionSummary toSubmissionSummary(GetSubmission200Response submissionResponse);

  /**
   * Returns a {@link LocalDate} from a submission period string.
   *
   * @param submissionPeriod The submission period string (Should be in YYYY-MM format).
   * @return The {@link LocalDate} representation of the submission period.
   */
  @Named("toSubmissionPeriod")
  default LocalDate toSubmissionPeriod(final String submissionPeriod) {
    // Assumes that API returns YYYY-MM format.
    String[] periodArray = submissionPeriod.split("-");
    return LocalDate.of(Integer.parseInt(periodArray[0]), Integer.parseInt(periodArray[1]), 1);
  }
}
