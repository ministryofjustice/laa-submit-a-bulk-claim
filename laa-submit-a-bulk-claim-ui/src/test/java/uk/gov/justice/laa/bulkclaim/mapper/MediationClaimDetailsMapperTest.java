package uk.gov.justice.laa.bulkclaim.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.MediationClaimDetails;
import uk.gov.justice.laa.bulkclaim.helper.TestObjectCreator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;

@DisplayName("Mediation claim details mapper test")
class MediationClaimDetailsMapperTest {

  MediationClaimDetailsMapper mapper = new MediationClaimDetailsMapperImpl();

  {
    ReflectionTestUtils.setField(mapper, "claimMapperHelper", new ClaimMapperHelper());
  }

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
