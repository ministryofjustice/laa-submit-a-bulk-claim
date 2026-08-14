package uk.gov.justice.laa.bulkclaim.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;

@Mapper(componentModel = "spring", uses = ClaimMapperHelper.class)
public interface CrimeLowerClaimDetailsMapper {

  @Mapping(target = ".", source = "claimResponse")
  @Mapping(target = "officeCode", source = "claimResponse.officeCode")
  @Mapping(
      target = "feeCodeDescription",
      source = "claimResponse.feeCalculationResponse.feeCodeDescription")
  @Mapping(
      target = "escapeCase",
      source = "claimResponse.feeCalculationResponse.boltOnDetails.escapeCaseFlag",
      defaultValue = "false")
  @Mapping(target = "caseStartDate", ignore = true)
  @Mapping(target = "fixedFee", source = "claimResponse", qualifiedByName = "fixedFee")
  @Mapping(target = "profitCosts", source = "claimResponse", qualifiedByName = "profitCosts")
  @Mapping(target = "disbursements", source = "claimResponse", qualifiedByName = "disbursements")
  @Mapping(
      target = "disbursementsVat",
      source = "claimResponse",
      qualifiedByName = "disbursementsVat")
  @Mapping(target = "vat", source = "claimResponse", qualifiedByName = "vat")
  @Mapping(target = "totalVat", source = "claimResponse", qualifiedByName = "totalVat")
  @Mapping(
      target = "totalIncludingVat",
      source = "claimResponse",
      qualifiedByName = "totalIncludingVat")
  @Mapping(target = "travelCosts", source = "claimResponse", qualifiedByName = "travelCosts")
  @Mapping(target = "waitingCosts", source = "claimResponse", qualifiedByName = "waitingCosts")
  CrimeLowerClaimDetails toCrimeLowerClaimDetails(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment);
}
