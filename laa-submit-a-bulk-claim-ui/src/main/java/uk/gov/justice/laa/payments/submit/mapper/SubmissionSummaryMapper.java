package uk.gov.justice.laa.payments.submit.mapper;

import java.time.LocalDate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.payments.submit.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.payments.submit.util.SubmissionPeriodUtil;

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
      case VALIDATION_FAILED, REPLACED -> "Invalid";
      case CREATED, READY_FOR_VALIDATION, VALIDATION_IN_PROGRESS -> "In progress";
    };
  }

  @Named("toSubmissionPeriod")
  default LocalDate toSubmissionPeriod(final String submissionPeriod) {
    return SubmissionPeriodUtil.toSubmissionPeriodStart(submissionPeriod);
  }
}
