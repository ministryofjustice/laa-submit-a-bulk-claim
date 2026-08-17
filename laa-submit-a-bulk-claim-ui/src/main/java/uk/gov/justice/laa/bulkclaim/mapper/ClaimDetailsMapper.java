package uk.gov.justice.laa.bulkclaim.mapper;

import org.mapstruct.Context;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ObjectFactory;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.LegalHelpClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.MediationClaimDetails;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;

@Mapper(componentModel = "spring", uses = ClaimMapperHelper.class)
public interface ClaimDetailsMapper {

  @Mapping(target = ".", source = "claimResponse")
  @Mapping(target = "feeCodeDescription", source = "feeCalculationResponse.feeCodeDescription")
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
  ClaimDetails toCommonClaimDetails(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment);

  @InheritConfiguration(name = "toCommonClaimDetails")
  @Mapping(
      target = "escapeCase",
      source = "feeCalculationResponse.boltOnDetails.escapeCaseFlag",
      defaultValue = "false")
  @Mapping(target = "caseStartDate", ignore = true)
  @Mapping(target = "profitCosts", source = "claimResponse", qualifiedByName = "profitCosts")
  @Mapping(target = "travelCosts", source = "claimResponse", qualifiedByName = "travelCosts")
  @Mapping(target = "waitingCosts", source = "claimResponse", qualifiedByName = "waitingCosts")
  CrimeLowerClaimDetails toCrimeLowerClaimDetails(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment);

  @InheritConfiguration(name = "toCommonClaimDetails")
  @Mapping(target = "categoryOfLaw", source = "feeCalculationResponse.categoryOfLaw")
  @Mapping(
      target = "escapeCase",
      source = "feeCalculationResponse.boltOnDetails.escapeCaseFlag",
      defaultValue = "false")
  @Mapping(target = "reportedLondonRateIndicator", source = "isLondonRate")
  @Mapping(target = "matterTypeCode", ignore = true)
  @Mapping(target = "profitCosts", source = "claimResponse", qualifiedByName = "profitCosts")
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
      source = "matterTypeCode",
      qualifiedByName = "matterTypeCodeOne")
  @Mapping(
      target = "matterTypeCodeTwo",
      source = "matterTypeCode",
      qualifiedByName = "matterTypeCodeTwo")
  LegalHelpClaimDetails toLegalHelpClaimDetails(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment);

  @InheritConfiguration(name = "toCommonClaimDetails")
  @Mapping(target = "client2UniqueClientNumber", source = "client2Ucn")
  @Mapping(target = "uniqueFileNumber", ignore = true)
  MediationClaimDetails toMediationClaimDetails(
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

  @ObjectFactory
  default ClaimDetails createClaimDetails(ClaimResponseV2 claimResponse) {
    return switch (claimResponse.getAreaOfLaw()) {
      case CRIME_LOWER -> new CrimeLowerClaimDetails();
      case LEGAL_HELP -> new LegalHelpClaimDetails();
      case MEDIATION -> new MediationClaimDetails();
    };
  }
}
