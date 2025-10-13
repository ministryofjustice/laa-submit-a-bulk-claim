package uk.gov.justice.laa.bulkclaim.mapper;

import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.BulkClaimCostItem;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.ClaimFeeCalculationBreakdown;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;

/**
 * Maps between {@link ClaimResponse} and {@link ClaimFeeCalculationBreakdown}.
 *
 * @author Jamie Briggs
 */
@Mapper(componentModel = "spring")
public interface ClaimFeeCalculationBreakdownMapper {

  @Mapping(target = "fixedFee.enteredValue", ignore = true)
  @Mapping(
      target = "fixedFee.calculatedValue",
      source = "claimResponse.feeCalculationResponse.fixedFeeAmount")
  @Mapping(
      target = "netProfitCost",
      expression =
          """
      java(toBulkClaimCostItem(claimResponse.getNetProfitCostsAmount(),
      claimResponse.getFeeCalculationResponse().getNetProfitCostsAmount()))""")
  @Mapping(
      target = "netDisbursments",
      expression =
          """
      java(toBulkClaimCostItem(claimResponse.getNetDisbursementAmount(),
      claimResponse.getFeeCalculationResponse().getDisbursementAmount()))""")
  @Mapping(
      target = "disbursementVat",
      expression =
          """
      java(toBulkClaimCostItem(claimResponse.getDisbursementsVatAmount(),
      claimResponse.getFeeCalculationResponse().getDisbursementVatAmount()))""")
  @Mapping(
      target = "netCostOfCounsel",
      expression =
          """
      java(toBulkClaimCostItem(claimResponse.getNetCounselCostsAmount(),
      claimResponse.getFeeCalculationResponse().getNetCostOfCounselAmount()))""")
  @Mapping(
      target = "travelAndWaitingCosts",
      expression =
          """
      java(toBulkClaimCostItem(claimResponse.getTravelWaitingCostsAmount(),
      claimResponse.getFeeCalculationResponse().getTravelAndWaitingCostsAmount()))""")
  @Mapping(
      target = "adjournedHearingFee",
      expression =
          """
      java(toBulkClaimCostItem(toBigDecimal(claimResponse.getAdjournedHearingFeeAmount()),
      claimResponse.getFeeCalculationResponse().getBoltOnDetails().getBoltOnAdjournedHearingFee()))""")
  @Mapping(
      target = "jrFormFilling",
      expression =
          """
      java(toBulkClaimCostItem(claimResponse.getJrFormFillingAmount(),
      claimResponse.getFeeCalculationResponse().getJrFormFillingAmount()))""")
  @Mapping(
      target = "detentionTravelAndWaitingCosts",
      expression =
          """
      java(toBulkClaimCostItem(claimResponse.getDetentionTravelWaitingCostsAmount(),
      claimResponse.getFeeCalculationResponse().getDetentionAndWaitingCostsAmount()))""")
  @Mapping(target = "cmrhTelephone.enteredValue", ignore = true)
  @Mapping(
      target = "cmrhTelephone.calculatedValue",
      source = "claimResponse.feeCalculationResponse.boltOnDetails.boltOnCmrhTelephoneFee")
  @Mapping(target = "cmrhOral.enteredValue", ignore = true)
  @Mapping(
      target = "cmrhOral.calculatedValue",
      source = "claimResponse.feeCalculationResponse.boltOnDetails.boltOnCmrhOralFee")
  @Mapping(target = "homeOfficeInterview.enteredValue", ignore = true)
  @Mapping(
      target = "homeOfficeInterview.calculatedValue",
      source = "claimResponse.feeCalculationResponse.boltOnDetails.boltOnHomeOfficeInterviewFee")
  @Mapping(target = "vat.enteredValue", ignore = true)
  @Mapping(
      target = "vat.calculatedValue",
      source = "claimResponse.feeCalculationResponse.calculatedVatAmount")
  @Mapping(target = "calculatedTotal", source = "claimResponse.feeCalculationResponse.totalAmount")
  ClaimFeeCalculationBreakdown toClaimFeeCalculationBreakdown(ClaimResponse claimResponse);

  @Mapping(target = "enteredValue", source = "enteredValue")
  @Mapping(target = "calculatedValue", source = "calculatedValue")
  BulkClaimCostItem toBulkClaimCostItem(BigDecimal enteredValue, BigDecimal calculatedValue);

  default BigDecimal toBigDecimal(Integer value) {
    return value == null
        ? null
        : BigDecimal.valueOf(value).setScale(2); // Ensures precision to 2 decimal points
  }

  default BigDecimal scaleBigDecimal(BigDecimal value) {
    return value == null
        ? null
        : value.setScale(2, BigDecimal.ROUND_HALF_UP); // Ensures consistent scaling
  }
}
