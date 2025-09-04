package uk.gov.justice.laa.bulkclaim.builder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionClaimRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionClaimRowCostsDetails;
import uk.gov.justice.laa.bulkclaim.mapper.SubmissionClaimMapper;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.claims.model.ClaimFields;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;
import uk.gov.justice.laa.claims.model.GetSubmission200ResponseClaimsInner;

@ExtendWith(MockitoExtension.class)
@DisplayName("Submission claim details builder tests")
class SubmissionClaimDetailsBuilderTest {

  private SubmissionClaimDetailsBuilder builder;

  @Mock DataClaimsRestService dataClaimsRestService;
  @Mock SubmissionClaimMapper submissionClaimMapper;

  @BeforeEach
  void beforeEach() {
    builder = new SubmissionClaimDetailsBuilder(dataClaimsRestService, submissionClaimMapper);
  }

  @Test
  @DisplayName("Should map claim rows to submission claim details")
  void shouldMapClaimRowsToSubmissionClaimDetails() {
    // Given
    UUID submissionId = UUID.fromString("45bf06e5-2298-4163-adc0-a6134b48f213");
    UUID claimId = UUID.fromString("87fdac7e-6de4-4a98-a788-e89a9d1c0225");
    GetSubmission200Response getSubmission200Response =
        GetSubmission200Response.builder()
            .submissionId(submissionId)
            .claims(List.of(GetSubmission200ResponseClaimsInner.builder().claimId(claimId).build()))
            .build();
    when(dataClaimsRestService.getSubmissionClaim(submissionId, claimId))
        .thenReturn(Mono.just(ClaimFields.builder().build()));
    SubmissionClaimRow expected =
        new SubmissionClaimRow(
            1,
            "ufn",
            "ucn",
            "client",
            "cat",
            "matter",
            LocalDate.of(2025, 5, 1),
            "feeType",
            "feeCode",
            new SubmissionClaimRowCostsDetails(
                new BigDecimal("10.10"),
                new BigDecimal("20.10"),
                new BigDecimal("30.10"),
                new BigDecimal("40.10"),
                new BigDecimal("50.10"),
                new BigDecimal("60.10"),
                new BigDecimal("70.10")));
    when(submissionClaimMapper.toSubmissionClaimRow(any())).thenReturn(expected);
    // When
    SubmissionClaimDetails result = builder.build(getSubmission200Response);
    // Then
    assertThat(result.submissionClaims().contains(expected)).isTrue();
  }

  @Test
  @DisplayName("Should calculate cost summary")
  void shouldCalculateCostSummary() {
    // Given
    UUID submissionId = UUID.fromString("45bf06e5-2298-4163-adc0-a6134b48f213");
    UUID claimId = UUID.fromString("87fdac7e-6de4-4a98-a788-e89a9d1c0225");
    GetSubmission200Response getSubmission200Response =
        GetSubmission200Response.builder()
            .submissionId(submissionId)
            .claims(
                List.of(
                    GetSubmission200ResponseClaimsInner.builder().claimId(claimId).build(),
                    GetSubmission200ResponseClaimsInner.builder().claimId(claimId).build()))
            .build();
    when(dataClaimsRestService.getSubmissionClaim(submissionId, claimId))
        .thenReturn(Mono.just(ClaimFields.builder().build()));
    SubmissionClaimRow claimOne =
        new SubmissionClaimRow(
            1,
            "ufn",
            "ucn",
            "client",
            "cat",
            "matter",
            LocalDate.of(2025, 5, 1),
            "feeType",
            "feeCode",
            new SubmissionClaimRowCostsDetails(
                new BigDecimal("10.10"),
                new BigDecimal("20.10"),
                new BigDecimal("30.10"),
                new BigDecimal("40.10"),
                new BigDecimal("50.10"),
                new BigDecimal("60.10"),
                new BigDecimal("70.10")));
    SubmissionClaimRow claimTwo =
        new SubmissionClaimRow(
            1,
            "ufn",
            "ucn",
            "client",
            "cat",
            "matter",
            LocalDate.of(2025, 5, 1),
            "feeType",
            "feeCode",
            new SubmissionClaimRowCostsDetails(
                new BigDecimal("11.10"),
                new BigDecimal("21.10"),
                new BigDecimal("31.10"),
                new BigDecimal("41.10"),
                new BigDecimal("51.10"),
                new BigDecimal("61.10"),
                new BigDecimal("71.10")));
    when(submissionClaimMapper.toSubmissionClaimRow(any())).thenReturn(claimOne);
    when(submissionClaimMapper.toSubmissionClaimRow(any())).thenReturn(claimTwo);
    // When
    SubmissionClaimDetails result = builder.build(getSubmission200Response);
    // Then
    assertThat(result.costsSummary().profitCosts()).isEqualTo(new BigDecimal("22.20"));
    assertThat(result.costsSummary().disbursements()).isEqualTo(new BigDecimal("42.20"));
    // Contains NetCounselCostsAmount, TravelWaitingCostsAmount, NetWaitingCostsAmount
    assertThat(result.costsSummary().additionalPayments()).isEqualTo(new BigDecimal("306.60"));
    assertThat(result.costsSummary().submissionValue()).isEqualTo(new BigDecimal("142.20"));
  }
}
