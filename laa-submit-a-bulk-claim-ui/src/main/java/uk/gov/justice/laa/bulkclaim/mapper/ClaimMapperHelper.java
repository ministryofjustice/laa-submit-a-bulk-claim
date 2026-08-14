package uk.gov.justice.laa.bulkclaim.mapper;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimField;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.FeeCalculationPatch;

@Component
public class ClaimMapperHelper {

  @Named("fixedFee")
  public ClaimField fixedFee(ClaimResponseV2 claimResponse, AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    return new ClaimField(
        null,
        feeCalculation == null ? null : feeCalculation.getFixedFeeAmount(),
        currentAssessment == null ? null : currentAssessment.getFixedFeeAmount());
  }

  @Named("profitCosts")
  public ClaimField profitCosts(ClaimResponseV2 claimResponse, AssessmentGet currentAssessment) {
    // Profit costs never shows an initial calculated value, even though an upstream
    // calculated value exists on the fee calculation response.
    return new ClaimField(
        claimResponse.getNetProfitCostsAmount(),
        null,
        currentAssessment == null ? null : currentAssessment.getNetProfitCostsAmount());
  }

  @Named("disbursements")
  public ClaimField disbursements(ClaimResponseV2 claimResponse, AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    return new ClaimField(
        claimResponse.getNetDisbursementAmount(),
        feeCalculation == null ? null : feeCalculation.getDisbursementAmount(),
        currentAssessment == null ? null : currentAssessment.getDisbursementAmount());
  }

  @Named("disbursementsVat")
  public ClaimField disbursementsVat(
      ClaimResponseV2 claimResponse, AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    return new ClaimField(
        claimResponse.getDisbursementsVatAmount(),
        feeCalculation == null ? null : feeCalculation.getDisbursementVatAmount(),
        currentAssessment == null ? null : currentAssessment.getDisbursementVatAmount());
  }

  @Named("vat")
  public ClaimField vat(ClaimResponseV2 claimResponse, AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    return new ClaimField(
        claimResponse.getIsVatApplicable(),
        feeCalculation == null ? null : feeCalculation.getVatIndicator(),
        currentAssessment == null ? null : currentAssessment.getIsVatApplicable());
  }

  @Named("totalVat")
  public ClaimField totalVat(ClaimResponseV2 claimResponse, AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    return new ClaimField(
        null,
        feeCalculation == null ? null : feeCalculation.getCalculatedVatAmount(),
        currentAssessment == null ? null : currentAssessment.getAllowedTotalVat());
  }

  @Named("totalIncludingVat")
  public ClaimField totalIncludingVat(
      ClaimResponseV2 claimResponse, AssessmentGet currentAssessment) {
    FeeCalculationPatch feeCalculation = claimResponse.getFeeCalculationResponse();
    return new ClaimField(
        null,
        feeCalculation == null ? null : feeCalculation.getTotalAmount(),
        currentAssessment == null ? null : currentAssessment.getAllowedTotalInclVat());
  }
}
