package uk.gov.justice.laa.bulkclaim.mapper;

import java.math.BigDecimal;
import org.mapstruct.Context;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimField;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.BoltOnPatch;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.FeeCalculationPatch;

@Component
public class ClaimMapperHelper {

  @Named("fixedFee")
  public ClaimField fixedFee(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    return new ClaimField(
        null,
        feeCalculation == null ? null : feeCalculation.getFixedFeeAmount(),
        currentAssessment == null ? null : currentAssessment.getFixedFeeAmount());
  }

  @Named("profitCosts")
  public ClaimField profitCosts(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    // Profit costs never shows an initial calculated value, even though an upstream
    // calculated value exists on the fee calculation response.
    return new ClaimField(
        claimResponse.getNetProfitCostsAmount(),
        null,
        currentAssessment == null ? null : currentAssessment.getNetProfitCostsAmount());
  }

  @Named("disbursements")
  public ClaimField disbursements(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    return new ClaimField(
        claimResponse.getNetDisbursementAmount(),
        feeCalculation == null ? null : feeCalculation.getDisbursementAmount(),
        currentAssessment == null ? null : currentAssessment.getDisbursementAmount());
  }

  @Named("disbursementsVat")
  public ClaimField disbursementsVat(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    return new ClaimField(
        claimResponse.getDisbursementsVatAmount(),
        feeCalculation == null ? null : feeCalculation.getDisbursementVatAmount(),
        currentAssessment == null ? null : currentAssessment.getDisbursementVatAmount());
  }

  @Named("vat")
  public ClaimField vat(ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    return new ClaimField(
        claimResponse.getIsVatApplicable(),
        feeCalculation == null ? null : feeCalculation.getVatIndicator(),
        currentAssessment == null ? null : currentAssessment.getIsVatApplicable());
  }

  @Named("totalVat")
  public ClaimField totalVat(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    return new ClaimField(
        null,
        feeCalculation == null ? null : feeCalculation.getCalculatedVatAmount(),
        currentAssessment == null ? null : currentAssessment.getAllowedTotalVat());
  }

  @Named("totalIncludingVat")
  public ClaimField totalIncludingVat(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    return new ClaimField(
        null,
        feeCalculation == null ? null : feeCalculation.getTotalAmount(),
        currentAssessment == null ? null : currentAssessment.getAllowedTotalInclVat());
  }

  @Named("travelCosts")
  public ClaimField travelCosts(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    return new ClaimField(
        claimResponse.getTravelWaitingCostsAmount(),
        feeCalculation == null ? null : feeCalculation.getNetTravelCostsAmount(),
        currentAssessment == null ? null : currentAssessment.getNetTravelCostsAmount());
  }

  @Named("waitingCosts")
  public ClaimField waitingCosts(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    return new ClaimField(
        claimResponse.getNetWaitingCostsAmount(),
        feeCalculation == null ? null : feeCalculation.getNetWaitingCostsAmount(),
        currentAssessment == null ? null : currentAssessment.getNetWaitingCostsAmount());
  }

  @Named("counselsCosts")
  public ClaimField counselsCosts(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    return new ClaimField(
        claimResponse.getNetCounselCostsAmount(),
        feeCalculation == null ? null : feeCalculation.getNetCostOfCounselAmount(),
        currentAssessment == null ? null : currentAssessment.getNetCostOfCounselAmount());
  }

  @Named("travelAndWaitingCosts")
  public ClaimField travelAndWaitingCosts(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    return new ClaimField(
        claimResponse.getTravelWaitingCostsAmount(),
        feeCalculation == null ? null : feeCalculation.getTravelAndWaitingCostsAmount(),
        assessedTravelAndWaitingCosts(currentAssessment));
  }

  // AssessmentGet has no combined travel-and-waiting field (unlike FeeCalculationPatch) - sum its
  // separate net_travel_costs_amount and net_waiting_costs_amount to match this field's shape.
  private static BigDecimal assessedTravelAndWaitingCosts(AssessmentGet currentAssessment) {
    if (currentAssessment == null) {
      return null;
    }
    BigDecimal travel = currentAssessment.getNetTravelCostsAmount();
    BigDecimal waiting = currentAssessment.getNetWaitingCostsAmount();
    if (travel == null && waiting == null) {
      return null;
    }
    return (travel == null ? BigDecimal.ZERO : travel)
        .add(waiting == null ? BigDecimal.ZERO : waiting);
  }

  @Named("detentionTravelWaitingCosts")
  public ClaimField detentionTravelWaitingCosts(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    return new ClaimField(
        claimResponse.getDetentionTravelWaitingCostsAmount(),
        feeCalculation == null ? null : feeCalculation.getDetentionTravelAndWaitingCostsAmount(),
        currentAssessment == null
            ? null
            : currentAssessment.getDetentionTravelAndWaitingCostsAmount());
  }

  @Named("jrFormFilling")
  public ClaimField jrFormFilling(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    return new ClaimField(
        claimResponse.getJrFormFillingAmount(),
        feeCalculation == null ? null : feeCalculation.getJrFormFillingAmount(),
        currentAssessment == null ? null : currentAssessment.getJrFormFillingAmount());
  }

  @Named("adjournedHearingFee")
  public ClaimField adjournedHearingFee(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    BoltOnPatch boltOns = boltOnDetails(claimResponse);
    return new ClaimField(
        null,
        boltOns == null ? null : boltOns.getBoltOnAdjournedHearingFee(),
        currentAssessment == null ? null : currentAssessment.getBoltOnAdjournedHearingFee());
  }

  @Named("cmrhOral")
  public ClaimField cmrhOral(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    BoltOnPatch boltOns = boltOnDetails(claimResponse);
    return new ClaimField(
        null,
        boltOns == null ? null : boltOns.getBoltOnCmrhOralFee(),
        currentAssessment == null ? null : currentAssessment.getBoltOnCmrhOralFee());
  }

  @Named("cmrhTelephone")
  public ClaimField cmrhTelephone(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    BoltOnPatch boltOns = boltOnDetails(claimResponse);
    return new ClaimField(
        null,
        boltOns == null ? null : boltOns.getBoltOnCmrhTelephoneFee(),
        currentAssessment == null ? null : currentAssessment.getBoltOnCmrhTelephoneFee());
  }

  @Named("homeOfficeInterview")
  public ClaimField homeOfficeInterview(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    BoltOnPatch boltOns = boltOnDetails(claimResponse);
    return new ClaimField(
        null,
        boltOns == null ? null : boltOns.getBoltOnHomeOfficeInterviewFee(),
        currentAssessment == null ? null : currentAssessment.getBoltOnHomeOfficeInterviewFee());
  }

  @Named("substantiveHearing")
  public ClaimField substantiveHearing(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    BoltOnPatch boltOns = boltOnDetails(claimResponse);
    return new ClaimField(
        null,
        boltOns == null ? null : boltOns.getBoltOnSubstantiveHearingFee(),
        currentAssessment == null ? null : currentAssessment.getBoltOnSubstantiveHearingFee());
  }

  private static BoltOnPatch boltOnDetails(ClaimResponseV2 claimResponse) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    return feeCalculation == null ? null : feeCalculation.getBoltOnDetails();
  }
}
