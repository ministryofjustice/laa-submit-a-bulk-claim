package uk.gov.justice.laa.bulkclaim.mapper;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryClaimErrorRow;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryRow;
import uk.gov.justice.laa.claims.model.ClaimFields;
import uk.gov.justice.laa.claims.model.ClaimValidationError;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;

/**
 * Maps between {@link GetSubmission200Response} and {@link SubmissionSummaryRow}, and {@link
 * ClaimFields} and {@link SubmissionSummaryClaimErrorRow}.
 *
 * @author Jamie Briggs
 */
@Mapper(componentModel = "spring")
public interface BulkClaimImportSummaryMapper {

  /**
   * Maps a {@link GetSubmission200Response} to a {@link SubmissionSummaryRow}.
   *
   * @param submissionResponse The response to map.
   * @return The mapped {@link SubmissionSummaryRow}.
   */
  @Mapping(target = "submissionReference", source = "submissionId")
  @Mapping(target = "officeAccount", source = "officeAccountNumber")
  @Mapping(target = "areaOfLaw", source = "areaOfLaw")
  @Mapping(
      target = "submissionPeriod",
      source = "submissionPeriod",
      qualifiedByName = "toSubmissionPeriod")
  @Mapping(target = "totalClaims", source = "numberOfClaims")
  SubmissionSummaryRow toSubmissionSummaryRow(GetSubmission200Response submissionResponse);

  List<SubmissionSummaryRow> toSubmissionSummaryRows(
      List<GetSubmission200Response> submissionResponses);

  /**
   * Returns a {@link LocalDate} from a submission period string.
   *
   * @param submissionPeriod The submission period string (Should be in YYYY-MM format).
   * @return The {@link LocalDate} representation of the submission period.
   */
  @Named("toSubmissionPeriod")
  default LocalDate toSubmissionPeriod(final String submissionPeriod) {
    // Assumes that API returns YYYY-MM format.
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
    YearMonth yearMonth = YearMonth.parse(submissionPeriod, formatter);
    return yearMonth.atDay(1);
  }

  /**
   * Maps a {@link ClaimFields} to a {@link SubmissionSummaryClaimErrorRow}, whilst also including a
   * submission reference.
   *
   * @param submissionReference The submission reference.
   * @param claimFields The claim fields to map.
   * @return The mapped {@link SubmissionSummaryClaimErrorRow}.
   */
  @Mapping(target = "ufn", source = "claimFields.uniqueFileNumber")
  @Mapping(target = "ucn", source = "claimFields.uniqueClientNumber")
  SubmissionSummaryClaimErrorRow toSubmissionSummaryClaimError(
      UUID submissionReference, ClaimValidationError claimFields);
}
