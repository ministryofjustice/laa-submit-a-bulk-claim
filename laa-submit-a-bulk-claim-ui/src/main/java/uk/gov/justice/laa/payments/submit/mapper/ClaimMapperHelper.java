package uk.gov.justice.laa.payments.submit.mapper;

import java.math.BigDecimal;
import java.util.function.Function;
import org.mapstruct.Context;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.BoltOnPatch;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.FeeCalculationPatch;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.ClaimFieldRow;

@Component
public class ClaimMapperHelper {

  private static Object assessedOrInitialCalculated(
      AssessmentGet assessment, Function<AssessmentGet, Object> getter, Object initialCalculated) {
    Object assessed = assessment == null ? null : getter.apply(assessment);
    return assessed != null ? assessed : initialCalculated;
  }

  @Named("fixedFee")
  public ClaimFieldRow fixedFee(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    Object initialCalculated = feeCalculation == null ? null : feeCalculation.getFixedFeeAmount();
    return new ClaimFieldRow(
        null,
        initialCalculated,
        assessedOrInitialCalculated(
            currentAssessment, AssessmentGet::getFixedFeeAmount, initialCalculated));
  }

  @Named("profitCosts")
  public ClaimFieldRow profitCosts(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    Object initialCalculated =
        feeCalculation == null ? null : feeCalculation.getNetProfitCostsAmount();
    return new ClaimFieldRow(
        claimResponse.getNetProfitCostsAmount(),
        initialCalculated,
        assessedOrInitialCalculated(
            currentAssessment, AssessmentGet::getNetProfitCostsAmount, initialCalculated));
  }

  @Named("disbursements")
  public ClaimFieldRow disbursements(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    Object initialCalculated =
        feeCalculation == null ? null : feeCalculation.getDisbursementAmount();
    return new ClaimFieldRow(
        claimResponse.getNetDisbursementAmount(),
        initialCalculated,
        assessedOrInitialCalculated(
            currentAssessment, AssessmentGet::getDisbursementAmount, initialCalculated));
  }

  @Named("disbursementsVat")
  public ClaimFieldRow disbursementsVat(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    Object initialCalculated =
        feeCalculation == null ? null : feeCalculation.getDisbursementVatAmount();
    return new ClaimFieldRow(
        claimResponse.getDisbursementsVatAmount(),
        initialCalculated,
        assessedOrInitialCalculated(
            currentAssessment, AssessmentGet::getDisbursementVatAmount, initialCalculated));
  }

  @Named("vat")
  public ClaimFieldRow vat(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    Object initialCalculated = feeCalculation == null ? null : feeCalculation.getVatIndicator();
    return new ClaimFieldRow(
        claimResponse.getIsVatApplicable(),
        initialCalculated,
        assessedOrInitialCalculated(
            currentAssessment, AssessmentGet::getIsVatApplicable, initialCalculated));
  }

  @Named("totalVat")
  public ClaimFieldRow totalVat(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    Object initialCalculated =
        feeCalculation == null ? null : feeCalculation.getCalculatedVatAmount();
    return new ClaimFieldRow(
        null,
        initialCalculated,
        assessedOrInitialCalculated(
            currentAssessment, AssessmentGet::getAllowedTotalVat, initialCalculated));
  }

  @Named("totalIncludingVat")
  public ClaimFieldRow totalIncludingVat(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    Object initialCalculated = feeCalculation == null ? null : feeCalculation.getTotalAmount();
    return new ClaimFieldRow(
        null,
        initialCalculated,
        assessedOrInitialCalculated(
            currentAssessment, AssessmentGet::getAllowedTotalInclVat, initialCalculated));
  }

  @Named("travelCosts")
  public ClaimFieldRow travelCosts(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    Object initialCalculated =
        feeCalculation == null ? null : feeCalculation.getNetTravelCostsAmount();
    return new ClaimFieldRow(
        claimResponse.getTravelWaitingCostsAmount(),
        initialCalculated,
        assessedOrInitialCalculated(
            currentAssessment, AssessmentGet::getNetTravelCostsAmount, initialCalculated));
  }

  @Named("waitingCosts")
  public ClaimFieldRow waitingCosts(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    Object initialCalculated =
        feeCalculation == null ? null : feeCalculation.getNetWaitingCostsAmount();
    return new ClaimFieldRow(
        claimResponse.getNetWaitingCostsAmount(),
        initialCalculated,
        assessedOrInitialCalculated(
            currentAssessment, AssessmentGet::getNetWaitingCostsAmount, initialCalculated));
  }

