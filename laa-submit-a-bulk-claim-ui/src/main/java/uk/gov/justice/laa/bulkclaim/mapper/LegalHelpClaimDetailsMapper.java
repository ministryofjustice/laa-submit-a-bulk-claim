package uk.gov.justice.laa.bulkclaim.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.LegalHelpClaimDetails;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;

@Mapper(componentModel = "spring", uses = ClaimMapperHelper.class)
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
  @Mapping(target = "reportedLondonRateIndicator", source = "claimResponse.isLondonRate")
  @Mapping(target = "matterTypeCode", ignore = true)
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
  @Mapping(target = "counselsCosts", source = "claimResponse", qualifiedByName = "counselsCosts")
  @Mapping(
      target = "travelAndWaitingCosts",
      source = "claimResponse",
      qualifiedByName = "travelAndWaitingCosts")
  @Mapping(
      target = "detentionTravelWaitingCosts",
      source = "claimResponse",
      qualifiedByName = "detentionTravelWaitingCosts")
  @Mapping(target = "jrFormFilling", source = "claimResponse", qualifiedByName = "jrFormFilling")
  @Mapping(
      target = "adjournedHearingFee",
      source = "claimResponse",
      qualifiedByName = "adjournedHearingFee")
  @Mapping(target = "cmrhOral", source = "claimResponse", qualifiedByName = "cmrhOral")
  @Mapping(target = "cmrhTelephone", source = "claimResponse", qualifiedByName = "cmrhTelephone")
  @Mapping(
      target = "homeOfficeInterview",
      source = "claimResponse",
      qualifiedByName = "homeOfficeInterview")
  @Mapping(
      target = "substantiveHearing",
      source = "claimResponse",
      qualifiedByName = "substantiveHearing")
  @Mapping(
      target = "matterTypeCodeOne",
      source = "claimResponse.matterTypeCode",
      qualifiedByName = "matterTypeCodeOne")
  @Mapping(
      target = "matterTypeCodeTwo",
      source = "claimResponse.matterTypeCode",
      qualifiedByName = "matterTypeCodeTwo")
  LegalHelpClaimDetails toLegalHelpClaimDetails(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment);

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
