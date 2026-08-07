package uk.gov.justice.laa.bulkclaim.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.MediationClaimDetails;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;

@Mapper(componentModel = "spring")
public interface MediationClaimDetailsMapper {

  @Mapping(target = ".", source = "claimResponse")
  @Mapping(target = "client1Forename", source = "claimResponse.clientForename")
  @Mapping(target = "client1Surname", source = "claimResponse.clientSurname")
  @Mapping(target = "client1UniqueClientNumber", source = "claimResponse.uniqueClientNumber")
  @Mapping(target = "client2Forename", source = "claimResponse.client2Forename")
  @Mapping(target = "client2Surname", source = "claimResponse.client2Surname")
  @Mapping(target = "client2UniqueClientNumber", source = "claimResponse.client2Ucn")
  @Mapping(target = "officeCode", source = "claimResponse.officeCode")
  @Mapping(target = "reportedDisbursements", source = "claimResponse.netDisbursementAmount")
  @Mapping(target = "reportedDisbursementsVat", source = "claimResponse.disbursementsVatAmount")
  @Mapping(target = "reportedVatApplicable", source = "claimResponse.isVatApplicable")
  @Mapping(
      target = "initialCalculatedFixedFee",
      source = "claimResponse.feeCalculationResponse.fixedFeeAmount")
  @Mapping(
      target = "initialCalculatedDisbursements",
      source = "claimResponse.feeCalculationResponse.disbursementAmount")
  @Mapping(
      target = "initialCalculatedDisbursementsVat",
      source = "claimResponse.feeCalculationResponse.disbursementVatAmount")
  @Mapping(
      target = "initialCalculatedVatIndicator",
      source = "claimResponse.feeCalculationResponse.vatIndicator")
  @Mapping(
      target = "initialCalculatedTotalVat",
      source = "claimResponse.feeCalculationResponse.calculatedVatAmount")
  @Mapping(
      target = "initialCalculatedTotalIncludingVat",
      source = "claimResponse.feeCalculationResponse.totalAmount")
  MediationClaimDetails toMediationClaimDetails(ClaimResponseV2 claimResponse);
}
