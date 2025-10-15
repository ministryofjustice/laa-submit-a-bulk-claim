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
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.ClaimMessagesSummary;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionSummaryClaimMessageRow;
import uk.gov.justice.laa.bulkclaim.mapper.BulkClaimImportSummaryMapper;
import uk.gov.justice.laa.bulkclaim.util.PaginationUtil;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessageBase;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessageType;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessagesResponse;

@ExtendWith(MockitoExtension.class)
class SubmissionClaimMessagesBuilderTest {

  @Mock private DataClaimsRestClient dataClaimsRestClient;
  @Mock private BulkClaimImportSummaryMapper bulkClaimImportSummaryMapper;
  @Mock private PaginationUtil paginationUtil;

  @InjectMocks private SubmissionClaimMessagesBuilder builder;

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

    SubmissionSummaryClaimMessageRow mappedError =
        new SubmissionSummaryClaimMessageRow(
            submissionId,
            "UFN123",
            "UCN456",
            "John Doe",
            "John",
            "Doe",
            null,
            null,
            null,
            null,
            "Invalid data",
            "ERROR");

    when(bulkClaimImportSummaryMapper.toSubmissionSummaryClaimMessage(any(), any()))
        .thenReturn(mappedError);

    ClaimMessagesSummary result = builder.buildErrors(submissionId, 0, 10);

    assertThat(result.claimMessages()).containsExactly(mappedError);
    assertThat(result.totalMessageCount()).isEqualTo(1);
    assertThat(result.totalClaimsWithErrors()).isEqualTo(1);
  }

  @Test
  @DisplayName("should build empty claim error summary when validation error response is null")
  void shouldBuildSummaryWithEmptyErrorsWhenValidationErrorResponseIsNull() {
    UUID submissionId = UUID.randomUUID();

    when(dataClaimsRestClient.getValidationMessages(
            submissionId, null, ValidationMessageType.ERROR.toString(), null, 0))
        .thenReturn(Mono.empty());

    ClaimMessagesSummary result = builder.buildErrors(submissionId, 0, 10);

    assertThat(result.claimMessages()).isEmpty();
    assertThat(result.totalMessageCount()).isZero();
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

    SubmissionSummaryClaimMessageRow mappedError =
        new SubmissionSummaryClaimMessageRow(
            submissionId,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "Missing claimId",
            "ERROR");

    when(bulkClaimImportSummaryMapper.toSubmissionSummaryClaimMessage(any(), any()))
        .thenReturn(mappedError);

    ClaimMessagesSummary result = builder.buildErrors(submissionId, 0, 10);

    assertThat(result.claimMessages()).containsExactly(mappedError);
    assertThat(result.totalMessageCount()).isEqualTo(1);
    assertThat(result.totalClaimsWithErrors()).isEqualTo(1);
  }
}
