package uk.gov.justice.laa.bulkclaim.mapper;

import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimRowCostsDetails;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;

/**
 * Maps between {@link ClaimResponse} and {@link SubmissionClaimRow}.
 *
 * @author Jamie Briggs
 */
@Mapper(componentModel = "spring")
public interface SubmissionClaimRowMapper {

  @Mapping(target = "ufn", source = "uniqueFileNumber")
  @Mapping(target = "ucn", source = "uniqueClientNumber")
  @Mapping(
      target = "client",
      expression =
          "java(claimFields.getClientForename() + \" \" + " + "claimFields.getClientSurname())")
  @Mapping(target = "category", source = "standardFeeCategoryCode")
  @Mapping(target = "matter", source = "matterTypeCode")
  @Mapping(target = "concludedOrClaimedDate", source = "caseConcludedDate")
  // TODO: Add fee type to the OpenAPI spec.
  @Mapping(target = "feeType", constant = "Fee type")
  @Mapping(target = "costsDetails", source = "claimFields")
  // TODO: Add total messages to the OpenAPI spec.
  @Mapping(target = "totalMessages", constant = "1")
  SubmissionClaimRow toSubmissionClaimRow(ClaimResponse claimFields);

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
