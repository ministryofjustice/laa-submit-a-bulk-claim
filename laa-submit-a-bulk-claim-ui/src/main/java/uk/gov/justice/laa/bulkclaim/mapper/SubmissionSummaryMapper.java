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
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@Mapper(componentModel = "spring")
public interface SubmissionSummaryMapper {

  @Mapping(target = "submissionReference", source = "submissionId")
  @Mapping(target = "areaOfLaw", source = "areaOfLaw", qualifiedByName = "fromAreaOfLaw")
  @Mapping(target = "officeAccount", source = "officeAccountNumber")
  @Mapping(
      target = "submissionPeriod",
      source = "submissionPeriod",
      qualifiedByName = "toSubmissionPeriod")
  @Mapping(target = "status", source = "status", qualifiedByName = "mapStatus")
  @Mapping(target = "submitted", source = "submitted")
  @Mapping(target = "submissionValue", ignore = true)
  @Mapping(target = "isDraft", source = "submissionResponse", qualifiedByName = "mapIsDraft")
  SubmissionSummary toSubmissionSummary(SubmissionResponse submissionResponse);

  @Named("fromAreaOfLaw")
  default String fromAreaOfLaw(AreaOfLaw areaOfLaw) {
    return areaOfLaw.getValue().replace("_", " ");
  }

  @Named("mapStatus")
  default String mapStatus(SubmissionStatus status) {
    if (status == null) {
      return null;
    }
    return switch (status) {
      case VALIDATION_SUCCEEDED -> "Submitted";
      case READY_FOR_SUBMISSION -> "Draft";
      case VALIDATION_FAILED, REPLACED -> "Invalid";
      case CREATED, READY_FOR_VALIDATION, VALIDATION_IN_PROGRESS -> "In progress";
    };
  }

  @Named("mapIsDraft")
  default boolean mapIsDraft(SubmissionResponse submissionResponse) {
    return submissionResponse.getStatus() == SubmissionStatus.READY_FOR_SUBMISSION;
  }

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
