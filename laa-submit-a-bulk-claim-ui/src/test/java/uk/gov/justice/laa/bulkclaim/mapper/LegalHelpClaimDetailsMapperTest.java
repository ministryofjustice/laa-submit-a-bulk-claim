package uk.gov.justice.laa.bulkclaim.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.LegalHelpClaimDetails;
import uk.gov.justice.laa.bulkclaim.helper.TestObjectCreator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;

@DisplayName("Legal help claim details mapper test")
class LegalHelpClaimDetailsMapperTest {

  LegalHelpClaimDetailsMapper mapper = new LegalHelpClaimDetailsMapperImpl();

  {
    ReflectionTestUtils.setField(mapper, "claimMapperHelper", new ClaimMapperHelper());
  }

  @Test
  @DisplayName("Should map summary fields from the claim")
  void shouldMapSummaryFields() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.LEGAL_HELP);

    LegalHelpClaimDetails result = mapper.toLegalHelpClaimDetails(claimResponse, null);

    assertThat(result.getClientForename()).isEqualTo(claimResponse.getClientForename());
    assertThat(result.getClientSurname()).isEqualTo(claimResponse.getClientSurname());
    assertThat(result.getUniqueFileNumber()).isEqualTo(claimResponse.getUniqueFileNumber());
    assertThat(result.getOfficeCode()).isEqualTo(claimResponse.getOfficeCode());
    assertThat(result.getDateSubmitted()).isEqualTo(claimResponse.getDateSubmitted());
    assertThat(result.getAreaOfLaw()).isEqualTo(AreaOfLaw.LEGAL_HELP);
    assertThat(result.getCategoryOfLaw())
        .isEqualTo(claimResponse.getFeeCalculationResponse().getCategoryOfLaw());
    assertThat(result.getFeeCode()).isEqualTo(claimResponse.getFeeCode());
    assertThat(result.getFeeCodeDescription())
        .isEqualTo(claimResponse.getFeeCalculationResponse().getFeeCodeDescription());
    assertThat(result.getMatterTypeCode()).isNull();
    assertThat(result.getMatterTypeCodeOne())
        .isEqualTo(claimResponse.getMatterTypeCode().split(":")[0]);
    assertThat(result.getMatterTypeCodeTwo())
        .isEqualTo(claimResponse.getMatterTypeCode().split(":")[1]);
    assertThat(result.getCaseStartDate()).isEqualTo(claimResponse.getCaseStartDate());
    assertThat(result.getCaseConcludedDate()).isEqualTo(claimResponse.getCaseConcludedDate());
    assertThat(result.getEscapeCase()).isTrue();
    assertThat(result.getReportedLondonRateIndicator()).isEqualTo(claimResponse.getIsLondonRate());
  }

  @Test
  @DisplayName(
      "Should populate value fields from the claim, fee calculation response and current"
          + " assessment")
  void shouldMapValueFields() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.LEGAL_HELP);
    var feeCalculation = claimResponse.getFeeCalculationResponse();
    var boltOns = feeCalculation.getBoltOnDetails();
    AssessmentGet currentAssessment =
        new AssessmentGet()
            .netTravelCostsAmount(new BigDecimal("100.00"))
            .netWaitingCostsAmount(new BigDecimal("25.50"));

    LegalHelpClaimDetails result = mapper.toLegalHelpClaimDetails(claimResponse, currentAssessment);

    assertThat(result.getFixedFee().initialCalculated())
        .isEqualTo(feeCalculation.getFixedFeeAmount());
    assertThat(result.getProfitCosts().reported())
        .isEqualTo(claimResponse.getNetProfitCostsAmount());
    assertThat(result.getProfitCosts().initialCalculated()).isNull();
    assertThat(result.getDisbursements().reported())
        .isEqualTo(claimResponse.getNetDisbursementAmount());
    assertThat(result.getDisbursementsVat().reported())
        .isEqualTo(claimResponse.getDisbursementsVatAmount());
    assertThat(result.getVat().reported()).isEqualTo(claimResponse.getIsVatApplicable());
    assertThat(result.getTotalVat().initialCalculated())
        .isEqualTo(feeCalculation.getCalculatedVatAmount());
    assertThat(result.getTotalIncludingVat().initialCalculated())
        .isEqualTo(feeCalculation.getTotalAmount());
    assertThat(result.getCounselsCosts().reported())
        .isEqualTo(claimResponse.getNetCounselCostsAmount());
    assertThat(result.getCounselsCosts().initialCalculated())
        .isEqualTo(feeCalculation.getNetCostOfCounselAmount());
    assertThat(result.getTravelAndWaitingCosts().reported())
        .isEqualTo(claimResponse.getTravelWaitingCostsAmount());
    assertThat(result.getTravelAndWaitingCosts().initialCalculated())
        .isEqualTo(feeCalculation.getTravelAndWaitingCostsAmount());
    assertThat(result.getTravelAndWaitingCosts().assessed()).isEqualTo(new BigDecimal("125.50"));
    assertThat(result.getDetentionTravelWaitingCosts().reported())
        .isEqualTo(claimResponse.getDetentionTravelWaitingCostsAmount());
    assertThat(result.getDetentionTravelWaitingCosts().initialCalculated())
        .isEqualTo(feeCalculation.getDetentionTravelAndWaitingCostsAmount());
    assertThat(result.getJrFormFilling().reported())
        .isEqualTo(claimResponse.getJrFormFillingAmount());
    assertThat(result.getJrFormFilling().initialCalculated())
        .isEqualTo(feeCalculation.getJrFormFillingAmount());
    assertThat(result.getAdjournedHearingFee().reported()).isNull();
    assertThat(result.getAdjournedHearingFee().initialCalculated())
        .isEqualTo(boltOns.getBoltOnAdjournedHearingFee());
    assertThat(result.getCmrhOral().initialCalculated()).isEqualTo(boltOns.getBoltOnCmrhOralFee());
    assertThat(result.getCmrhTelephone().initialCalculated())
        .isEqualTo(boltOns.getBoltOnCmrhTelephoneFee());
    assertThat(result.getHomeOfficeInterview().initialCalculated())
        .isEqualTo(boltOns.getBoltOnHomeOfficeInterviewFee());
    assertThat(result.getSubstantiveHearing().initialCalculated())
        .isEqualTo(boltOns.getBoltOnSubstantiveHearingFee());
  }
}
