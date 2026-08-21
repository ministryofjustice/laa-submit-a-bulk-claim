package uk.gov.justice.laa.payments.submit.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.ClaimSummary;

@Mapper(componentModel = "spring")
public interface ClaimSummaryMapper {

  @Mapping(target = ".", source = "claimResponse")
  @Mapping(
      target = "isEscaped",
      source = "claimResponse.feeCalculationResponse.boltOnDetails.escapeCaseFlag")
  @Mapping(target = "uniqueClientNumber2", source = "claimResponse.client2Ucn")
  @Mapping(target = "areaOfLaw", source = "areaOfLaw")
  ClaimSummary toClaimSummary(ClaimResponse claimResponse, String areaOfLaw);
}
