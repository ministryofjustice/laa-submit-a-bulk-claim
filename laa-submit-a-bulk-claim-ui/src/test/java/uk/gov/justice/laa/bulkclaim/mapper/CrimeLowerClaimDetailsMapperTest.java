package uk.gov.justice.laa.bulkclaim.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;
import uk.gov.justice.laa.bulkclaim.helper.TestObjectCreator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;

@DisplayName("Crime lower claim details mapper test")
class CrimeLowerClaimDetailsMapperTest {

  CrimeLowerClaimDetailsMapper mapper = new CrimeLowerClaimDetailsMapperImpl();

  {
    ReflectionTestUtils.setField(mapper, "claimMapperHelper", new ClaimMapperHelper());
  }

  @Test
  @DisplayName("Should map summary fields from the claim")
  void shouldMapSummaryFields() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.CRIME_LOWER);

    CrimeLowerClaimDetails result = mapper.toCrimeLowerClaimDetails(claimResponse, null);

    assertThat(result.getClientForename()).isEqualTo(claimResponse.getClientForename());
    assertThat(result.getClientSurname()).isEqualTo(claimResponse.getClientSurname());
    assertThat(result.getUniqueFileNumber()).isEqualTo(claimResponse.getUniqueFileNumber());
    assertThat(result.getOfficeCode()).isEqualTo(claimResponse.getOfficeCode());
    assertThat(result.getDateSubmitted()).isEqualTo(claimResponse.getDateSubmitted());
    assertThat(result.getAreaOfLaw()).isEqualTo(AreaOfLaw.CRIME_LOWER);
    assertThat(result.getFeeCode()).isEqualTo(claimResponse.getFeeCode());
    assertThat(result.getFeeCodeDescription())
        .isEqualTo(claimResponse.getFeeCalculationResponse().getFeeCodeDescription());
    assertThat(result.getCrimeMatterTypeCode()).isEqualTo(claimResponse.getCrimeMatterTypeCode());
    assertThat(result.getCaseStartDate()).isNull();
    assertThat(result.getRepresentationOrderDate())
        .isEqualTo(claimResponse.getRepresentationOrderDate());
    assertThat(result.getStageReachedCode()).isEqualTo(claimResponse.getStageReachedCode());
    assertThat(result.getOutcomeCode()).isEqualTo(claimResponse.getOutcomeCode());
    assertThat(result.getCaseConcludedDate()).isEqualTo(claimResponse.getCaseConcludedDate());
    assertThat(result.getEscapeCase()).isTrue();
  }

  @Test
  @DisplayName(
      "Should populate value fields from the claim, fee calculation response and current"
          + " assessment")
  void shouldMapValueFields() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.CRIME_LOWER);
    var feeCalculation = claimResponse.getFeeCalculationResponse();
    AssessmentGet currentAssessment =
        new AssessmentGet().netTravelCostsAmount(new BigDecimal("999.99"));

    CrimeLowerClaimDetails result =
        mapper.toCrimeLowerClaimDetails(claimResponse, currentAssessment);

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
    assertThat(result.getTravelCosts().reported())
        .isEqualTo(claimResponse.getTravelWaitingCostsAmount());
    assertThat(result.getTravelCosts().initialCalculated())
        .isEqualTo(feeCalculation.getNetTravelCostsAmount());
    assertThat(result.getTravelCosts().assessed())
        .isEqualTo(currentAssessment.getNetTravelCostsAmount());
    assertThat(result.getWaitingCosts().reported())
        .isEqualTo(claimResponse.getNetWaitingCostsAmount());
    assertThat(result.getWaitingCosts().initialCalculated())
        .isEqualTo(feeCalculation.getNetWaitingCostsAmount());
  }
}
