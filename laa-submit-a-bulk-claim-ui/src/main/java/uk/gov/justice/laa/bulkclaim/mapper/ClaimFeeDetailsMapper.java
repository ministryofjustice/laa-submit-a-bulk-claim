package uk.gov.justice.laa.bulkclaim.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimFeeCalculatedDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimFeeSubmittedDetails;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;

/**
 * Maps between {@link ClaimResponse} and {@link SubmissionClaimDetails}.
 *
 * @author Jamie Briggs
 */
@Mapper(componentModel = "spring")
public interface ClaimFeeDetailsMapper {

  // TODO: Add new calculated fee details to the claim response object and map. This method
  //  technically maps the submitted details.
  SubmissionClaimFeeSubmittedDetails toSubmittedFeeDetails(ClaimResponse claimFields);

  @Mapping(source = "adviceTime", target = "adviceTime")
  @Mapping(source = "travelTime", target = "travelTime")
  @Mapping(source = "waitingTime", target = "waitingTime")
  @Mapping(source = "isLondonRate", target = "isLondonRate")
  @Mapping(source = "costsDamagesRecoveredAmount", target = "costsDamagesRecoveredAmount")
  @Mapping(source = "feeCalculationResponse.totalAmount", target = "totalValue")
  @Mapping(source = "feeCalculationResponse.netProfitCostsAmount", target = "netProfitCostsAmount")
  @Mapping(
      source = "feeCalculationResponse.requestedNetDisbursementAmount",
      target = "netDisbursementAmount")
  @Mapping(
      source = "feeCalculationResponse.netCostOfCounselAmount",
      target = "netCounselCostsAmount")
  @Mapping(
      source = "feeCalculationResponse.disbursementVatAmount",
      target = "disbursementsVatAmount")
  @Mapping(
      source = "feeCalculationResponse.travelAndWaitingCostsAmount",
      target = "travelWaitingCostsAmount")
  @Mapping(
      source = "feeCalculationResponse.netWaitingCostsAmount",
      target = "netWaitingCostsAmount")
  @Mapping(source = "feeCalculationResponse.vatIndicator", target = "isVatApplicable")
  @Mapping(
      source = "feeCalculationResponse.boltOnDetails.boltOnAdjournedHearingFee",
      target = "adjournedHearingFeeAmount")
  @Mapping(
      source = "feeCalculationResponse.detentionAndWaitingCostsAmount",
      target = "detentionTravelWaitingCostsAmount")
  @Mapping(source = "feeCalculationResponse.jrFormFillingAmount", target = "jrFormFillingAmount")
  @BeanMapping(
      nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
      ignoreByDefault = true)
  SubmissionClaimFeeCalculatedDetails toCalculatedFeeDetails(ClaimResponse claimFields);
}
