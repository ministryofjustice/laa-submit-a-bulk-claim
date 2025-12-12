package uk.gov.justice.laa.bulkclaim.builder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimRowCostsDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimsDetails;
import uk.gov.justice.laa.bulkclaim.mapper.SubmissionClaimRowMapper;
import uk.gov.justice.laa.bulkclaim.util.PaginationUtil;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionClaim;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("Submission claim details builder tests")
class SubmissionClaimsDetailsBuilderTest {

  private SubmissionClaimDetailsBuilder builder;

  @Mock DataClaimsRestClient dataClaimsRestClient;
  @Mock SubmissionClaimRowMapper submissionClaimRowMapper;
  @Mock PaginationUtil paginationUtil;

  @BeforeEach
  void beforeEach() {
    builder =
        new SubmissionClaimDetailsBuilder(
            dataClaimsRestClient, submissionClaimRowMapper, paginationUtil);
  }

  @Test
  @DisplayName("Should map claim rows to submission claim details")
  void shouldMapClaimRowsToSubmissionClaimDetails() {
    // Given
    UUID submissionId = UUID.fromString("45bf06e5-2298-4163-adc0-a6134b48f213");
    UUID claimId = UUID.fromString("87fdac7e-6de4-4a98-a788-e89a9d1c0225");
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder()
            .submissionId(submissionId)
            .calculatedTotalAmount(new BigDecimal("70.50"))
            .claims(List.of(SubmissionClaim.builder().claimId(claimId).build()))
            .build();
    ClaimResultSet claimResultSet =
        ClaimResultSet.builder()
            .totalElements(1)
            .content(Collections.singletonList(ClaimResponse.builder().totalWarnings(1).build()))
            .size(10)
            .number(2)
            .totalPages(2)
            .build();
    when(dataClaimsRestClient.getClaims(any(), any(), any(), any()))
        .thenReturn(ResponseEntity.of(Optional.of(claimResultSet)));
    SubmissionClaimRow expected =
        new SubmissionClaimRow(
            UUID.fromString("5146e93f-92c8-4c56-bd25-0cb6953f534d"),
            1,
            "ufn",
            "ucn",
            "client",
            "client-forename",
            "client-surname",
            "client2-forename",
            "client2-surname",
            "client2-ucn",
            "cat",
            "matter",
            LocalDate.of(2025, 5, 1),
            1,
            "feeType",
            "feeCode",
            new SubmissionClaimRowCostsDetails(
                new BigDecimal("10.10"),
                new BigDecimal("20.10"),
                new BigDecimal("30.10"),
                new BigDecimal("40.10"),
                new BigDecimal("50.10"),
                new BigDecimal("60.10"),
                new BigDecimal("70.10")),
            Boolean.TRUE);

    when(submissionClaimRowMapper.toSubmissionClaimRow(any(), anyInt())).thenReturn(expected);
    // When
    SubmissionClaimsDetails result = builder.build(submissionResponse, 0, 10);
    // Then
    assertThat(result.submissionClaims().contains(expected)).isTrue();
    assertThat(result.totalClaimValue()).isEqualTo(new BigDecimal("70.50"));
  }
}
