package uk.gov.justice.laa.bulkclaim.mapper;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Locale;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryClaimErrorRow;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryRow;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationErrorFields;

/**
 * Maps between {@link SubmissionResponse} and {@link SubmissionSummaryRow}, and {@link
 * ClaimResponse} and {@link SubmissionSummaryClaimErrorRow}.
 *
 * @author Jamie Briggs
 */
@Mapper(componentModel = "spring")
public interface BulkClaimImportSummaryMapper {

  /**
   * Maps a {@link SubmissionResponse} to a {@link SubmissionSummaryRow}.
   *
   * @param submissionResponse The response to map.
   * @return The mapped {@link SubmissionSummaryRow}.
   */
  @Mapping(target = "submitted", source = "submitted")
  @Mapping(target = "submissionReference", source = "submissionId")
  @Mapping(target = "officeAccount", source = "officeAccountNumber")
  @Mapping(target = "areaOfLaw", source = "areaOfLaw")
  @Mapping(
      target = "submissionPeriod",
      source = "submissionPeriod",
      qualifiedByName = "toSubmissionPeriod")
  @Mapping(target = "totalClaims", source = "numberOfClaims")
  SubmissionSummaryRow toSubmissionSummaryRow(SubmissionResponse submissionResponse);

  List<SubmissionSummaryRow> toSubmissionSummaryRows(List<SubmissionResponse> submissionResponses);

  /**
   * Returns a {@link LocalDate} from a submission period string.
   *
   * @param submissionPeriod The submission period string (Should be in YYYY-MM format).
   * @return The {@link LocalDate} representation of the submission period.
   */
  @Named("toSubmissionPeriod")
  default LocalDate toSubmissionPeriod(final String submissionPeriod) {
    DateTimeFormatter formatter =
        new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("MMM-yyyy")
            .toFormatter(Locale.ENGLISH);

    YearMonth yearMonth = YearMonth.parse(submissionPeriod, formatter);
    return yearMonth.atDay(1);
  }

  @Mapping(target = "ufn", ignore = true)
  @Mapping(target = "ucn", ignore = true)
  @Mapping(target = "client", ignore = true)
  @Mapping(target = "submissionReference", source = "submissionId")
  @Mapping(target = "errorDescription", source = "errorDescription")
  SubmissionSummaryClaimErrorRow toSubmissionSummaryClaimError(ValidationErrorFields errors);
}
