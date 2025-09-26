package uk.gov.justice.laa.bulkclaim.mapper;

import java.math.BigDecimal;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimFeeCalculationDetails;
import uk.gov.justice.laa.bulkclaim.helper.TestObjectCreator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimStatus;

@DisplayName("Submission claim details mapper test")
@ExtendWith(SpringExtension.class)
class SubmissionClaimDetailsMapperTest {

  private SubmissionClaimDetailsMapper mapper;

  @BeforeEach
  void setup() {
    mapper = new SubmissionClaimDetailsMapperImpl();
  }

  @Test
  @DisplayName("Should map submission claim details")
  void shouldMapSubmissionClaim() {
    // Given
    ClaimResponse claimResponse = TestObjectCreator.buildClaimResponse();
    // When
    SubmissionClaimDetails result = mapper.toSubmissionClaimDetails(claimResponse);
    // Then
    SoftAssertions.assertSoftly(
        softAssertions -> {
          softAssertions
              .assertThat(result.id())
              .isEqualTo(UUID.fromString("6d10189b-b020-4cc5-891b-e53bbefa501a"));
          softAssertions.assertThat(result.status()).isEqualTo(ClaimStatus.READY_TO_PROCESS);
          softAssertions
              .assertThat(result.scheduleReference())
              .isEqualTo("schedule-reference-value");
          softAssertions.assertThat(result.lineNumber()).isEqualTo(1);
          softAssertions
              .assertThat(result.caseReferenceNumber())
              .isEqualTo("case-reference-number");
          softAssertions.assertThat(result.uniqueFileNumber()).isEqualTo("unique-file-number");
          softAssertions.assertThat(result.caseStartDate()).isEqualTo("2025-09-19");
          softAssertions.assertThat(result.caseConcludedDate()).isEqualTo("2026-10-20");
          softAssertions.assertThat(result.matterTypeCode()).isEqualTo("matter-type-code");
          softAssertions
              .assertThat(result.crimeMatterTypeCode())
              .isEqualTo("crime-matter-type-code");
          softAssertions.assertThat(result.feeSchemeCode()).isEqualTo("fee-scheme-code");
          softAssertions.assertThat(result.feeCode()).isEqualTo("fee-code");
          softAssertions
              .assertThat(result.procurementAreaCode())
              .isEqualTo("procurement-area-code");
          softAssertions.assertThat(result.accessPointCode()).isEqualTo("access-point-code");
          softAssertions.assertThat(result.deliveryLocation()).isEqualTo("delivery-location-value");
          softAssertions.assertThat(result.representationOrderDate()).isEqualTo("2027-11-21");
          softAssertions.assertThat(result.suspectsDefendantsCount()).isEqualTo(2);
          softAssertions.assertThat(result.policeStationCourtAttendancesCount()).isEqualTo(3);
          softAssertions
              .assertThat(result.policeStationCourtPrisonId())
              .isEqualTo("police-station-court-prison-id");
          softAssertions.assertThat(result.dsccNumber()).isEqualTo("dscc-number");
          softAssertions.assertThat(result.maatId()).isEqualTo("maat-id");
          softAssertions
              .assertThat(result.prisonLawPriorApprovalNumber())
              .isEqualTo("prison-law-prior-approval-number");
          softAssertions.assertThat(result.isDutySolicitor()).isTrue();
          softAssertions.assertThat(result.isYouthCourt()).isTrue();
          softAssertions.assertThat(result.schemeId()).isEqualTo("scheme-id");
          softAssertions.assertThat(result.mediationSessionsCount()).isEqualTo(4);
          softAssertions.assertThat(result.mediationTimeMinutes()).isEqualTo(5);
          softAssertions.assertThat(result.outreachLocation()).isEqualTo("outreach-location");
          softAssertions.assertThat(result.referralSource()).isEqualTo("referral-source");
          softAssertions.assertThat(result.totalValue()).isEqualTo(new BigDecimal("1234.56"));
          softAssertions.assertThat(result.clientForename()).isEqualTo("client-forename");
          softAssertions.assertThat(result.clientSurname()).isEqualTo("client-surname");
          softAssertions.assertThat(result.clientDateOfBirth()).isEqualTo("1995-01-01");
          softAssertions.assertThat(result.uniqueClientNumber()).isEqualTo("unique-client-number");
          softAssertions.assertThat(result.clientPostcode()).isEqualTo("client-postcode");
          softAssertions.assertThat(result.genderCode()).isEqualTo("gender-code");
          softAssertions.assertThat(result.ethnicityCode()).isEqualTo("ethnicity-code");
          softAssertions.assertThat(result.disabilityCode()).isEqualTo("disability-code");
          softAssertions.assertThat(result.isLegallyAided()).isTrue();
          softAssertions.assertThat(result.clientTypeCode()).isEqualTo("client-type-code");
          softAssertions
              .assertThat(result.homeOfficeClientNumber())
              .isEqualTo("home-office-client-number");
          softAssertions.assertThat(result.claReferenceNumber()).isEqualTo("cla-reference-number");
          softAssertions.assertThat(result.claExemptionCode()).isEqualTo("cla-exemption-code");
          softAssertions.assertThat(result.client2Forename()).isEqualTo("client-2-forename");
          softAssertions.assertThat(result.client2Surname()).isEqualTo("client-2-surname");
          softAssertions.assertThat(result.client2DateOfBirth()).isEqualTo("1999-05-02");
          softAssertions.assertThat(result.client2Ucn()).isEqualTo("client-2-ucn");
          softAssertions.assertThat(result.client2Postcode()).isEqualTo("client-2-postcode");
          softAssertions.assertThat(result.client2GenderCode()).isEqualTo("client-2-gender-code");
          softAssertions
              .assertThat(result.client2EthnicityCode())
              .isEqualTo("client-2-ethnicity-code");
          softAssertions
              .assertThat(result.client2DisabilityCode())
              .isEqualTo("client-2-disability-code");
          softAssertions.assertThat(result.client2IsLegallyAided()).isTrue();
          softAssertions.assertThat(result.caseId()).isEqualTo("case-id");
          softAssertions.assertThat(result.uniqueCaseId()).isEqualTo("unique-case-id");
          softAssertions.assertThat(result.caseStageCode()).isEqualTo("case-stage-code");
          softAssertions.assertThat(result.stageReachedCode()).isEqualTo("stage-reached-code");
          softAssertions
              .assertThat(result.standardFeeCategoryCode())
              .isEqualTo("standard-fee-category-code");
          softAssertions.assertThat(result.outcomeCode()).isEqualTo("outcome-code");
          softAssertions
              .assertThat(result.designatedAccreditedRepresentativeCode())
              .isEqualTo("designated-accredited-representative-code");
          softAssertions.assertThat(result.isPostalApplicationAccepted()).isTrue();
          softAssertions.assertThat(result.isClient2PostalApplicationAccepted()).isTrue();
          softAssertions
              .assertThat(result.mentalHealthTribunalReference())
              .isEqualTo("mental-health-tribunal-reference");
          softAssertions.assertThat(result.isNrmAdvice()).isTrue();
          softAssertions.assertThat(result.followOnWork()).isEqualTo("follow-on-work");
          softAssertions.assertThat(result.transferDate()).isEqualTo("2027-04-24");
          softAssertions
              .assertThat(result.exemptionCriteriaSatisfied())
              .isEqualTo("exemption-criteria-satisfied");
          softAssertions
              .assertThat(result.exceptionalCaseFundingReference())
              .isEqualTo("exceptional-case-funding-reference");
          softAssertions.assertThat(result.isLegacyCase()).isTrue();
          softAssertions.assertThat(result.isToleranceApplicable()).isTrue();
          softAssertions
              .assertThat(result.priorAuthorityReference())
              .isEqualTo("prior-authority-reference");
          softAssertions.assertThat(result.isAdditionalTravelPayment()).isTrue();
          softAssertions
              .assertThat(result.meetingsAttendedCode())
              .isEqualTo("meetings-attended-code");
          softAssertions.assertThat(result.isEligibleClient()).isTrue();
          softAssertions.assertThat(result.courtLocationCode()).isEqualTo("court-location-code");
          softAssertions.assertThat(result.adviceTypeCode()).isEqualTo("advice-type-code");
          softAssertions.assertThat(result.medicalReportsCount()).isEqualTo(5);
          softAssertions.assertThat(result.isIrcSurgery()).isTrue();
          softAssertions.assertThat(result.surgeryDate()).isEqualTo("2017-04-23");
          softAssertions.assertThat(result.surgeryClientsCount()).isEqualTo(6);
          softAssertions.assertThat(result.surgeryMattersCount()).isEqualTo(7);
          softAssertions.assertThat(result.cmrhOralCount()).isEqualTo(1);
          softAssertions.assertThat(result.cmrhTelephoneCount()).isEqualTo(2);
          softAssertions
              .assertThat(result.aitHearingCentreCode())
              .isEqualTo("ait-hearing-centre-code");
          softAssertions.assertThat(result.isSubstantiveHearing()).isTrue();
          softAssertions.assertThat(result.hoInterview()).isEqualTo(3);
          softAssertions
              .assertThat(result.localAuthorityNumber())
              .isEqualTo("local-authority-number");
        });
  }

