package uk.gov.justice.laa.bulkclaim.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummaryRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessageRow;
import uk.gov.justice.laa.bulkclaim.util.SubmissionPeriodUtil;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessageBase;

@Mapper(componentModel = "spring")
public interface BulkClaimImportSummaryMapper {

  @Mapping(target = "submitted", source = "submitted")
  @Mapping(target = "submissionReference", source = "submissionId")
  @Mapping(target = "officeAccount", source = "officeAccountNumber")
  @Mapping(target = "areaOfLaw", source = "areaOfLaw", qualifiedByName = "fromAreaOfLaw")
  @Mapping(
      target = "submissionPeriod",
      source = "submissionPeriod",
      qualifiedByName = "toSubmissionPeriod")
  @Mapping(target = "totalClaims", source = "numberOfClaims")
  SubmissionSummaryRow toSubmissionSummaryRow(SubmissionResponse submissionResponse);

  List<SubmissionSummaryRow> toSubmissionSummaryRows(List<SubmissionResponse> submissionResponses);

  @Named("fromAreaOfLaw")
  default String fromAreaOfLaw(AreaOfLaw areaOfLaw) {
    return areaOfLaw.getValue().replace("_", " ");
  }

  @Named("toSubmissionPeriod")
  default LocalDate toSubmissionPeriod(final String submissionPeriod) {
    return SubmissionPeriodUtil.toSubmissionPeriodStart(submissionPeriod);
  }

  @Mapping(target = "ufn", source = "claimResponse.uniqueFileNumber")
  @Mapping(target = "ucn", source = "claimResponse.uniqueClientNumber")
  @Mapping(target = "client", expression = "java(buildClientName(claimResponse))")
  @Mapping(target = "clientForename", source = "claimResponse.clientForename")
  @Mapping(target = "clientSurname", source = "claimResponse.clientSurname")
  @Mapping(target = "client2Forename", source = "claimResponse.client2Forename")
  @Mapping(target = "client2Surname", source = "claimResponse.client2Surname")
  @Mapping(target = "client2Ucn", source = "claimResponse.client2Ucn")
  @Mapping(target = "crimeMatterTypeCode", source = "claimResponse.crimeMatterTypeCode")
  @Mapping(target = "submissionReference", source = "message.submissionId")
  @Mapping(
      target = "claimReference",
      source = "message.claimId",
      qualifiedByName = "toClaimReference")
  @Mapping(target = "message", source = "message.displayMessage")
  @Mapping(target = "type", source = "message.type")
  MessageRow toSubmissionSummaryClaimMessage(
      ValidationMessageBase message, ClaimResponseV2 claimResponse);

  default String buildClientName(ClaimResponseV2 claimResponse) {
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

  @Named("toClaimReference")
  default Optional<UUID> toClaimReference(UUID uuid) {
    return Optional.ofNullable(uuid);
  }
}
