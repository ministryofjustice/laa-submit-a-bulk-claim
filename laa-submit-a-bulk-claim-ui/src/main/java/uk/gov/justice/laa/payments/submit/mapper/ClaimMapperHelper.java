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

  private static Object assessedOrCurrentCalculated(
      AssessmentGet assessment, Function<AssessmentGet, Object> getter, Object currentCalculated) {
    Object assessed = assessment == null ? null : getter.apply(assessment);
    return assessed != null ? assessed : currentCalculated;
  }

  @Named("fixedFee")
  public ClaimFieldRow fixedFee(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch currentFeeCalculation = claimResponse.getFeeCalculationResponse();
    Object currentCalculated =
        currentFeeCalculation == null ? null : currentFeeCalculation.getFixedFeeAmount();
    return new ClaimFieldRow(
        null,
        currentCalculated,
        assessedOrCurrentCalculated(
            currentAssessment, AssessmentGet::getFixedFeeAmount, currentCalculated));
  }

  @Named("profitCosts")
  public ClaimFieldRow profitCosts(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch currentFeeCalculation = claimResponse.getFeeCalculationResponse();
    Object currentCalculated =
        currentFeeCalculation == null ? null : currentFeeCalculation.getNetProfitCostsAmount();
    return new ClaimFieldRow(
        claimResponse.getNetProfitCostsAmount(),
        currentCalculated,
        assessedOrCurrentCalculated(
            currentAssessment, AssessmentGet::getNetProfitCostsAmount, currentCalculated));
  }

  @Named("disbursements")
  public ClaimFieldRow disbursements(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch currentFeeCalculation = claimResponse.getFeeCalculationResponse();
    Object currentCalculated =
        currentFeeCalculation == null ? null : currentFeeCalculation.getDisbursementAmount();
    return new ClaimFieldRow(
        claimResponse.getNetDisbursementAmount(),
        currentCalculated,
        assessedOrCurrentCalculated(
            currentAssessment, AssessmentGet::getDisbursementAmount, currentCalculated));
  }

  @Named("disbursementsVat")
  public ClaimFieldRow disbursementsVat(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch currentFeeCalculation = claimResponse.getFeeCalculationResponse();
    Object currentCalculated =
        currentFeeCalculation == null ? null : currentFeeCalculation.getDisbursementVatAmount();
    return new ClaimFieldRow(
        claimResponse.getDisbursementsVatAmount(),
        currentCalculated,
        assessedOrCurrentCalculated(
            currentAssessment, AssessmentGet::getDisbursementVatAmount, currentCalculated));
  }

  @Named("vat")
  public ClaimFieldRow vat(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch currentFeeCalculation = claimResponse.getFeeCalculationResponse();
    Object currentCalculated =
        currentFeeCalculation == null ? null : currentFeeCalculation.getVatIndicator();
    return new ClaimFieldRow(
        claimResponse.getIsVatApplicable(),
        currentCalculated,
        assessedOrCurrentCalculated(
            currentAssessment, AssessmentGet::getIsVatApplicable, currentCalculated));
  }

  @Named("totalVat")
  public ClaimFieldRow totalVat(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch currentFeeCalculation = claimResponse.getFeeCalculationResponse();
    Object currentCalculated =
        currentFeeCalculation == null ? null : currentFeeCalculation.getCalculatedVatAmount();
    return new ClaimFieldRow(
        null,
        currentCalculated,
        assessedOrCurrentCalculated(
            currentAssessment, AssessmentGet::getAllowedTotalVat, currentCalculated));
  }

  @Named("totalIncludingVat")
  public ClaimFieldRow totalIncludingVat(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch currentFeeCalculation = claimResponse.getFeeCalculationResponse();
    Object currentCalculated =
        currentFeeCalculation == null ? null : currentFeeCalculation.getTotalAmount();
    return new ClaimFieldRow(
        null,
        currentCalculated,
        assessedOrCurrentCalculated(
            currentAssessment, AssessmentGet::getAllowedTotalInclVat, currentCalculated));
  }

  @Named("travelCosts")
  public ClaimFieldRow travelCosts(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch currentFeeCalculation = claimResponse.getFeeCalculationResponse();
    Object currentCalculated =
        currentFeeCalculation == null ? null : currentFeeCalculation.getNetTravelCostsAmount();
    return new ClaimFieldRow(
        claimResponse.getTravelWaitingCostsAmount(),
        currentCalculated,
        assessedOrCurrentCalculated(
            currentAssessment, AssessmentGet::getNetTravelCostsAmount, currentCalculated));
  }

  @Named("waitingCosts")
  public ClaimFieldRow waitingCosts(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch currentFeeCalculation = claimResponse.getFeeCalculationResponse();
    Object currentCalculated =
        currentFeeCalculation == null ? null : currentFeeCalculation.getNetWaitingCostsAmount();
    return new ClaimFieldRow(
        claimResponse.getNetWaitingCostsAmount(),
        currentCalculated,
        assessedOrCurrentCalculated(
            currentAssessment, AssessmentGet::getNetWaitingCostsAmount, currentCalculated));
  }

  @Named("counselsCosts")
  public ClaimFieldRow counselsCosts(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch currentFeeCalculation = claimResponse.getFeeCalculationResponse();
    Object currentCalculated =
        currentFeeCalculation == null ? null : currentFeeCalculation.getNetCostOfCounselAmount();
    return new ClaimFieldRow(
        claimResponse.getNetCounselCostsAmount(),
        currentCalculated,
        assessedOrCurrentCalculated(
            currentAssessment, AssessmentGet::getNetCostOfCounselAmount, currentCalculated));
  }

  @Named("travelAndWaitingCosts")
  public ClaimFieldRow travelAndWaitingCosts(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch currentFeeCalculation = claimResponse.getFeeCalculationResponse();
    Object currentCalculated =
        currentFeeCalculation == null
            ? null
            : currentFeeCalculation.getTravelAndWaitingCostsAmount();
    return new ClaimFieldRow(
        claimResponse.getTravelWaitingCostsAmount(),
        currentCalculated,
        assessedOrCurrentCalculated(
            currentAssessment,
            ClaimMapperHelper::assessedTravelAndWaitingCosts,
            currentCalculated));
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
    FeeCalculationPatch currentFeeCalculation = claimResponse.getFeeCalculationResponse();
    Object currentCalculated =
        currentFeeCalculation == null
            ? null
            : currentFeeCalculation.getDetentionTravelAndWaitingCostsAmount();
    return new ClaimFieldRow(
        claimResponse.getDetentionTravelWaitingCostsAmount(),
        currentCalculated,
        assessedOrCurrentCalculated(
            currentAssessment,
            AssessmentGet::getDetentionTravelAndWaitingCostsAmount,
            currentCalculated));
  }

  @Named("jrFormFilling")
  public ClaimFieldRow jrFormFilling(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    FeeCalculationPatch currentFeeCalculation = claimResponse.getFeeCalculationResponse();
    Object currentCalculated =
        currentFeeCalculation == null ? null : currentFeeCalculation.getJrFormFillingAmount();
    return new ClaimFieldRow(
        claimResponse.getJrFormFillingAmount(),
        currentCalculated,
        assessedOrCurrentCalculated(
            currentAssessment, AssessmentGet::getJrFormFillingAmount, currentCalculated));
  }

  @Named("adjournedHearingFee")
  public ClaimFieldRow adjournedHearingFee(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    BoltOnPatch boltOns = boltOnDetails(claimResponse);
    Object currentCalculated = boltOns == null ? null : boltOns.getBoltOnAdjournedHearingFee();
    return new ClaimFieldRow(
        null,
        currentCalculated,
        assessedOrCurrentCalculated(
            currentAssessment, AssessmentGet::getBoltOnAdjournedHearingFee, currentCalculated));
  }

  @Named("cmrhOral")
  public ClaimFieldRow cmrhOral(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    BoltOnPatch boltOns = boltOnDetails(claimResponse);
    Object currentCalculated = boltOns == null ? null : boltOns.getBoltOnCmrhOralFee();
    return new ClaimFieldRow(
        null,
        currentCalculated,
        assessedOrCurrentCalculated(
            currentAssessment, AssessmentGet::getBoltOnCmrhOralFee, currentCalculated));
  }

  @Named("cmrhTelephone")
  public ClaimFieldRow cmrhTelephone(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    BoltOnPatch boltOns = boltOnDetails(claimResponse);
    Object currentCalculated = boltOns == null ? null : boltOns.getBoltOnCmrhTelephoneFee();
    return new ClaimFieldRow(
        null,
        currentCalculated,
        assessedOrCurrentCalculated(
            currentAssessment, AssessmentGet::getBoltOnCmrhTelephoneFee, currentCalculated));
  }

  @Named("homeOfficeInterview")
  public ClaimFieldRow homeOfficeInterview(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    BoltOnPatch boltOns = boltOnDetails(claimResponse);
    Object currentCalculated = boltOns == null ? null : boltOns.getBoltOnHomeOfficeInterviewFee();
    return new ClaimFieldRow(
        null,
        currentCalculated,
        assessedOrCurrentCalculated(
            currentAssessment, AssessmentGet::getBoltOnHomeOfficeInterviewFee, currentCalculated));
  }

  @Named("substantiveHearing")
  public ClaimFieldRow substantiveHearing(
      ClaimResponseV2 claimResponse, @Context AssessmentGet currentAssessment) {
    BoltOnPatch boltOns = boltOnDetails(claimResponse);
    Object currentCalculated = boltOns == null ? null : boltOns.getBoltOnSubstantiveHearingFee();
    return new ClaimFieldRow(
        null,
        currentCalculated,
        assessedOrCurrentCalculated(
            currentAssessment, AssessmentGet::getBoltOnSubstantiveHearingFee, currentCalculated));
  }

  private static BoltOnPatch boltOnDetails(ClaimResponseV2 claimResponse) {
    FeeCalculationPatch currentFeeCalculation = claimResponse.getFeeCalculationResponse();
    return currentFeeCalculation == null ? null : currentFeeCalculation.getBoltOnDetails();
  }
}
