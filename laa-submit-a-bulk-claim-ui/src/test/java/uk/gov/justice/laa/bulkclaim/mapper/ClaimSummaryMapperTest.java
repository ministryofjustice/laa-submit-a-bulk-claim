package uk.gov.justice.laa.bulkclaim.mapper;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.ClaimSummary;
import uk.gov.justice.laa.bulkclaim.helper.TestObjectCreator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@DisplayName("Claim summary mapper test")
class ClaimSummaryMapperTest {

  ClaimSummaryMapper mapper = new ClaimSummaryMapperImpl();

  @Test
  @DisplayName("Should map all details")
  void shouldMapAllDetails() {
    // Given
    String areaOfLaw = "CIVIL";
    String officeAccountNumber = "0P322F";
    OffsetDateTime submissionDate= OffsetDateTime.of(2025, 5, 5, 6, 52, 27, 954000000, ZoneOffset.UTC);
    ClaimResponse claimResponse = TestObjectCreator.buildClaimResponse();
    // When
    ClaimSummary result = mapper.toClaimSummary(claimResponse, areaOfLaw, officeAccountNumber, submissionDate);
    // Then
    SoftAssertions.assertSoftly(
        softAssertions -> {
          softAssertions.assertThat(result.areaOfLaw()).isEqualTo(areaOfLaw);
          softAssertions
              .assertThat(result.matterTypeCode())
              .isEqualTo(claimResponse.getMatterTypeCode());
          softAssertions.assertThat(result.feeCode()).isEqualTo(claimResponse.getFeeCode());
          softAssertions
              .assertThat(result.clientForename())
              .isEqualTo(claimResponse.getClientForename());
          softAssertions
              .assertThat(result.clientSurname())
              .isEqualTo(claimResponse.getClientSurname());
          softAssertions
              .assertThat(result.uniqueClientNumber())
              .isEqualTo(claimResponse.getUniqueClientNumber());
          softAssertions
              .assertThat(result.client2Forename())
              .isEqualTo(claimResponse.getClient2Forename());
          softAssertions
              .assertThat(result.client2Surname())
              .isEqualTo(claimResponse.getClient2Surname());
          softAssertions
              .assertThat(result.uniqueClientNumber2())
              .isEqualTo(claimResponse.getClient2Ucn());
          softAssertions
              .assertThat(result.stageReachedCode())
              .isEqualTo(claimResponse.getStageReachedCode());
          softAssertions
              .assertThat(result.uniqueFileNumber())
              .isEqualTo(claimResponse.getUniqueFileNumber());
          softAssertions.assertThat(result.outcomeCode()).isEqualTo(claimResponse.getOutcomeCode());
          softAssertions
              .assertThat(result.caseConcludedDate())
              .isEqualTo(claimResponse.getCaseConcludedDate());
          softAssertions.assertThat(result.isEscaped()).isTrue();
        });
  }
}
