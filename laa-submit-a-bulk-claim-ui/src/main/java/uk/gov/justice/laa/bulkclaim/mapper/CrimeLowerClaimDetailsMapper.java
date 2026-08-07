package uk.gov.justice.laa.bulkclaim.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;

@Mapper(componentModel = "spring")
public interface CrimeLowerClaimDetailsMapper {

  @Mapping(target = ".", source = "claimResponse")
  @Mapping(target = "officeCode", source = "claimResponse.officeCode")
  @Mapping(
      target = "feeCodeDescription",
      source = "claimResponse.feeCalculationResponse.feeCodeDescription")
  @Mapping(
      target = "escapeCase",
      source = "claimResponse.feeCalculationResponse.boltOnDetails.escapeCaseFlag")
  @Mapping(target = "reportedProfitCosts", source = "claimResponse.netProfitCostsAmount")
  @Mapping(target = "reportedDisbursements", source = "claimResponse.netDisbursementAmount")
  @Mapping(target = "reportedDisbursementsVat", source = "claimResponse.disbursementsVatAmount")
  @Mapping(target = "reportedTravelCosts", source = "claimResponse.travelWaitingCostsAmount")
  @Mapping(target = "reportedWaitingCosts", source = "claimResponse.netWaitingCostsAmount")
  @Mapping(target = "reportedVatApplicable", source = "claimResponse.isVatApplicable")
  @Mapping(
      target = "initialCalculatedFixedFee",
      source = "claimResponse.feeCalculationResponse.fixedFeeAmount")
  @Mapping(
      target = "initialCalculatedProfitCosts",
      source = "claimResponse.feeCalculationResponse.netProfitCostsAmount")
  @Mapping(
      target = "initialCalculatedDisbursements",
      source = "claimResponse.feeCalculationResponse.disbursementAmount")
  @Mapping(
      target = "initialCalculatedDisbursementsVat",
      source = "claimResponse.feeCalculationResponse.disbursementVatAmount")
  @Mapping(
      target = "initialCalculatedTravelCosts",
      source = "claimResponse.feeCalculationResponse.netTravelCostsAmount")
  @Mapping(
      target = "initialCalculatedWaitingCosts",
      source = "claimResponse.feeCalculationResponse.netWaitingCostsAmount")
  @Mapping(
      target = "initialCalculatedVatIndicator",
      source = "claimResponse.feeCalculationResponse.vatIndicator")
  @Mapping(
      target = "initialCalculatedTotalVat",
      source = "claimResponse.feeCalculationResponse.calculatedVatAmount")
  @Mapping(
      target = "initialCalculatedTotalIncludingVat",
      source = "claimResponse.feeCalculationResponse.totalAmount")
  CrimeLowerClaimDetails toCrimeLowerClaimDetails(ClaimResponseV2 claimResponse);
}
