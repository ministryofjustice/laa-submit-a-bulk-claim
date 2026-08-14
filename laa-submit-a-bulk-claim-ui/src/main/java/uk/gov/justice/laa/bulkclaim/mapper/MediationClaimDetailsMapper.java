package uk.gov.justice.laa.bulkclaim.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.MediationClaimDetails;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;

@Mapper(componentModel = "spring", uses = ClaimMapperHelper.class)
public interface MediationClaimDetailsMapper {

  @Mapping(target = ".", source = "claimResponse")
  @Mapping(target = "clientForename", source = "claimResponse.clientForename")
  @Mapping(target = "clientSurname", source = "claimResponse.clientSurname")
  @Mapping(target = "uniqueClientNumber", source = "claimResponse.uniqueClientNumber")
  @Mapping(target = "client2Forename", source = "claimResponse.client2Forename")
  @Mapping(target = "client2Surname", source = "claimResponse.client2Surname")
  @Mapping(target = "client2UniqueClientNumber", source = "claimResponse.client2Ucn")
  @Mapping(target = "officeCode", source = "claimResponse.officeCode")
  @Mapping(
      target = "feeCodeDescription",
      source = "claimResponse.feeCalculationResponse.feeCodeDescription")
  @Mapping(target = "uniqueFileNumber", ignore = true)
  @Mapping(target = "fixedFee", source = "claimResponse", qualifiedByName = "fixedFee")
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
  MediationClaimDetails toMediationClaimDetails(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment);
}
