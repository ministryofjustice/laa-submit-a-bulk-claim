package uk.gov.justice.laa.payments.submit.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.payments.submit.controller.ControllerTestHelper.OIDC_USER;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessageBase;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessageType;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessagesResponse;
import uk.gov.justice.laa.payments.submit.client.DataClaimsRestClient;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessageRow;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessagesSource;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.payments.submit.mapper.BulkClaimImportSummaryMapper;
import uk.gov.justice.laa.payments.submit.service.ClaimService;
import uk.gov.justice.laa.payments.submit.service.SubmissionMessagesService;
import uk.gov.justice.laa.payments.submit.util.PaginationUtil;

@ExtendWith(MockitoExtension.class)
class SubmissionMessagesServiceTest {
  @Mock private ClaimService claimService;
  @Mock private DataClaimsRestClient dataClaimsRestClient;
  @Mock private BulkClaimImportSummaryMapper bulkClaimImportSummaryMapper;
  @Mock private PaginationUtil paginationUtil;

  @InjectMocks private SubmissionMessagesService messagesService;

  // TODO: Test for warnings on messages tab

  @Test
  @DisplayName("should build claim error summary with errors when claimId present")
  void shouldGetMessagesSummaryWithErrorsWhenClaimIdPresent() {
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
            submissionId,
            null,
            ValidationMessageType.ERROR.toString(),
            null,
            0,
            10,
            "client_surname,asc"))
        .thenReturn(Mono.just(errorResponse));

    MessageRow mappedError =
        new MessageRow(
            submissionId,
            Optional.empty(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "Invalid data",
            "ERROR");

    when(bulkClaimImportSummaryMapper.toSubmissionSummaryClaimMessage(any(), any()))
        .thenReturn(mappedError);

    MessagesSummary result =
        messagesService.getErrorMessages(OIDC_USER, submissionId, 0, 10, "client_surname,asc");

    assertThat(result.messages()).containsExactly(mappedError);
    assertThat(result.totalMessageCount()).isEqualTo(1);
    assertThat(result.totalClaimsWithErrors()).isEqualTo(1);
    assertThat(result.messagesSource()).isEqualTo(MessagesSource.CLAIM);
    verifyNoInteractions(claimService);
  }

  @Test
  @DisplayName("should build empty claim error summary when validation error response is null")
  void shouldGetMessagesWithClaimSummarySummaryWithEmptyErrorsWhenValidationErrorResponseIsNull() {
    UUID submissionId = UUID.randomUUID();

    when(dataClaimsRestClient.getValidationMessages(
            submissionId,
            null,
            ValidationMessageType.ERROR.toString(),
            null,
            0,
            10,
            "client_surname,asc"))
        .thenReturn(Mono.empty());

    MessagesSummary result =
        messagesService.getErrorMessages(OIDC_USER, submissionId, 0, 10, "client_surname,asc");

    assertThat(result.messages()).isEmpty();
    assertThat(result.totalMessageCount()).isZero();
    assertThat(result.totalClaimsWithErrors()).isZero();
    assertThat(result.messagesSource()).isEqualTo(MessagesSource.CLAIM);
    verifyNoInteractions(claimService);
  }

  @Test
  @DisplayName("should build claim error summary with default claim response when claimId is null")
  void shouldGetMessagesWithClaimSummarySummaryWithDefaultClaimResponseWhenClaimIdIsNull() {
    UUID submissionId = UUID.randomUUID();

    ValidationMessageBase error =
        new ValidationMessageBase()
            .submissionId(submissionId)
            .claimId(null)
            .displayMessage("Missing claimId");

    ValidationMessagesResponse errorResponse =
        new ValidationMessagesResponse().content(List.of(error)).totalElements(1).totalClaims(1);

    when(dataClaimsRestClient.getValidationMessages(
            submissionId,
            null,
            ValidationMessageType.ERROR.toString(),
            null,
            0,
            10,
            "client_surname,asc"))
        .thenReturn(Mono.just(errorResponse));

    MessageRow mappedError =
        new MessageRow(
            submissionId,
            Optional.empty(),
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

    MessagesSummary result =
        messagesService.getErrorMessages(OIDC_USER, submissionId, 0, 10, "client_surname,asc");

    assertThat(result.messages()).containsExactly(mappedError);
    assertThat(result.totalMessageCount()).isEqualTo(1);
    assertThat(result.totalClaimsWithErrors()).isEqualTo(1);
    assertThat(result.messagesSource()).isEqualTo(MessagesSource.SUBMISSION);
    verifyNoInteractions(claimService);
  }

  @Test
  @DisplayName("should build claim error summary with default claim response when claimId is set")
  void shouldGetMessagesWithClaimSummarySummaryWithDefaultClaimResponseWhenClaimIdIsSet() {
    UUID submissionId = UUID.randomUUID();
    UUID claimId = UUID.randomUUID();

    ValidationMessageBase error =
        new ValidationMessageBase()
            .submissionId(submissionId)
            .claimId(null)
            .displayMessage("Missing claimId");

    ValidationMessagesResponse errorResponse =
        new ValidationMessagesResponse().content(List.of(error)).totalElements(1).totalClaims(1);

    when(dataClaimsRestClient.getValidationMessages(
            submissionId,
            null,
            ValidationMessageType.ERROR.toString(),
            null,
            0,
            10,
            "client_surname,asc"))
        .thenReturn(Mono.just(errorResponse));

    MessageRow mappedError =
        new MessageRow(
            submissionId,
            Optional.of(claimId),
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

    MessagesSummary result =
        messagesService.getErrorMessages(OIDC_USER, submissionId, 0, 10, "client_surname,asc");

    assertThat(result.messages()).containsExactly(mappedError);
    assertThat(result.totalMessageCount()).isEqualTo(1);
    assertThat(result.totalClaimsWithErrors()).isEqualTo(1);
    assertThat(result.messagesSource()).isEqualTo(MessagesSource.SUBMISSION);
    verifyNoInteractions(claimService);
  }

  @Test
  @DisplayName("should fetch distinct non null claims once when building warning messages")
  void shouldFetchDistinctNonNullClaimsOnceWhenBuildingWarningMessages() {
    UUID submissionId = UUID.randomUUID();
    UUID firstClaimId = UUID.randomUUID();
    UUID secondClaimId = UUID.randomUUID();
    Page pagination = Page.builder().number(0).size(10).totalPages(1).totalElements(4).build();

    ValidationMessageBase firstMessage =
        new ValidationMessageBase()
            .submissionId(submissionId)
            .claimId(firstClaimId)
            .displayMessage("First warning");
    ValidationMessageBase duplicateClaimMessage =
        new ValidationMessageBase()
            .submissionId(submissionId)
            .claimId(firstClaimId)
            .displayMessage("Duplicate warning");
    ValidationMessageBase secondMessage =
        new ValidationMessageBase()
            .submissionId(submissionId)
            .claimId(secondClaimId)
            .displayMessage("Second warning");
    ValidationMessageBase submissionMessage =
        new ValidationMessageBase()
            .submissionId(submissionId)
            .claimId(null)
            .displayMessage("Submission warning");

    ValidationMessagesResponse response =
        new ValidationMessagesResponse()
            .content(List.of(firstMessage, duplicateClaimMessage, secondMessage, submissionMessage))
            .totalElements(4)
            .totalClaims(2);

    when(dataClaimsRestClient.getValidationMessages(
            submissionId,
            null,
            ValidationMessageType.WARNING.toString(),
            null,
            0,
            10,
            "client_surname,asc"))
        .thenReturn(Mono.just(response));
    when(claimService.getClaimV2(submissionId, firstClaimId, OIDC_USER))
        .thenReturn(new ClaimResponseV2());
    when(claimService.getClaimV2(submissionId, secondClaimId, OIDC_USER))
        .thenReturn(new ClaimResponseV2());
    when(bulkClaimImportSummaryMapper.toSubmissionSummaryClaimMessage(any(), any()))
        .thenReturn(
            MessageRow.builder().message("row-1").build(),
            MessageRow.builder().message("row-2").build(),
            MessageRow.builder().message("row-3").build(),
            MessageRow.builder().message("row-4").build());
    when(paginationUtil.fromValidationMessages(response, 0, 10)).thenReturn(pagination);

    MessagesSummary result =
        messagesService.getMessagesWithClaimSummary(
            OIDC_USER,
            submissionId,
            null,
            ValidationMessageType.WARNING,
            0,
            10,
            "client_surname,asc");

    assertThat(result.messages()).hasSize(4);
    assertThat(result.totalMessageCount()).isEqualTo(4);
    assertThat(result.totalClaimsWithErrors()).isEqualTo(2);
    assertThat(result.pagination()).isEqualTo(pagination);
    assertThat(result.messagesSource()).isEqualTo(MessagesSource.CLAIM);
    verify(claimService, times(1)).getClaimV2(submissionId, firstClaimId, OIDC_USER);
    verify(claimService, times(1)).getClaimV2(submissionId, secondClaimId, OIDC_USER);
    verifyNoMoreInteractions(claimService);
  }

  @Test
  @DisplayName("should return message counts without fetching claim summaries")
  void shouldReturnMessageCountsWithoutFetchingClaimSummaries() {
    UUID submissionId = UUID.randomUUID();
    ValidationMessagesResponse response =
        new ValidationMessagesResponse().content(List.of()).totalElements(3).totalClaims(2);

    when(dataClaimsRestClient.getValidationMessages(
            submissionId, null, ValidationMessageType.WARNING.toString(), null, 0, 1, null))
        .thenReturn(Mono.just(response));

    MessagesSummary result =
        messagesService.getMessageCounts(submissionId, null, ValidationMessageType.WARNING);

    assertThat(result.messages()).isEmpty();
    assertThat(result.totalMessageCount()).isEqualTo(3);
    assertThat(result.totalClaimsWithErrors()).isEqualTo(2);
    assertThat(result.pagination()).isNull();
    assertThat(result.messagesSource()).isEqualTo(MessagesSource.CLAIM);
    verifyNoInteractions(claimService);
  }
}
