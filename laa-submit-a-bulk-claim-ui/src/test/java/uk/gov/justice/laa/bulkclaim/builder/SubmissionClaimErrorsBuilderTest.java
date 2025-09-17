package uk.gov.justice.laa.bulkclaim.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
import uk.gov.justice.laa.bulkclaim.dto.summary.ClaimErrorSummary;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryClaimErrorRow;
import uk.gov.justice.laa.bulkclaim.mapper.BulkClaimImportSummaryMapper;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessageBase;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessageType;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessagesResponse;

@ExtendWith(MockitoExtension.class)
class SubmissionClaimErrorsBuilderTest {

  @Mock private DataClaimsRestClient dataClaimsRestClient;
  @Mock private BulkClaimImportSummaryMapper bulkClaimImportSummaryMapper;

  @InjectMocks private SubmissionClaimErrorsBuilder builder;

  @Test
  @DisplayName("should build claim error summary with errors when claimId present")
  void shouldBuildSummaryWithErrorsWhenClaimIdPresent() {
    UUID submissionId = UUID.randomUUID();
    UUID claimId = UUID.randomUUID();

    ValidationMessageBase error =
        new ValidationMessageBase()
            .submissionId(submissionId)
            .claimId(claimId)
            .displayMessage("Invalid data");

    ValidationMessagesResponse errorResponse =
        new ValidationMessagesResponse().content(List.of(error)).totalElements(1).totalClaims(1);

    when(dataClaimsRestClient.getValidationMessages(
            submissionId, null, ValidationMessageType.ERROR.toString(), null, 0))
        .thenReturn(Mono.just(errorResponse));

    when(dataClaimsRestClient.getSubmissionClaim(submissionId, claimId))
        .thenReturn(Mono.just(new ClaimResponse()));

    SubmissionSummaryClaimErrorRow mappedError =
        new SubmissionSummaryClaimErrorRow(
            submissionId, "UFN123", "UCN456", "John Doe", "Invalid data");

    when(bulkClaimImportSummaryMapper.toSubmissionSummaryClaimMessage(any(), any()))
        .thenReturn(mappedError);

    ClaimErrorSummary result = builder.build(submissionId, 0);

    assertThat(result.claimErrors()).containsExactly(mappedError);
    assertThat(result.totalErrorCount()).isEqualTo(1);
    assertThat(result.totalClaimsWithErrors()).isEqualTo(1);
  }

  @Test
  @DisplayName("should build empty claim error summary when validation error response is null")
  void shouldBuildSummaryWithEmptyErrorsWhenValidationErrorResponseIsNull() {
    UUID submissionId = UUID.randomUUID();

    when(dataClaimsRestClient.getValidationMessages(
            submissionId, null, ValidationMessageType.ERROR.toString(), null, 0))
        .thenReturn(Mono.empty());

    ClaimErrorSummary result = builder.build(submissionId, 0);

    assertThat(result.claimErrors()).isEmpty();
    assertThat(result.totalErrorCount()).isZero();
    assertThat(result.totalClaimsWithErrors()).isZero();
  }

  @Test
  @DisplayName("should build claim error summary with default claim response when claimId is null")
  void shouldBuildSummaryWithDefaultClaimResponseWhenClaimIdIsNull() {
    UUID submissionId = UUID.randomUUID();

    ValidationMessageBase error =
        new ValidationMessageBase()
            .submissionId(submissionId)
            .claimId(null)
            .displayMessage("Missing claimId");

    ValidationMessagesResponse errorResponse =
        new ValidationMessagesResponse().content(List.of(error)).totalElements(1).totalClaims(1);

    when(dataClaimsRestClient.getValidationMessages(
            submissionId, null, ValidationMessageType.ERROR.toString(), null, 0))
        .thenReturn(Mono.just(errorResponse));

    SubmissionSummaryClaimErrorRow mappedError =
        new SubmissionSummaryClaimErrorRow(submissionId, null, null, null, "Missing claimId");

    when(bulkClaimImportSummaryMapper.toSubmissionSummaryClaimMessage(any(), any()))
        .thenReturn(mappedError);

    ClaimErrorSummary result = builder.build(submissionId, 0);

    assertThat(result.claimErrors()).containsExactly(mappedError);
    assertThat(result.totalErrorCount()).isEqualTo(1);
    assertThat(result.totalClaimsWithErrors()).isEqualTo(1);
  }
}
