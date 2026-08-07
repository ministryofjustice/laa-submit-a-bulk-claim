package uk.gov.justice.laa.bulkclaim.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.MediationClaimDetails;
import uk.gov.justice.laa.bulkclaim.helper.TestObjectCreator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;

@DisplayName("Mediation claim details mapper test")
class MediationClaimDetailsMapperTest {

  MediationClaimDetailsMapper mapper = new MediationClaimDetailsMapperImpl();

  @Test
  @DisplayName("Should map summary fields from the claim")
  void shouldMapSummaryFields() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.MEDIATION);

    MediationClaimDetails result = mapper.toMediationClaimDetails(claimResponse);

    assertThat(result.client1Forename()).isEqualTo(claimResponse.getClientForename());
    assertThat(result.client1Surname()).isEqualTo(claimResponse.getClientSurname());
    assertThat(result.client1UniqueClientNumber()).isEqualTo(claimResponse.getUniqueClientNumber());
    assertThat(result.client2Forename()).isEqualTo(claimResponse.getClient2Forename());
    assertThat(result.client2Surname()).isEqualTo(claimResponse.getClient2Surname());
    assertThat(result.client2UniqueClientNumber()).isEqualTo(claimResponse.getClient2Ucn());
    assertThat(result.feeCode()).isEqualTo(claimResponse.getFeeCode());
    assertThat(result.feeCodeDescription())
        .isEqualTo(claimResponse.getFeeCalculationResponse().getFeeCodeDescription());
    assertThat(result.officeCode()).isEqualTo(claimResponse.getOfficeCode());
    assertThat(result.dateSubmitted()).isEqualTo(claimResponse.getDateSubmitted());
    assertThat(result.areaOfLaw()).isEqualTo(AreaOfLaw.MEDIATION);
    assertThat(result.matterTypeCode()).isEqualTo(claimResponse.getMatterTypeCode());
    assertThat(result.caseStartDate()).isEqualTo(claimResponse.getCaseStartDate());
    assertThat(result.caseConcludedDate()).isEqualTo(claimResponse.getCaseConcludedDate());
  }

  @Test
  @DisplayName("Should map reported and initial calculated values")
  void shouldMapValues() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.MEDIATION);
    var feeCalculation = claimResponse.getFeeCalculationResponse();

    MediationClaimDetails result = mapper.toMediationClaimDetails(claimResponse);

    assertThat(result.reportedDisbursements()).isEqualTo(claimResponse.getNetDisbursementAmount());
    assertThat(result.reportedDisbursementsVat())
        .isEqualTo(claimResponse.getDisbursementsVatAmount());
    assertThat(result.reportedVatApplicable()).isEqualTo(claimResponse.getIsVatApplicable());
    assertThat(result.initialCalculatedFixedFee()).isEqualTo(feeCalculation.getFixedFeeAmount());
    assertThat(result.initialCalculatedDisbursements())
        .isEqualTo(feeCalculation.getDisbursementAmount());
    assertThat(result.initialCalculatedDisbursementsVat())
        .isEqualTo(feeCalculation.getDisbursementVatAmount());
    assertThat(result.initialCalculatedVatIndicator()).isEqualTo(feeCalculation.getVatIndicator());
    assertThat(result.initialCalculatedTotalVat())
        .isEqualTo(feeCalculation.getCalculatedVatAmount());
    assertThat(result.initialCalculatedTotalIncludingVat())
        .isEqualTo(feeCalculation.getTotalAmount());
  }
}
