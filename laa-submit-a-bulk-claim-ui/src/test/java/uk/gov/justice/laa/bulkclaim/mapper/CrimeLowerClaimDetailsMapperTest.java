package uk.gov.justice.laa.bulkclaim.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;
import uk.gov.justice.laa.bulkclaim.helper.TestObjectCreator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;

@DisplayName("Crime lower claim details mapper test")
class CrimeLowerClaimDetailsMapperTest {

  CrimeLowerClaimDetailsMapper mapper = new CrimeLowerClaimDetailsMapperImpl();

  @Test
  @DisplayName("Should map summary fields from the claim")
  void shouldMapSummaryFields() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.CRIME_LOWER);

    CrimeLowerClaimDetails result = mapper.toCrimeLowerClaimDetails(claimResponse);

    assertThat(result.clientForename()).isEqualTo(claimResponse.getClientForename());
    assertThat(result.clientSurname()).isEqualTo(claimResponse.getClientSurname());
    assertThat(result.uniqueFileNumber()).isEqualTo(claimResponse.getUniqueFileNumber());
    assertThat(result.officeCode()).isEqualTo(claimResponse.getOfficeCode());
    assertThat(result.dateSubmitted()).isEqualTo(claimResponse.getDateSubmitted());
    assertThat(result.areaOfLaw()).isEqualTo(AreaOfLaw.CRIME_LOWER);
    assertThat(result.feeCode()).isEqualTo(claimResponse.getFeeCode());
    assertThat(result.feeCodeDescription())
        .isEqualTo(claimResponse.getFeeCalculationResponse().getFeeCodeDescription());
    assertThat(result.matterTypeCode()).isEqualTo(claimResponse.getMatterTypeCode());
    assertThat(result.crimeMatterTypeCode()).isEqualTo(claimResponse.getCrimeMatterTypeCode());
    assertThat(result.representationOrderDate())
        .isEqualTo(claimResponse.getRepresentationOrderDate());
    assertThat(result.stageReachedCode()).isEqualTo(claimResponse.getStageReachedCode());
    assertThat(result.outcomeCode()).isEqualTo(claimResponse.getOutcomeCode());
    assertThat(result.caseConcludedDate()).isEqualTo(claimResponse.getCaseConcludedDate());
    assertThat(result.escapeCase()).isTrue();
  }

  @Test
  @DisplayName("Should map reported values from the claim")
  void shouldMapReportedValues() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.CRIME_LOWER);

    CrimeLowerClaimDetails result = mapper.toCrimeLowerClaimDetails(claimResponse);

    assertThat(result.reportedProfitCosts()).isEqualTo(claimResponse.getNetProfitCostsAmount());
    assertThat(result.reportedDisbursements()).isEqualTo(claimResponse.getNetDisbursementAmount());
    assertThat(result.reportedDisbursementsVat())
        .isEqualTo(claimResponse.getDisbursementsVatAmount());
    assertThat(result.reportedTravelCosts()).isEqualTo(claimResponse.getTravelWaitingCostsAmount());
    assertThat(result.reportedWaitingCosts()).isEqualTo(claimResponse.getNetWaitingCostsAmount());
    assertThat(result.reportedVatApplicable()).isEqualTo(claimResponse.getIsVatApplicable());
  }

  @Test
  @DisplayName("Should map initial calculated values from the fee calculation response")
  void shouldMapInitialCalculatedValues() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.CRIME_LOWER);
    var feeCalculation = claimResponse.getFeeCalculationResponse();

    CrimeLowerClaimDetails result = mapper.toCrimeLowerClaimDetails(claimResponse);

    assertThat(result.initialCalculatedFixedFee()).isEqualTo(feeCalculation.getFixedFeeAmount());
    assertThat(result.initialCalculatedProfitCosts())
        .isEqualTo(feeCalculation.getNetProfitCostsAmount());
    assertThat(result.initialCalculatedDisbursements())
        .isEqualTo(feeCalculation.getDisbursementAmount());
    assertThat(result.initialCalculatedDisbursementsVat())
        .isEqualTo(feeCalculation.getDisbursementVatAmount());
    assertThat(result.initialCalculatedTravelCosts())
        .isEqualTo(feeCalculation.getNetTravelCostsAmount());
    assertThat(result.initialCalculatedWaitingCosts())
        .isEqualTo(feeCalculation.getNetWaitingCostsAmount());
    assertThat(result.initialCalculatedVatIndicator()).isEqualTo(feeCalculation.getVatIndicator());
    assertThat(result.initialCalculatedTotalVat())
        .isEqualTo(feeCalculation.getCalculatedVatAmount());
    assertThat(result.initialCalculatedTotalIncludingVat())
        .isEqualTo(feeCalculation.getTotalAmount());
  }
}
