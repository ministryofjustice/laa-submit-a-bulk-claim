package uk.gov.justice.laa.bulkclaim.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.LegalHelpClaimDetails;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;

@Mapper(componentModel = "spring")
public interface LegalHelpClaimDetailsMapper {

  @Mapping(target = ".", source = "claimResponse")
  @Mapping(target = "officeCode", source = "claimResponse.officeCode")
  @Mapping(target = "categoryOfLaw", source = "claimResponse.feeCalculationResponse.categoryOfLaw")
  @Mapping(
      target = "feeCodeDescription",
      source = "claimResponse.feeCalculationResponse.feeCodeDescription")
  @Mapping(
      target = "escapeCase",
      source = "claimResponse.feeCalculationResponse.boltOnDetails.escapeCaseFlag",
      defaultValue = "false")
  @Mapping(target = "reportedProfitCosts", source = "claimResponse.netProfitCostsAmount")
  @Mapping(target = "reportedDisbursements", source = "claimResponse.netDisbursementAmount")
  @Mapping(target = "reportedDisbursementsVat", source = "claimResponse.disbursementsVatAmount")
  @Mapping(
      target = "reportedTravelAndWaitingCosts",
      source = "claimResponse.travelWaitingCostsAmount")
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
      target = "initialCalculatedCounselsCosts",
      source = "claimResponse.feeCalculationResponse.netCostOfCounselAmount")
  @Mapping(
      target = "initialCalculatedTravelAndWaitingCosts",
      source = "claimResponse.feeCalculationResponse.travelAndWaitingCostsAmount")
  @Mapping(
      target = "initialCalculatedDetentionTravelWaitingCosts",
      source = "claimResponse.feeCalculationResponse.detentionTravelAndWaitingCostsAmount")
  @Mapping(
      target = "initialCalculatedJrFormFilling",
      source = "claimResponse.feeCalculationResponse.jrFormFillingAmount")
  @Mapping(
      target = "initialCalculatedAdjournedHearingFee",
      source = "claimResponse.feeCalculationResponse.boltOnDetails.boltOnAdjournedHearingFee")
  @Mapping(
      target = "initialCalculatedCmrhOral",
      source = "claimResponse.feeCalculationResponse.boltOnDetails.boltOnCmrhOralFee")
  @Mapping(
      target = "initialCalculatedCmrhTelephone",
      source = "claimResponse.feeCalculationResponse.boltOnDetails.boltOnCmrhTelephoneFee")
  @Mapping(
      target = "initialCalculatedHomeOfficeInterview",
      source = "claimResponse.feeCalculationResponse.boltOnDetails.boltOnHomeOfficeInterviewFee")
  @Mapping(
      target = "initialCalculatedSubstantiveHearing",
      source = "claimResponse.feeCalculationResponse.boltOnDetails.boltOnSubstantiveHearingFee")
  @Mapping(
      target = "initialCalculatedVatIndicator",
      source = "claimResponse.feeCalculationResponse.vatIndicator")
  @Mapping(
      target = "initialCalculatedTotalVat",
      source = "claimResponse.feeCalculationResponse.calculatedVatAmount")
  @Mapping(
      target = "initialCalculatedTotalIncludingVat",
      source = "claimResponse.feeCalculationResponse.totalAmount")
  @Mapping(
      target = "matterTypeCodeOne",
      source = "matterTypeCode",
      qualifiedByName = "matterTypeCodeOne")
  @Mapping(
      target = "matterTypeCodeTwo",
      source = "matterTypeCode",
      qualifiedByName = "matterTypeCodeTwo")
  LegalHelpClaimDetails toLegalHelpClaimDetails(ClaimResponseV2 claimResponse);

  @Named("matterTypeCodeOne")
  static String matterTypeCodeOne(String matterTypeCode) {
    if (matterTypeCode == null) {
      return null;
    }
    return matterTypeCode.split(":")[0];
  }

  @Named("matterTypeCodeTwo")
  static String matterTypeCodeTwo(String matterTypeCode) {
    if (matterTypeCode == null) {
      return null;
    }
    return matterTypeCode.split(":")[1];
  }
}
