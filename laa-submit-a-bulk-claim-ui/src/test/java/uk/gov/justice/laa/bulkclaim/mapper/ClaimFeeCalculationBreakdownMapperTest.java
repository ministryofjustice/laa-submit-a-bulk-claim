package uk.gov.justice.laa.bulkclaim.mapper;

import java.math.BigDecimal;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.ClaimFeeCalculationBreakdown;
import uk.gov.justice.laa.bulkclaim.helper.TestObjectCreator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.BoltOnPatch;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.FeeCalculationPatch;

@ExtendWith({SpringExtension.class})
@DisplayName("Claim fee calculation breakdown mapper test")
class ClaimFeeCalculationBreakdownMapperTest {

  private ClaimFeeCalculationBreakdownMapper mapper;

  @BeforeEach
  void setup() {
    mapper = new ClaimFeeCalculationBreakdownMapperImpl();
  }

  @Test
  @DisplayName("Should map all details")
  void shouldMapAllDetails() {
    // Given
    ClaimResponse claimResponse = TestObjectCreator.buildClaimResponse();
    claimResponse.setFeeCalculationResponse(
        new FeeCalculationPatch()
            .fixedFeeAmount(BigDecimal.valueOf(1100.10))
            .netProfitCostsAmount(BigDecimal.valueOf(1200.20))
            .disbursementAmount(BigDecimal.valueOf(1300.30))
            .disbursementVatAmount(BigDecimal.valueOf(1400.40))
            .netCostOfCounselAmount(BigDecimal.valueOf(1500.50))
            .travelAndWaitingCostsAmount(BigDecimal.valueOf(1600.60))
            .jrFormFillingAmount(BigDecimal.valueOf(1800.80))
            .detentionTravelAndWaitingCostsAmount(BigDecimal.valueOf(1700.70))
            .calculatedVatAmount(BigDecimal.valueOf(20.50))
            // TODO: Substantive hearing missing?
            .boltOnDetails(
                BoltOnPatch.builder()
                    .boltOnCmrhTelephoneFee(BigDecimal.valueOf(1800.80))
                    .boltOnCmrhOralFee(BigDecimal.valueOf(1900.90))
                    .boltOnAdjournedHearingFee(BigDecimal.valueOf(2010.10))
                    .boltOnHomeOfficeInterviewFee(BigDecimal.valueOf(2020.20))
                    .build())
            .totalAmount(BigDecimal.valueOf(51234.12)));
    // When
    ClaimFeeCalculationBreakdown result = mapper.toClaimFeeCalculationBreakdown(claimResponse);
    // Then
    SoftAssertions.assertSoftly(
        softAssertions -> {
          softAssertions
              .assertThat(result.fixedFee().enteredValue())
              .isNull(); // Should be ignored, not entered by user
          softAssertions
              .assertThat(result.fixedFee().calculatedValue())
              .isEqualTo(monetaryValue(1100.10));
          softAssertions
              .assertThat(result.netProfitCost().enteredValue())
              .isEqualTo(monetaryValue(100.10));
          softAssertions
              .assertThat(result.netProfitCost().calculatedValue())
              .isEqualTo(monetaryValue(1200.20));
          softAssertions
              .assertThat(result.netDisbursments().enteredValue())
              .isEqualTo(monetaryValue(200.20));
          softAssertions
              .assertThat(result.netDisbursments().calculatedValue())
              .isEqualTo(monetaryValue(1300.30));
          softAssertions
              .assertThat(result.disbursementVat().enteredValue())
              .isEqualTo(monetaryValue(17.50));
          softAssertions
              .assertThat(result.disbursementVat().calculatedValue())
              .isEqualTo(monetaryValue(1400.40));
          softAssertions
              .assertThat(result.netCostOfCounsel().enteredValue())
              .isEqualTo(monetaryValue(300.30));
          softAssertions
              .assertThat(result.netCostOfCounsel().calculatedValue())
              .isEqualTo(monetaryValue(1500.50));
          softAssertions
              .assertThat(result.travelAndWaitingCosts().enteredValue())
              .isEqualTo(monetaryValue(500.50));
          softAssertions
              .assertThat(result.travelAndWaitingCosts().calculatedValue())
              .isEqualTo(monetaryValue(1600.60));
          softAssertions
              .assertThat(result.adjournedHearingFee().enteredValue())
              .isEqualTo(monetaryValue(9));
          softAssertions
              .assertThat(result.adjournedHearingFee().calculatedValue())
              .isEqualTo(monetaryValue(2010.10));
          softAssertions
              .assertThat(result.jrFormFilling().enteredValue())
              .isEqualTo(monetaryValue(800.80));
          softAssertions
              .assertThat(result.jrFormFilling().calculatedValue())
              .isEqualTo(monetaryValue(1800.80));
          softAssertions
              .assertThat(result.detentionTravelAndWaitingCosts().enteredValue())
              .isEqualTo(monetaryValue(700.70));
          softAssertions
              .assertThat(result.detentionTravelAndWaitingCosts().calculatedValue())
              .isEqualTo(monetaryValue(1700.70));
          softAssertions
              .assertThat(result.cmrhTelephone().enteredValue())
              .isNull(); // Not entered by the user
          softAssertions
              .assertThat(result.cmrhTelephone().calculatedValue())
              .isEqualTo(monetaryValue(1800.80));
          softAssertions
              .assertThat(result.cmrhOral().enteredValue())
              .isNull(); // Not entered by the user
          softAssertions
              .assertThat(result.cmrhOral().calculatedValue())
              .isEqualTo(monetaryValue(1900.90));
          softAssertions
              .assertThat(result.homeOfficeInterview().enteredValue())
              .isNull(); // Not entered by the user
          softAssertions
              .assertThat(result.homeOfficeInterview().calculatedValue())
              .isEqualTo(monetaryValue(2020.20));
          // TODO: Substantive hearing is missing?
          softAssertions
              .assertThat(result.vat().enteredValue())
              .isNull(); // Not entered by the user
          softAssertions.assertThat(result.vat().calculatedValue()).isEqualTo(monetaryValue(20.50));
          softAssertions.assertThat(result.calculatedTotal()).isEqualTo(monetaryValue(51234.12));
        });
  }

  private static BigDecimal monetaryValue(double value) {
    return BigDecimal.valueOf(value).setScale(2, BigDecimal.ROUND_HALF_UP);
  }
}
