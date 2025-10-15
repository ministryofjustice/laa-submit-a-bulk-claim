package uk.gov.justice.laa.bulkclaim.mapper;

import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimRowCostsDetails;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.FeeCalculationType;

/**
 * Maps between {@link ClaimResponse} and {@link SubmissionClaimRow}.
 *
 * @author Jamie Briggs
 */
@Mapper(componentModel = "spring")
public interface SubmissionClaimRowMapper {

  @Mapping(target = "ufn", source = "claimFields.uniqueFileNumber")
  @Mapping(target = "ucn", source = "claimFields.uniqueClientNumber")
  @Mapping(
      target = "client",
      expression =
          "java(claimFields.getClientForename() + \" \" + " + "claimFields.getClientSurname())")
  @Mapping(target = "clientForename", source = "claimFields.clientForename")
  @Mapping(target = "clientSurname", source = "claimFields.clientSurname")
  @Mapping(target = "client2Forename", source = "claimFields.client2Forename")
  @Mapping(target = "client2Surname", source = "claimFields.client2Surname")
  @Mapping(target = "client2Ucn", source = "claimFields.client2Ucn")
  @Mapping(target = "category", source = "claimFields.standardFeeCategoryCode")
  @Mapping(target = "matter", source = "claimFields.matterTypeCode")
  @Mapping(target = "concludedOrClaimedDate", source = "claimFields.caseConcludedDate")
  @Mapping(
      target = "feeType",
      source = "claimFields.feeCalculationResponse.feeType",
      qualifiedByName = "toFeeType")
  @Mapping(target = "feeCode", source = "claimFields.feeCalculationResponse.feeCode")
  @Mapping(target = "costsDetails", source = "claimFields")
  @Mapping(target = "totalMessages", source = "totalMessages")
  @Mapping(target = "escapeCase", expression = "java(resolveEscapeCase(claimFields))")
  SubmissionClaimRow toSubmissionClaimRow(ClaimResponse claimFields, int totalMessages);

  /**
   * Maps the FeeCalculationType to a human readable string.
   *
   * @param feeCalculationType The FeeCalculationType to map.
   * @return The mapped FeeCalculationType string.
   */
  @Named("toFeeType")
  default String toFeeType(final FeeCalculationType feeCalculationType) {
    if (feeCalculationType == null) {
      return null;
    }
    // Convert to sentence case
    String value = feeCalculationType.getValue().replace("_", " ");
    return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
  }

  @Mapping(target = "claimValue", source = "claimFields.feeCalculationResponse.totalAmount")
  SubmissionClaimRowCostsDetails toSubmissionClaimRowCostsDetails(ClaimResponse claimFields);

  /**
   * Retrieves the escape case flag from the nested fee calculation response if available.
   *
   * @param claimFields the claim response to inspect
   * @return {@code Boolean.TRUE} if the claim is an escape case, {@code Boolean.FALSE} if not, or
   *     {@code null} if the value is unavailable
   */
  default Boolean resolveEscapeCase(ClaimResponse claimFields) {
    if (claimFields == null
        || claimFields.getFeeCalculationResponse() == null
        || claimFields.getFeeCalculationResponse().getBoltOnDetails() == null) {
      return null;
    }
    return claimFields.getFeeCalculationResponse().getBoltOnDetails().getEscapeCaseFlag();
  }

  /**
   * Ensures that a BigDecimal is not null and instead changes to be zero if it is null.
   *
   * @param value the BigDecimal to tidy
   * @return the tidied BigDecimal
   */
  static BigDecimal tidy(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }
}
