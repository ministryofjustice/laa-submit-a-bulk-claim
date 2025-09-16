package uk.gov.justice.laa.bulkclaim.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.summary.BulkClaimImportSummary;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryClaimErrorRow;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryRow;
import uk.gov.justice.laa.bulkclaim.mapper.BulkClaimImportSummaryMapper;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationErrorFields;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationErrorsResponse;

@ExtendWith(MockitoExtension.class)
class BulkClaimSummaryBuilderTest {

  @Mock private DataClaimsRestClient dataClaimsRestClient;

  @Mock private BulkClaimImportSummaryMapper bulkClaimImportSummaryMapper;

  @InjectMocks private BulkClaimSummaryBuilder builder;

  @Test
  @DisplayName("should build summary with errors when claimId present")
  void shouldBuildSummaryWithErrorsWhenClaimIdPresent() {
    UUID submissionId = UUID.randomUUID();
    UUID claimId = UUID.randomUUID();

    SubmissionResponse submissionResponse =
        SubmissionResponse.builder().submissionId(submissionId).build();

    SubmissionSummaryRow summaryRow =
        new SubmissionSummaryRow(OffsetDateTime.now(), submissionId, "12345", "Civil", null, 5);
    when(bulkClaimImportSummaryMapper.toSubmissionSummaryRows(List.of(submissionResponse)))
        .thenReturn(List.of(summaryRow));

    ValidationErrorFields error =
        new ValidationErrorFields()
            .submissionId(submissionId)
            .claimId(claimId)
            .errorDescription("Invalid data");

    ValidationErrorsResponse errorResponse =
        new ValidationErrorsResponse().content(List.of(error)).totalElements(1).totalClaims(1);

    when(dataClaimsRestClient.getValidationErrors(submissionId))
        .thenReturn(Mono.just(errorResponse));
    when(dataClaimsRestClient.getSubmissionClaim(submissionId, claimId))
        .thenReturn(Mono.just(new ClaimResponse()));

    SubmissionSummaryClaimErrorRow mappedError =
        new SubmissionSummaryClaimErrorRow(
            submissionId, "UFN123", "UCN456", "John Doe", "Invalid data");

    when(bulkClaimImportSummaryMapper.toSubmissionSummaryClaimError(any(), any()))
        .thenReturn(mappedError);

    BulkClaimImportSummary result = builder.build(List.of(submissionResponse));

    assertThat(result.submissions()).hasSize(1);
    assertThat(result.claimErrors()).containsExactly(mappedError);
    assertThat(result.totalErrorCount()).isEqualTo(1);
    assertThat(result.totalClaimsWithErrors()).isEqualTo(1);
  }

  @Test
  @DisplayName("should build summary with empty errors when validation error response is null")
  void shouldBuildSummaryWithEmptyErrorsWhenValidationErrorResponseIsNull() {
    UUID submissionId = UUID.randomUUID();
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder().submissionId(submissionId).build();

    SubmissionSummaryRow summaryRow =
        new SubmissionSummaryRow(OffsetDateTime.now(), submissionId, "12345", "Civil", null, 2);
    when(bulkClaimImportSummaryMapper.toSubmissionSummaryRows(List.of(submissionResponse)))
        .thenReturn(List.of(summaryRow));

    when(dataClaimsRestClient.getValidationErrors(submissionId)).thenReturn(Mono.empty());

    BulkClaimImportSummary result = builder.build(List.of(submissionResponse));

    assertThat(result.submissions()).hasSize(1);
    assertThat(result.claimErrors()).isEmpty();
    assertThat(result.totalErrorCount()).isZero();
    assertThat(result.totalClaimsWithErrors()).isZero();
  }

  @Test
  @DisplayName("should build summary with default claim response when claimId is null")
  void shouldBuildSummaryWithDefaultClaimResponseWhenClaimIdIsNull() {
    UUID submissionId = UUID.randomUUID();
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder().submissionId(submissionId).build();

    SubmissionSummaryRow summaryRow =
        new SubmissionSummaryRow(OffsetDateTime.now(), submissionId, "12345", "Civil", null, 3);
    when(bulkClaimImportSummaryMapper.toSubmissionSummaryRows(List.of(submissionResponse)))
        .thenReturn(List.of(summaryRow));

    ValidationErrorFields error =
        new ValidationErrorFields()
            .submissionId(submissionId)
            .claimId(null)
            .errorDescription("Missing claimId");

    ValidationErrorsResponse errorResponse =
        new ValidationErrorsResponse().content(List.of(error)).totalElements(1).totalClaims(1);

    when(dataClaimsRestClient.getValidationErrors(submissionId))
        .thenReturn(Mono.just(errorResponse));

    SubmissionSummaryClaimErrorRow mappedError =
        new SubmissionSummaryClaimErrorRow(submissionId, null, null, null, "Missing claimId");

    when(bulkClaimImportSummaryMapper.toSubmissionSummaryClaimError(any(), any()))
        .thenReturn(mappedError);

    BulkClaimImportSummary result = builder.build(List.of(submissionResponse));

    assertThat(result.submissions()).hasSize(1);
    assertThat(result.claimErrors()).containsExactly(mappedError);
    assertThat(result.totalErrorCount()).isEqualTo(1);
    assertThat(result.totalClaimsWithErrors()).isEqualTo(1);
  }
}
