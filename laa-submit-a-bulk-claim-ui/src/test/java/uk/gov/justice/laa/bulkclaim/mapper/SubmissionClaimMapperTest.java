package uk.gov.justice.laa.bulkclaim.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionClaimRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionClaimRowCostsDetails;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;

@DisplayName("Submission claim mapper test")
@ExtendWith(SpringExtension.class)
class SubmissionClaimMapperTest {

  private SubmissionClaimMapper mapper;

  @BeforeEach
  void setup() {
    mapper = new SubmissionClaimMapperImpl();
  }

  @Test
  @DisplayName("Should map submission claim")
  void shouldMapSubmissionClaim() {
    // Given
    ClaimResponse claimResponse =
        ClaimResponse.builder()
            .lineNumber(1)
            .uniqueFileNumber("UFN123")
            .uniqueClientNumber("UCN123")
            .clientForename("Client")
            .clientSurname("name")
            .standardFeeCategoryCode("Family")
            .matterTypeCode("FAMD:FRES")
            .caseConcludedDate(LocalDate.of(2025, 3, 18))
            // TODO: Check this is how the claim value is made up
            .netProfitCostsAmount(new BigDecimal("100.10"))
            .netCounselCostsAmount(new BigDecimal("100.10"))
            .netDisbursementAmount(new BigDecimal("100.10"))
            .netWaitingCostsAmount(new BigDecimal("100.10"))
            // TODO: Fee type is not available on OpenAPI spec.
            // .feeType("Fee type")
            .feeCode("Fee code")
            .build();
    // When
    SubmissionClaimRow result = mapper.toSubmissionClaimRow(claimResponse);
    // Then
    SoftAssertions.assertSoftly(
        softAssertions -> {
          softAssertions.assertThat(result.lineNumber()).isEqualTo(1);
          softAssertions.assertThat(result.ufn()).isEqualTo("UFN123");
          softAssertions.assertThat(result.ucn()).isEqualTo("UCN123");
          softAssertions.assertThat(result.client()).isEqualTo("Client name");
          softAssertions.assertThat(result.category()).isEqualTo("Family");
          softAssertions.assertThat(result.matter()).isEqualTo("FAMD:FRES");
          softAssertions
              .assertThat(result.concludedOrClaimedDate())
              .isEqualTo(LocalDate.of(2025, 3, 18));
          softAssertions.assertThat(result.feeType()).isEqualTo("Fee type");
          softAssertions.assertThat(result.feeCode()).isEqualTo("Fee code");
          softAssertions.assertThat(result.costsDetails()).isNotNull();
        });
  }

  @Test
  @DisplayName("Should map claim costs details")
  void shouldMapClaimCostsDetails() {
    // Given
    ClaimResponse claimResponse =
        ClaimResponse.builder()
            .netProfitCostsAmount(new BigDecimal("100.10"))
            .netCounselCostsAmount(new BigDecimal("200.20"))
            .netDisbursementAmount(new BigDecimal("300.30"))
            .disbursementsVatAmount(new BigDecimal("17.50"))
            .netWaitingCostsAmount(new BigDecimal("400.40"))
            .travelWaitingCostsAmount(new BigDecimal("500.50"))
            .totalValue(new BigDecimal("1234.56"))
            .build();
    // When
    SubmissionClaimRowCostsDetails result = mapper.toSubmissionClaimRowCostsDetails(claimResponse);
    // Then
    SoftAssertions.assertSoftly(
        softAssertions -> {
          softAssertions
              .assertThat(result.netProfitCostsAmount())
              .isEqualTo(new BigDecimal("100.10"));
          softAssertions
              .assertThat(result.netCounselCostsAmount())
              .isEqualTo(new BigDecimal("200.20"));
          softAssertions
              .assertThat(result.netDisbursementAmount())
              .isEqualTo(new BigDecimal("300.30"));
          softAssertions
              .assertThat(result.disbursementsVatAmount())
              .isEqualTo(new BigDecimal("17.50"));
          softAssertions
              .assertThat(result.netWaitingCostsAmount())
              .isEqualTo(new BigDecimal("400.40"));
          softAssertions
              .assertThat(result.travelWaitingCostsAmount())
              .isEqualTo(new BigDecimal("500.50"));
          softAssertions.assertThat(result.claimValue()).isEqualTo(new BigDecimal("1234.56"));
        });
  }
}
