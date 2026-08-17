package uk.gov.justice.laa.bulkclaim.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.LegalHelpClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.MediationClaimDetails;
import uk.gov.justice.laa.bulkclaim.helper.TestObjectCreator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;

@DisplayName("Claim details mapper test")
class ClaimDetailsMapperTest {

  ClaimDetailsMapper mapper = new ClaimDetailsMapperImpl();

  {
    ReflectionTestUtils.setField(mapper, "claimMapperHelper", new ClaimMapperHelper());
  }

  @Nested
  @DisplayName("Crime lower")
  class CrimeLower {

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
      assertThat(result.getProfitCosts().initialCalculated())
          .isEqualTo(claimResponse.getFeeCalculationResponse().getNetProfitCostsAmount());
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

  @Nested
  @DisplayName("Legal help")
  class LegalHelp {

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
      assertThat(result.getReportedLondonRateIndicator())
          .isEqualTo(claimResponse.getIsLondonRate());
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

      LegalHelpClaimDetails result =
          mapper.toLegalHelpClaimDetails(claimResponse, currentAssessment);

      assertThat(result.getFixedFee().initialCalculated())
          .isEqualTo(feeCalculation.getFixedFeeAmount());
      assertThat(result.getProfitCosts().reported())
          .isEqualTo(claimResponse.getNetProfitCostsAmount());
      assertThat(result.getProfitCosts().initialCalculated())
          .isEqualTo(feeCalculation.getNetProfitCostsAmount());
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
      assertThat(result.getCmrhOral().initialCalculated())
          .isEqualTo(boltOns.getBoltOnCmrhOralFee());
      assertThat(result.getCmrhTelephone().initialCalculated())
          .isEqualTo(boltOns.getBoltOnCmrhTelephoneFee());
      assertThat(result.getHomeOfficeInterview().initialCalculated())
          .isEqualTo(boltOns.getBoltOnHomeOfficeInterviewFee());
      assertThat(result.getSubstantiveHearing().initialCalculated())
          .isEqualTo(boltOns.getBoltOnSubstantiveHearingFee());
    }
  }

  @Nested
  @DisplayName("Mediation")
  class Mediation {

    @Test
    @DisplayName("Should map summary fields from the claim")
    void shouldMapSummaryFields() {
      ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.MEDIATION);

      MediationClaimDetails result = mapper.toMediationClaimDetails(claimResponse, null);

      assertThat(result.getClientForename()).isEqualTo(claimResponse.getClientForename());
      assertThat(result.getClientSurname()).isEqualTo(claimResponse.getClientSurname());
      assertThat(result.getUniqueClientNumber()).isEqualTo(claimResponse.getUniqueClientNumber());
      assertThat(result.getClient2Forename()).isEqualTo(claimResponse.getClient2Forename());
      assertThat(result.getClient2Surname()).isEqualTo(claimResponse.getClient2Surname());
      assertThat(result.getClient2UniqueClientNumber()).isEqualTo(claimResponse.getClient2Ucn());
      assertThat(result.getFeeCode()).isEqualTo(claimResponse.getFeeCode());
      assertThat(result.getFeeCodeDescription())
          .isEqualTo(claimResponse.getFeeCalculationResponse().getFeeCodeDescription());
      assertThat(result.getOfficeCode()).isEqualTo(claimResponse.getOfficeCode());
      assertThat(result.getDateSubmitted()).isEqualTo(claimResponse.getDateSubmitted());
      assertThat(result.getAreaOfLaw()).isEqualTo(AreaOfLaw.MEDIATION);
      assertThat(result.getMatterTypeCode()).isEqualTo(claimResponse.getMatterTypeCode());
      assertThat(result.getCaseStartDate()).isEqualTo(claimResponse.getCaseStartDate());
      assertThat(result.getCaseConcludedDate()).isEqualTo(claimResponse.getCaseConcludedDate());
      assertThat(result.getUniqueFileNumber()).isNull();
      assertThat(result.getEscapeCase()).isNull();
      assertThat(result.getProfitCosts()).isNull();
    }

    @Test
    @DisplayName("Should populate value fields from the claim and fee calculation response")
    void shouldMapValueFields() {
      ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.MEDIATION);
      var feeCalculation = claimResponse.getFeeCalculationResponse();

      MediationClaimDetails result = mapper.toMediationClaimDetails(claimResponse, null);

      assertThat(result.getFixedFee().initialCalculated())
          .isEqualTo(feeCalculation.getFixedFeeAmount());
      assertThat(result.getDisbursements().reported())
          .isEqualTo(claimResponse.getNetDisbursementAmount());
      assertThat(result.getDisbursements().initialCalculated())
          .isEqualTo(feeCalculation.getDisbursementAmount());
      assertThat(result.getDisbursementsVat().reported())
          .isEqualTo(claimResponse.getDisbursementsVatAmount());
      assertThat(result.getDisbursementsVat().initialCalculated())
          .isEqualTo(feeCalculation.getDisbursementVatAmount());
      assertThat(result.getVat().reported()).isEqualTo(claimResponse.getIsVatApplicable());
      assertThat(result.getTotalVat().initialCalculated())
          .isEqualTo(feeCalculation.getCalculatedVatAmount());
      assertThat(result.getTotalIncludingVat().initialCalculated())
          .isEqualTo(feeCalculation.getTotalAmount());
    }
  }
}
