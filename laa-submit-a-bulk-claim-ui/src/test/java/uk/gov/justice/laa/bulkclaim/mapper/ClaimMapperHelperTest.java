package uk.gov.justice.laa.bulkclaim.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.bulkclaim.helper.TestObjectCreator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;

@DisplayName("Claim mapper helper test")
class ClaimMapperHelperTest {

  private final ClaimMapperHelper helper = new ClaimMapperHelper();
  private final ClaimResponseV2 claimResponse =
      TestObjectCreator.buildClaimResponseV2(AreaOfLaw.CRIME_LOWER);
  private final AssessmentGet currentAssessment =
      new AssessmentGet()
          .fixedFeeAmount(new BigDecimal("1.00"))
          .netProfitCostsAmount(new BigDecimal("2.00"))
          .disbursementAmount(new BigDecimal("3.00"))
          .disbursementVatAmount(new BigDecimal("4.00"))
          .isVatApplicable(true)
          .allowedTotalVat(new BigDecimal("5.00"))
          .allowedTotalInclVat(new BigDecimal("6.00"));

  @Test
  @DisplayName("Should build fixed fee from the fee calculation response and assessment")
  void shouldBuildFixedFee() {
    ClaimFieldRow result = helper.fixedFee(claimResponse, currentAssessment);

    assertThat(result.reported()).isNull();
    assertThat(result.initialCalculated())
        .isEqualTo(claimResponse.getFeeCalculationResponse().getFixedFeeAmount());
    assertThat(result.assessed()).isEqualTo(currentAssessment.getFixedFeeAmount());
  }

  @Test
  @DisplayName("Should build profit costs from the claim, fee calculation response and assessment")
  void shouldBuildProfitCosts() {
    ClaimFieldRow result = helper.profitCosts(claimResponse, currentAssessment);

    assertThat(claimResponse.getFeeCalculationResponse().getNetProfitCostsAmount()).isNotNull();
    assertThat(result.reported()).isEqualTo(claimResponse.getNetProfitCostsAmount());
    assertThat(result.initialCalculated())
        .isEqualTo(claimResponse.getFeeCalculationResponse().getNetProfitCostsAmount());
    assertThat(result.assessed()).isEqualTo(currentAssessment.getNetProfitCostsAmount());
  }

  @Test
  @DisplayName("Should build disbursements from the claim, fee calculation response and assessment")
  void shouldBuildDisbursements() {
    ClaimFieldRow result = helper.disbursements(claimResponse, currentAssessment);

    assertThat(result.reported()).isEqualTo(claimResponse.getNetDisbursementAmount());
    assertThat(result.initialCalculated())
        .isEqualTo(claimResponse.getFeeCalculationResponse().getDisbursementAmount());
    assertThat(result.assessed()).isEqualTo(currentAssessment.getDisbursementAmount());
  }

  @Test
  @DisplayName(
      "Should build disbursements VAT from the claim, fee calculation response and assessment")
  void shouldBuildDisbursementsVat() {
    ClaimFieldRow result = helper.disbursementsVat(claimResponse, currentAssessment);

    assertThat(result.reported()).isEqualTo(claimResponse.getDisbursementsVatAmount());
    assertThat(result.initialCalculated())
        .isEqualTo(claimResponse.getFeeCalculationResponse().getDisbursementVatAmount());
    assertThat(result.assessed()).isEqualTo(currentAssessment.getDisbursementVatAmount());
  }

  @Test
  @DisplayName("Should build VAT from the claim, fee calculation response and assessment")
  void shouldBuildVat() {
    ClaimFieldRow result = helper.vat(claimResponse, currentAssessment);

    assertThat(result.reported()).isEqualTo(claimResponse.getIsVatApplicable());
    assertThat(result.initialCalculated())
        .isEqualTo(claimResponse.getFeeCalculationResponse().getVatIndicator());
    assertThat(result.assessed()).isEqualTo(currentAssessment.getIsVatApplicable());
  }

  @Test
  @DisplayName("Should build total VAT from the fee calculation response and assessment")
  void shouldBuildTotalVat() {
    ClaimFieldRow result = helper.totalVat(claimResponse, currentAssessment);

    assertThat(result.reported()).isNull();
    assertThat(result.initialCalculated())
        .isEqualTo(claimResponse.getFeeCalculationResponse().getCalculatedVatAmount());
    assertThat(result.assessed()).isEqualTo(currentAssessment.getAllowedTotalVat());
  }

  @Test
  @DisplayName("Should build total including VAT from the fee calculation response and assessment")
  void shouldBuildTotalIncludingVat() {
    ClaimFieldRow result = helper.totalIncludingVat(claimResponse, currentAssessment);

    assertThat(result.reported()).isNull();
    assertThat(result.initialCalculated())
        .isEqualTo(claimResponse.getFeeCalculationResponse().getTotalAmount());
    assertThat(result.assessed()).isEqualTo(currentAssessment.getAllowedTotalInclVat());
  }

  @Test
  @DisplayName("Should tolerate a null current assessment")
  void shouldTolerateNullAssessment() {
    ClaimFieldRow result = helper.disbursements(claimResponse, null);

    assertThat(result.reported()).isEqualTo(claimResponse.getNetDisbursementAmount());
    assertThat(result.initialCalculated())
        .isEqualTo(claimResponse.getFeeCalculationResponse().getDisbursementAmount());
    assertThat(result.assessed()).isNull();
  }

  @Test
  @DisplayName("Should tolerate a claim response with no fee calculation response")
  void shouldTolerateMissingFeeCalculationResponse() {
    ClaimResponseV2 noFeeCalculation =
        TestObjectCreator.buildClaimResponseV2(AreaOfLaw.CRIME_LOWER).feeCalculationResponse(null);

    ClaimFieldRow result = helper.disbursements(noFeeCalculation, currentAssessment);

    assertThat(result.reported()).isEqualTo(noFeeCalculation.getNetDisbursementAmount());
    assertThat(result.initialCalculated()).isNull();
    assertThat(result.assessed()).isEqualTo(currentAssessment.getDisbursementAmount());
  }
}
