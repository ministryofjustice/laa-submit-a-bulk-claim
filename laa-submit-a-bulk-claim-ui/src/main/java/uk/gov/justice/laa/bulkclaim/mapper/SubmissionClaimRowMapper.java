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
  SubmissionClaimRow toSubmissionClaimRow(ClaimResponse claimFields, int totalMessages);

  /**
   * Maps the FeeCalculationType to a human readable string.
   *
   * @param feeCalculationType The FeeCalculationType to map.
   * @return The mapped FeeCalculationType string.
   */
  @Named("toFeeType")
  default String toFeeType(final FeeCalculationType feeCalculationType) {
    // Convert to sentence case
    String value = feeCalculationType.getValue().replace("_", " ");
    return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
  }

  @Mapping(target = "claimValue", source = "claimFields.totalValue")
  SubmissionClaimRowCostsDetails toSubmissionClaimRowCostsDetails(ClaimResponse claimFields);

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