  @Named("counselsCosts")
  public ClaimFieldRow counselsCosts(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    Object initialCalculated =
        feeCalculation == null ? null : feeCalculation.getNetCostOfCounselAmount();
    return new ClaimFieldRow(
        claimResponse.getNetCounselCostsAmount(),
        initialCalculated,
        assessedOrInitialCalculated(
            currentAssessment, AssessmentGet::getNetCostOfCounselAmount, initialCalculated));
  }

  @Named("travelAndWaitingCosts")
  public ClaimFieldRow travelAndWaitingCosts(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    Object initialCalculated =
        feeCalculation == null ? null : feeCalculation.getTravelAndWaitingCostsAmount();
    return new ClaimFieldRow(
        claimResponse.getTravelWaitingCostsAmount(),
        initialCalculated,
        assessedOrInitialCalculated(
            currentAssessment,
            ClaimMapperHelper::assessedTravelAndWaitingCosts,
            initialCalculated));
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
  public ClaimFieldRow detentionTravelWaitingCosts(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    Object initialCalculated =
        feeCalculation == null ? null : feeCalculation.getDetentionTravelAndWaitingCostsAmount();
    return new ClaimFieldRow(
        claimResponse.getDetentionTravelWaitingCostsAmount(),
        initialCalculated,
        assessedOrInitialCalculated(
            currentAssessment,
            AssessmentGet::getDetentionTravelAndWaitingCostsAmount,
            initialCalculated));
  }

  @Named("jrFormFilling")
  public ClaimFieldRow jrFormFilling(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    Object initialCalculated =
        feeCalculation == null ? null : feeCalculation.getJrFormFillingAmount();
    return new ClaimFieldRow(
        claimResponse.getJrFormFillingAmount(),
        initialCalculated,
        assessedOrInitialCalculated(
            currentAssessment, AssessmentGet::getJrFormFillingAmount, initialCalculated));
  }

  @Named("adjournedHearingFee")
  public ClaimFieldRow adjournedHearingFee(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    BoltOnPatch boltOns = boltOnDetails(claimResponse);
    Object initialCalculated = boltOns == null ? null : boltOns.getBoltOnAdjournedHearingFee();
    return new ClaimFieldRow(
        null,
        initialCalculated,
        assessedOrInitialCalculated(
            currentAssessment, AssessmentGet::getBoltOnAdjournedHearingFee, initialCalculated));
  }

  @Named("cmrhOral")
  public ClaimFieldRow cmrhOral(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    BoltOnPatch boltOns = boltOnDetails(claimResponse);
    Object initialCalculated = boltOns == null ? null : boltOns.getBoltOnCmrhOralFee();
    return new ClaimFieldRow(
        null,
        initialCalculated,
        assessedOrInitialCalculated(
            currentAssessment, AssessmentGet::getBoltOnCmrhOralFee, initialCalculated));
  }

  @Named("cmrhTelephone")
  public ClaimFieldRow cmrhTelephone(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    BoltOnPatch boltOns = boltOnDetails(claimResponse);
    Object initialCalculated = boltOns == null ? null : boltOns.getBoltOnCmrhTelephoneFee();
    return new ClaimFieldRow(
        null,
        initialCalculated,
        assessedOrInitialCalculated(
            currentAssessment, AssessmentGet::getBoltOnCmrhTelephoneFee, initialCalculated));
  }

  @Named("homeOfficeInterview")
  public ClaimFieldRow homeOfficeInterview(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    BoltOnPatch boltOns = boltOnDetails(claimResponse);
    Object initialCalculated = boltOns == null ? null : boltOns.getBoltOnHomeOfficeInterviewFee();
    return new ClaimFieldRow(
        null,
        initialCalculated,
        assessedOrInitialCalculated(
            currentAssessment, AssessmentGet::getBoltOnHomeOfficeInterviewFee, initialCalculated));
  }

  @Named("substantiveHearing")
  public ClaimFieldRow substantiveHearing(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    BoltOnPatch boltOns = boltOnDetails(claimResponse);
    Object initialCalculated = boltOns == null ? null : boltOns.getBoltOnSubstantiveHearingFee();
    return new ClaimFieldRow(
        null,
        initialCalculated,
        assessedOrInitialCalculated(
            currentAssessment, AssessmentGet::getBoltOnSubstantiveHearingFee, initialCalculated));
  }

  private static BoltOnPatch boltOnDetails(ClaimResponseV2 claimResponse) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    return feeCalculation == null ? null : feeCalculation.getBoltOnDetails();
  }
}
