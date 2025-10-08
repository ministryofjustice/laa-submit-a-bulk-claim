package uk.gov.justice.laa.bulkclaim.mapper;

import java.math.BigDecimal;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimFeeSubmittedDetails;
import uk.gov.justice.laa.bulkclaim.helper.TestObjectCreator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;

@DisplayName("Submission claim details mapper test")
@ExtendWith(SpringExtension.class)
class ClaimFeeDetailsMapperTest {

  private ClaimFeeDetailsMapper mapper;

  @BeforeEach
  void setup() {
    mapper = new ClaimFeeDetailsMapperImpl();
  }

  @Test
  @DisplayName("Should map submitted fee details")
  void shouldMapSubmittedFeeDetails() {
    // Given
    ClaimResponse claimResponse = TestObjectCreator.buildClaimResponse();
    // When
    SubmissionClaimFeeSubmittedDetails result = mapper.toSubmittedFeeDetails(claimResponse);
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
