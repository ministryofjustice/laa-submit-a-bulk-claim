package uk.gov.justice.laa.bulkclaim.mapper;

import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimRowCostsDetails;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.FeeCalculationType;

@Mapper(componentModel = "spring")
public interface SubmissionClaimRowMapper {

  @Mapping(target = "ufn", source = "claimFields.uniqueFileNumber")
  @Mapping(target = "ucn", source = "claimFields.uniqueClientNumber")
  @Mapping(target = "clientForename", source = "claimFields.clientForename")
  @Mapping(target = "clientSurname", source = "claimFields.clientSurname")
  @Mapping(target = "client2Forename", source = "claimFields.client2Forename")
  @Mapping(target = "client2Surname", source = "claimFields.client2Surname")
  @Mapping(target = "client2Ucn", source = "claimFields.client2Ucn")
  @Mapping(target = "category", source = "claimFields.standardFeeCategoryCode")
  @Mapping(target = "matter", source = "claimFields.matterTypeCode")
  @Mapping(target = "concludedOrClaimedDate", source = "claimFields.caseConcludedDate")
  @Mapping(target = "status", source = "claimFields.status", qualifiedByName = "toClaimStatus")
  @Mapping(
      target = "feeType",
      source = "claimFields.feeCalculationResponse.feeType",
      qualifiedByName = "toFeeType")
  @Mapping(target = "feeCode", source = "claimFields.feeCalculationResponse.feeCode")
  @Mapping(target = "costsDetails", source = "claimFields")
  @Mapping(target = "totalMessages", source = "totalMessages")
  @Mapping(target = "escapeCase", expression = "java(resolveEscapeCase(claimFields))")
  SubmissionClaimRow toSubmissionClaimRow(ClaimResponse claimFields, int totalMessages);

  @Mapping(target = "ufn", source = "claimFields.uniqueFileNumber")
  @Mapping(target = "ucn", source = "claimFields.uniqueClientNumber")
  @Mapping(target = "clientForename", source = "claimFields.clientForename")
  @Mapping(target = "clientSurname", source = "claimFields.clientSurname")
  @Mapping(target = "client2Forename", source = "claimFields.client2Forename")
  @Mapping(target = "client2Surname", source = "claimFields.client2Surname")
  @Mapping(target = "client2Ucn", source = "claimFields.client2Ucn")
  @Mapping(target = "category", source = "claimFields.standardFeeCategoryCode")
  @Mapping(target = "matter", source = "claimFields.matterTypeCode")
  @Mapping(target = "concludedOrClaimedDate", source = "claimFields.caseConcludedDate")
  @Mapping(target = "status", source = "claimFields.status", qualifiedByName = "toClaimStatus")
  @Mapping(
      target = "feeType",
      source = "claimFields.feeCalculationResponse.feeType",
      qualifiedByName = "toFeeType")
  @Mapping(target = "feeCode", source = "claimFields.feeCalculationResponse.feeCode")
  @Mapping(target = "costsDetails", source = "claimFields")
  @Mapping(target = "totalMessages", source = "claimFields.totalWarnings")
  @Mapping(target = "escapeCase", expression = "java(resolveEscapeCase(claimFields))")
  SubmissionClaimRow toSubmissionClaimRow(ClaimResponseV2 claimFields);

  @Named("toFeeType")
  default String toFeeType(final FeeCalculationType feeCalculationType) {
    if (feeCalculationType == null) {
      return null;
    }
    // Convert to sentence case
    String value = feeCalculationType.getValue().replace("_", " ");
    return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
  }

  @Named("toClaimStatus")
  default String toClaimStatus(final ClaimStatus claimStatus) {
    return claimStatus == null ? null : claimStatus.getValue();
  }

  @Mapping(target = "claimValue", source = "claimFields.feeCalculationResponse.totalAmount")
  SubmissionClaimRowCostsDetails toSubmissionClaimRowCostsDetails(ClaimResponse claimFields);

  @Mapping(target = "claimValue", source = "claimFields.feeCalculationResponse.totalAmount")
  SubmissionClaimRowCostsDetails toSubmissionClaimRowCostsDetails(ClaimResponseV2 claimFields);

  /** Retrieves the escape case flag from the nested fee calculation response if available. */
  default Boolean resolveEscapeCase(ClaimResponse claimFields) {
    if (claimFields == null
        || claimFields.getFeeCalculationResponse() == null
        || claimFields.getFeeCalculationResponse().getBoltOnDetails() == null) {
      return null;
    }
    return claimFields.getFeeCalculationResponse().getBoltOnDetails().getEscapeCaseFlag();
  }

  /**
   * Resolves the escape case flag from the given ClaimResponseV2 object. This method navigates
   * through the nested structure of the provided claimFields to determine if the claim has been
   * marked as an escape case.
   */
  default Boolean resolveEscapeCase(ClaimResponseV2 claimFields) {
    if (claimFields == null
        || claimFields.getFeeCalculationResponse() == null
        || claimFields.getFeeCalculationResponse().getBoltOnDetails() == null) {
      return null;
    }
    return claimFields.getFeeCalculationResponse().getBoltOnDetails().getEscapeCaseFlag();
  }

  /** Ensures that a BigDecimal is not null and instead changes to be zero if it is null. */
  static BigDecimal tidy(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }
}
