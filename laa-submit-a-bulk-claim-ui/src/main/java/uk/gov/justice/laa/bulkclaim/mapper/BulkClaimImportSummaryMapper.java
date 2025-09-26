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
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummaryRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionSummaryClaimMessageRow;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessageBase;

/**
 * Maps between {@link SubmissionResponse} and {@link SubmissionSummaryRow}, and {@link
 * ClaimResponse} and {@link SubmissionSummaryClaimMessageRow}.
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

  /**
   * Maps claim response and validation error fields to a submission summary claim error row.
   *
   * @param message the validation message base
   * @param claimResponse the claim response containing client and claim details
   * @return a mapped SubmissionSummaryClaimMessageRow
   */
  @Mapping(target = "ufn", source = "claimResponse.uniqueFileNumber")
  @Mapping(target = "ucn", source = "claimResponse.uniqueClientNumber")
  @Mapping(target = "client", expression = "java(buildClientName(claimResponse))")
  @Mapping(target = "submissionReference", source = "message.submissionId")
  @Mapping(target = "message", source = "message.displayMessage")
  @Mapping(target = "type", source = "message.type")
  SubmissionSummaryClaimMessageRow toSubmissionSummaryClaimMessage(
      ValidationMessageBase message, ClaimResponse claimResponse);

  /**
   * Builds a client name from the claim response, preferring the primary client if available.
   *
   * @param claimResponse the claim response containing client name fields
   * @return the full client name or null if none is available
   */
  default String buildClientName(ClaimResponse claimResponse) {
    if (claimResponse == null) {
      return null;
    }

    // Prefer client1, fallback to client2
    String forename = claimResponse.getClientForename();
    String surname = claimResponse.getClientSurname();
    if ((forename == null || forename.isBlank()) && (surname == null || surname.isBlank())) {
      forename = claimResponse.getClient2Forename();
      surname = claimResponse.getClient2Surname();
    }

    if ((forename == null || forename.isBlank()) && (surname == null || surname.isBlank())) {
      return null; // no usable name at all
    }

    return String.format("%s %s", forename != null ? forename : "", surname != null ? surname : "")
        .trim();
  }
}
