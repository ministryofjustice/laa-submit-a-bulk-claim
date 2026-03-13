package uk.gov.justice.laa.bulkclaim.builder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClientV2;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimRowCostsDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimsDetails;
import uk.gov.justice.laa.bulkclaim.mapper.SubmissionClaimRowMapper;
import uk.gov.justice.laa.bulkclaim.util.PaginationUtil;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Submission claim details builder tests")
class SubmissionClaimsDetailsBuilderTest {

  private SubmissionClaimDetailsBuilder builder;

  @Mock DataClaimsRestClient dataClaimsRestClient;
  @Mock DataClaimsRestClientV2 dataClaimsRestClientV2;
  @Mock SubmissionClaimRowMapper submissionClaimRowMapper;
  @Mock PaginationUtil paginationUtil;

  @BeforeEach
  void beforeEach() {
    builder =
        new SubmissionClaimDetailsBuilder(
            dataClaimsRestClient, dataClaimsRestClientV2, submissionClaimRowMapper, paginationUtil);
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
    SubmissionClaimRow expected = getSubmissionClaimRow();

    when(submissionClaimRowMapper.toSubmissionClaimRow(any(), anyInt())).thenReturn(expected);
    // When
    SubmissionClaimsDetails result = builder.build(submissionResponse, 0, 10);
    // Then
    assertThat(result.submissionClaims().contains(expected)).isTrue();
    assertThat(result.totalClaimValue()).isEqualTo(new BigDecimal("70.50"));
  }

  @Test
  @DisplayName("Should map claim result set V2 to submission claim details")
  void shouldMapClaimResultSet2ToSubmissionClaimDetails() {

    var submissionResponse =
        SubmissionResponse.builder()
            .submissionId(UUID.fromString("45bf06e5-2298-4163-adc0-a6134b48f213"))
            .calculatedTotalAmount(new BigDecimal("70.50"))
            .officeAccountNumber("123456")
            .build();

    var claimResultSet =
        ClaimResultSetV2.builder()
            .totalElements(1)
            .content(Collections.singletonList(ClaimResponseV2.builder().totalWarnings(1).build()))
            .size(10)
            .number(2)
            .totalPages(2)
            .build();

    when(dataClaimsRestClientV2.getClaims(
            eq("123456"),
            eq(UUID.fromString("45bf06e5-2298-4163-adc0-a6134b48f213")),
            eq(0),
            eq(10),
            eq("sort")))
        .thenReturn(ResponseEntity.of(Optional.of(claimResultSet)));

    SubmissionClaimRow expected = getSubmissionClaimRow();

    when(submissionClaimRowMapper.toSubmissionClaimRow(eq(claimResultSet.getContent().get(0))))
        .thenReturn(expected);
    when(paginationUtil.from(eq(2), eq(10), eq(1)))
        .thenReturn(Page.builder().number(2).size(10).build());

    var actualResults = builder.build(submissionResponse, 0, 10, "sort");

    verify(dataClaimsRestClientV2)
        .getClaims(
            eq("123456"),
            eq(UUID.fromString("45bf06e5-2298-4163-adc0-a6134b48f213")),
            eq(0),
            eq(10),
            eq("sort"));

    verify(submissionClaimRowMapper).toSubmissionClaimRow(eq(claimResultSet.getContent().get(0)));

    verify(paginationUtil).from(eq(2), eq(10), eq(1));

    assertThat(actualResults.submissionClaims()).isEqualTo(List.of(expected));
    assertThat(actualResults.totalClaimValue()).isEqualTo(new BigDecimal("70.50"));
    assertThat(actualResults.pagination()).isEqualTo(Page.builder().number(2).size(10).build());
  }

  private static @NotNull SubmissionClaimRow getSubmissionClaimRow() {
    return new SubmissionClaimRow(
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
  }
}
