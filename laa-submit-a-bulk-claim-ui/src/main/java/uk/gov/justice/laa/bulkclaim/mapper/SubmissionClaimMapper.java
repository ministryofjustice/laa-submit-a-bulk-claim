package uk.gov.justice.laa.bulkclaim.mapper;

import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionClaimRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionClaimRowCostsDetails;
import uk.gov.justice.laa.claims.model.ClaimFields;

@Mapper(componentModel = "spring")
public interface SubmissionClaimMapper {

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
  SubmissionClaimRow toSubmissionClaimRow(ClaimFields claimFields);

  // TODO: Add claim value to the OpenAPI spec.
  @Mapping(target = "claimValue", constant = "0.00")
  SubmissionClaimRowCostsDetails toSubmissionClaimRowCostsDetails(ClaimFields claimFields);

  static BigDecimal tidy(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }
}