  @Test
  @DisplayName("Should map fee calculation details")
  void shouldMapFeeCalculationDetails() {
    // Given
    ClaimResponse claimResponse = TestObjectCreator.buildClaimResponse();
    // When
    SubmissionClaimFeeCalculationDetails result = mapper.toFeeCalculationDetails(claimResponse);
    // Then
    SoftAssertions.assertSoftly(
        softAssertions -> {
          softAssertions.assertThat(result.totalValue()).isEqualTo(new BigDecimal("1234.56"));
          softAssertions.assertThat(result.adviceTime()).isEqualTo(6);
          softAssertions.assertThat(result.travelTime()).isEqualTo(7);
          softAssertions.assertThat(result.waitingTime()).isEqualTo(8);
          softAssertions
              .assertThat(result.netProfitCostsAmount())
              .isEqualTo(new BigDecimal("100.10"));
          softAssertions
              .assertThat(result.netDisbursementAmount())
              .isEqualTo(new BigDecimal("200.20"));
          softAssertions
              .assertThat(result.netCounselCostsAmount())
              .isEqualTo(new BigDecimal("300.30"));
          softAssertions
              .assertThat(result.disbursementsVatAmount())
              .isEqualTo(new BigDecimal("17.50"));
          softAssertions
              .assertThat(result.travelWaitingCostsAmount())
              .isEqualTo(new BigDecimal("500.50"));
          softAssertions
              .assertThat(result.netWaitingCostsAmount())
              .isEqualTo(new BigDecimal("400.40"));
          softAssertions.assertThat(result.isVatApplicable()).isTrue();
          softAssertions.assertThat(result.isLondonRate()).isTrue();
          softAssertions.assertThat(result.adjournedHearingFeeAmount()).isEqualTo(9);
          softAssertions
              .assertThat(result.costsDamagesRecoveredAmount())
              .isEqualTo(new BigDecimal("600.60"));
          softAssertions
              .assertThat(result.detentionTravelWaitingCostsAmount())
              .isEqualTo(new BigDecimal("700.70"));
          softAssertions
              .assertThat(result.jrFormFillingAmount())
              .isEqualTo(new BigDecimal("800.80"));
        });
  }
}
