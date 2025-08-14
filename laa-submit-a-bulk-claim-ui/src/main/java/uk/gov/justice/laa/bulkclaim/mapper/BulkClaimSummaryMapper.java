package uk.gov.justice.laa.bulkclaim.mapper;

import java.time.LocalDate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryClaimError;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryRow;
import uk.gov.justice.laa.claims.model.ClaimFields;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;

/** Maps between AddressFormData and AddressDetail models. */
@Mapper(componentModel = "spring")
public interface BulkClaimSummaryMapper {

  @Mapping(target = "submissionReference", source = "submission.submissionId")
  @Mapping(target = "officeAccount", source = "submission.officeAccountNumber")
  @Mapping(target = "areaOfLaw", source = "submission.areaOfLaw")
  @Mapping(target = "submissionDate", source = "submission.submissionPeriod",
    qualifiedByName = "toSubmissionPeriod")
  @Mapping(target = "totalClaims", source = "submission.numberOfClaims")
  @Mapping(target = "totalErrors", constant = "100")
  SubmissionSummaryRow toSubmissionSummaryRow(GetSubmission200Response submissionResponse);

  @Named("toSubmissionPeriod")
  default LocalDate toSubmissionPeriod(
      final String submissionPeriod) {
    // Assumes that API returns YYYY-MM format.
    String[] periodArray = submissionPeriod.split("-");
    return LocalDate.of(Integer.parseInt(periodArray[0]), Integer.parseInt(periodArray[1]), 1);
  }

  @Mapping(target = "ufn", source = "uniqueFileNumber")
  @Mapping(target = "ucn", source = "uniqueClientNumber")
  @Mapping(target = "client", expression = """
    java(claimFields.getClientForename() + \" \" + claimFields.getClientSurname())
    """)
  @Mapping(target = "errorDescription", constant = "Error description")
  SubmissionSummaryClaimError toSubmissionSummaryClaimError(ClaimFields claimFields);
}
