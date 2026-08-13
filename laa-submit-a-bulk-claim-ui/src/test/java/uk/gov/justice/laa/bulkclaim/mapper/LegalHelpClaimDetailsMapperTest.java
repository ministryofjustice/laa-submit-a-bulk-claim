package uk.gov.justice.laa.bulkclaim.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.LegalHelpClaimDetails;
import uk.gov.justice.laa.bulkclaim.helper.TestObjectCreator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;

@DisplayName("Legal help claim details mapper test")
class LegalHelpClaimDetailsMapperTest {

  LegalHelpClaimDetailsMapper mapper = new LegalHelpClaimDetailsMapperImpl();

  @Test
  @DisplayName("Should map summary fields from the claim")
  void shouldMapSummaryFields() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.LEGAL_HELP);

    LegalHelpClaimDetails result = mapper.toLegalHelpClaimDetails(claimResponse);

    assertThat(result.clientForename()).isEqualTo(claimResponse.getClientForename());
    assertThat(result.clientSurname()).isEqualTo(claimResponse.getClientSurname());
    assertThat(result.uniqueFileNumber()).isEqualTo(claimResponse.getUniqueFileNumber());
    assertThat(result.officeCode()).isEqualTo(claimResponse.getOfficeCode());
    assertThat(result.dateSubmitted()).isEqualTo(claimResponse.getDateSubmitted());
    assertThat(result.areaOfLaw()).isEqualTo(AreaOfLaw.LEGAL_HELP);
    assertThat(result.categoryOfLaw())
        .isEqualTo(claimResponse.getFeeCalculationResponse().getCategoryOfLaw());
    assertThat(result.feeCode()).isEqualTo(claimResponse.getFeeCode());
    assertThat(result.feeCodeDescription())
        .isEqualTo(claimResponse.getFeeCalculationResponse().getFeeCodeDescription());
    assertThat(result.matterTypeCodeOne())
        .isEqualTo(claimResponse.getMatterTypeCode().split(":")[0]);
    assertThat(result.matterTypeCodeTwo())
        .isEqualTo(claimResponse.getMatterTypeCode().split(":")[1]);
    assertThat(result.caseStartDate()).isEqualTo(claimResponse.getCaseStartDate());
    assertThat(result.caseConcludedDate()).isEqualTo(claimResponse.getCaseConcludedDate());
    assertThat(result.escapeCase()).isTrue();
  }

  @Test
  @DisplayName("Should map reported values from the claim")
  void shouldMapReportedValues() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.LEGAL_HELP);

    LegalHelpClaimDetails result = mapper.toLegalHelpClaimDetails(claimResponse);

    assertThat(result.reportedProfitCosts()).isEqualTo(claimResponse.getNetProfitCostsAmount());
    assertThat(result.reportedDisbursements()).isEqualTo(claimResponse.getNetDisbursementAmount());
    assertThat(result.reportedDisbursementsVat())
        .isEqualTo(claimResponse.getDisbursementsVatAmount());
    assertThat(result.reportedTravelAndWaitingCosts())
        .isEqualTo(claimResponse.getTravelWaitingCostsAmount());
    assertThat(result.reportedVatApplicable()).isEqualTo(claimResponse.getIsVatApplicable());
  }

  @Test
  @DisplayName("Should map initial calculated values from the fee calculation response")
  void shouldMapInitialCalculatedValues() {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.LEGAL_HELP);
    var feeCalculation = claimResponse.getFeeCalculationResponse();
    var boltOns = feeCalculation.getBoltOnDetails();

    LegalHelpClaimDetails result = mapper.toLegalHelpClaimDetails(claimResponse);

    assertThat(result.initialCalculatedFixedFee()).isEqualTo(feeCalculation.getFixedFeeAmount());
    assertThat(result.initialCalculatedProfitCosts())
        .isEqualTo(feeCalculation.getNetProfitCostsAmount());
    assertThat(result.initialCalculatedDisbursements())
        .isEqualTo(feeCalculation.getDisbursementAmount());
    assertThat(result.initialCalculatedDisbursementsVat())
        .isEqualTo(feeCalculation.getDisbursementVatAmount());
    assertThat(result.initialCalculatedCounselsCosts())
        .isEqualTo(feeCalculation.getNetCostOfCounselAmount());
    assertThat(result.initialCalculatedTravelAndWaitingCosts())
        .isEqualTo(feeCalculation.getTravelAndWaitingCostsAmount());
    assertThat(result.initialCalculatedDetentionTravelWaitingCosts())
        .isEqualTo(feeCalculation.getDetentionTravelAndWaitingCostsAmount());
    assertThat(result.initialCalculatedJrFormFilling())
        .isEqualTo(feeCalculation.getJrFormFillingAmount());
    assertThat(result.initialCalculatedAdjournedHearingFee())
        .isEqualTo(boltOns.getBoltOnAdjournedHearingFee());
    assertThat(result.initialCalculatedCmrhOral()).isEqualTo(boltOns.getBoltOnCmrhOralFee());
    assertThat(result.initialCalculatedCmrhTelephone())
        .isEqualTo(boltOns.getBoltOnCmrhTelephoneFee());
    assertThat(result.initialCalculatedHomeOfficeInterview())
        .isEqualTo(boltOns.getBoltOnHomeOfficeInterviewFee());
    assertThat(result.initialCalculatedSubstantiveHearing())
        .isEqualTo(boltOns.getBoltOnSubstantiveHearingFee());
    assertThat(result.initialCalculatedVatIndicator()).isEqualTo(feeCalculation.getVatIndicator());
    assertThat(result.initialCalculatedTotalVat())
        .isEqualTo(feeCalculation.getCalculatedVatAmount());
    assertThat(result.initialCalculatedTotalIncludingVat())
        .isEqualTo(feeCalculation.getTotalAmount());
  }
}
