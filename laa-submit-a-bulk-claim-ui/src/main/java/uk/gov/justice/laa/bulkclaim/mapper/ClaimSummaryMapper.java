package uk.gov.justice.laa.bulkclaim.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.ClaimSummary;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;

@Mapper(componentModel = "spring")
public interface ClaimSummaryMapper {

  @Mapping(target = ".", source = "claimResponse")
  @Mapping(target = "isEscaped", constant = "true") // TODO: Add when added to API response
  @Mapping(target = "areaOfLaw", source = "areaOfLaw")
  ClaimSummary toClaimSummary(ClaimResponse claimResponse, String areaOfLaw);
}
