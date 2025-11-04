package uk.gov.justice.laa.bulkclaim.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimRowCostsDetails;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.FeeCalculationPatch;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.FeeCalculationType;

@DisplayName("Submission claim row mapper test")
@ExtendWith(SpringExtension.class)
class SubmissionClaimRowMapperTest {

  private SubmissionClaimRowMapper mapper;

  @BeforeEach
  void setup() {
    mapper = new SubmissionClaimRowMapperImpl();
  }

  @Test
  @DisplayName("Should map submission claim")
  void shouldMapSubmissionClaim() {
    // Given
    ClaimResponse claimResponse =
        ClaimResponse.builder()
            .id("5146e93f-92c8-4c56-bd25-0cb6953f534")
            .lineNumber(1)
            .uniqueFileNumber("UFN123")
            .uniqueClientNumber("UCN123")
            .clientForename("Client")
            .clientSurname("name")
            .client2Forename("Second")
            .client2Surname("User")
            .client2Ucn("UCN999")
            .standardFeeCategoryCode("Family")
            .matterTypeCode("FAMD:FRES")
            .caseConcludedDate(LocalDate.of(2025, 3, 18).toString())
            // TODO: Check this is how the claim value is made up
            .netProfitCostsAmount(new BigDecimal("100.10"))
            .netCounselCostsAmount(new BigDecimal("100.10"))
            .netDisbursementAmount(new BigDecimal("100.10"))
            .netWaitingCostsAmount(new BigDecimal("100.10"))
            // TODO: Fee type is not available on OpenAPI spec.
            // .feeType("Fee type")
            .feeCode("Fee code")
            .feeCalculationResponse(
                FeeCalculationPatch.builder()
                    .feeType(FeeCalculationType.DISB_ONLY)
                    .feeCode("FC123")
                    .build())
            .build();
    // When
    SubmissionClaimRow result = mapper.toSubmissionClaimRow(claimResponse, 2);
    // Then
    SoftAssertions.assertSoftly(
        softAssertions -> {
          softAssertions
              .assertThat(result.id())
              .isEqualTo(UUID.fromString("5146e93f-92c8-4c56-bd25-0cb6953f534"));
          softAssertions.assertThat(result.lineNumber()).isEqualTo(1);
          softAssertions.assertThat(result.ufn()).isEqualTo("UFN123");
          softAssertions.assertThat(result.ucn()).isEqualTo("UCN123");
          softAssertions.assertThat(result.client()).isEqualTo("Client name");
          softAssertions.assertThat(result.clientForename()).isEqualTo("Client");
          softAssertions.assertThat(result.clientSurname()).isEqualTo("name");
          softAssertions.assertThat(result.client2Forename()).isEqualTo("Second");
          softAssertions.assertThat(result.client2Surname()).isEqualTo("User");
          softAssertions.assertThat(result.client2Ucn()).isEqualTo("UCN999");
          softAssertions.assertThat(result.category()).isEqualTo("Family");
          softAssertions.assertThat(result.matter()).isEqualTo("FAMD:FRES");
          softAssertions
              .assertThat(result.concludedOrClaimedDate())
              .isEqualTo(LocalDate.of(2025, 3, 18));
          softAssertions.assertThat(result.feeType()).isEqualTo("Disb only");
          softAssertions.assertThat(result.feeCode()).isEqualTo("FC123");
          softAssertions.assertThat(result.costsDetails()).isNotNull();
          softAssertions.assertThat(result.totalMessages()).isEqualTo(2);
          softAssertions.assertThat(result.escapeCase()).isNull();
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
            .feeCalculationResponse(
                FeeCalculationPatch.builder().totalAmount(new BigDecimal("1234.56")).build())
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

  @Test
  @DisplayName("Should map escape case flag when present")
  void shouldMapEscapeCaseFlagWhenPresent() {
    // Given
    ClaimResponse claimResponse = mock(ClaimResponse.class, RETURNS_DEEP_STUBS);
    when(claimResponse.getId()).thenReturn("5146e93f-92c8-4c56-bd25-0cb6953f534");
    when(claimResponse.getLineNumber()).thenReturn(1);
    when(claimResponse.getUniqueFileNumber()).thenReturn("UFN123");
    when(claimResponse.getUniqueClientNumber()).thenReturn("UCN123");
    when(claimResponse.getClientForename()).thenReturn("Client");
    when(claimResponse.getClientSurname()).thenReturn("Name");
    when(claimResponse.getStandardFeeCategoryCode()).thenReturn("Family");
    when(claimResponse.getMatterTypeCode()).thenReturn("FAMD:FRES");
    when(claimResponse.getCaseConcludedDate()).thenReturn(LocalDate.of(2025, 3, 18).toString());
    when(claimResponse.getFeeCalculationResponse().getFeeType())
        .thenReturn(FeeCalculationType.DISB_ONLY);
    when(claimResponse.getFeeCalculationResponse().getFeeCode()).thenReturn("FC123");
    when(claimResponse.getFeeCalculationResponse().getBoltOnDetails().getEscapeCaseFlag())
        .thenReturn(Boolean.TRUE);

    // When
    SubmissionClaimRow result = mapper.toSubmissionClaimRow(claimResponse, 0);

    // Then
    assertThat(result.escapeCase()).isTrue();
  }

  @Test
  @DisplayName("Should return null fee type when fee calculation type is null")
  void shouldReturnNullWhenFeeCalculationTypeIsNull() {
    assertThat(mapper.toFeeType(null)).isNull();
  }
}
