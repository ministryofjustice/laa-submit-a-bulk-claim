package uk.gov.justice.laa.payments.submit.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessageBase;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessageType;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessagesResponse;
import uk.gov.justice.laa.payments.submit.client.DataClaimsRestClient;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessageRow;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessagesSource;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.payments.submit.mapper.BulkClaimImportSummaryMapper;
import uk.gov.justice.laa.payments.submit.util.PaginationUtil;

@Service
@RequiredArgsConstructor
public class SubmissionMessagesService {

  private final ClaimService claimService;
  private final DataClaimsRestClient dataClaimsRestClient;
  private final BulkClaimImportSummaryMapper bulkClaimImportSummaryMapper;
  private final PaginationUtil paginationUtil;

  // TODO: Remove oidcUser
  public MessagesSummary getErrorMessages(
      OidcUser oidcUser, UUID submissionId, int page, int size, String sort) {
    return getMessages(submissionId, null, ValidationMessageType.ERROR, page, size, sort);
  }

  // TODO: Remove oidcUser
  public MessagesSummary getAllWarningMessages(OidcUser oidcUser, UUID submissionId, UUID claimId) {
    return getMessages(submissionId, claimId, ValidationMessageType.WARNING, null, null, null);
  }

  public MessagesSummary getMessageCounts(
      UUID submissionId, UUID claimId, ValidationMessageType type) {
    var messagesResponse = getMessageResponse(submissionId, claimId, type, 0, 1, null);

    return new MessagesSummary(
        List.of(),
        getTotalMessageCount(messagesResponse),
        getTotalClaims(messagesResponse),
        null,
        getMessageSource(messagesResponse));
  }

  public MessagesSummary getMessages(
      UUID submissionId,
      UUID claimId,
      ValidationMessageType type,
      Integer page,
      Integer size,
      String sort) {
    var messagesResponse = getMessageResponse(submissionId, claimId, type, page, size, sort);

    var errorList =
        Optional.ofNullable(messagesResponse)
            .map(ValidationMessagesResponse::getContent)
            .orElseGet(List::of)
            .stream()
            .map(
                messages ->
                    bulkClaimImportSummaryMapper.toSubmissionSummaryClaimMessage(messages, null))
            .toList();

    return new MessagesSummary(
        errorList,
        getTotalMessageCount(messagesResponse),
        getTotalClaims(messagesResponse),
        paginationUtil.fromValidationMessages(messagesResponse, page, size),
        getMessageSource(messagesResponse));
  }

  public MessagesSummary getMessagesWithClaimSummary(
      OidcUser oidcUser,
      UUID submissionId,
      UUID claimId,
      ValidationMessageType type,
      Integer page,
      Integer size,
      String sort) {
    var messagesResponse = getMessageResponse(submissionId, claimId, type, page, size, sort);

    // Get all claims from data claims service (Only keep unique keys)
    Set<UUID> claimRefs =
        Optional.ofNullable(messagesResponse)
            .map(ValidationMessagesResponse::getContent)
            .orElse(Collections.emptyList())
            .stream()
            .map(ValidationMessageBase::getClaimId)
            .collect(Collectors.toSet());

    // Collate all possible claim responses which messagesResponse could have
    Map<UUID, ClaimResponseV2> claims =
        claimRefs.stream()
            .filter(Objects::nonNull)
            .collect(
                Collectors.toMap(x -> x, x -> claimService.getClaimV2(submissionId, x, oidcUser)));

    // Loop through an error map and add claims
    final List<MessageRow> errorList =
        Optional.ofNullable(messagesResponse)
            .map(ValidationMessagesResponse::getContent)
            .orElseGet(List::of)
            .stream()
            .map(
                messages -> {
                  ClaimResponseV2 claimResponse =
                      Optional.ofNullable(messages.getClaimId())
                          .map(claims::get)
                          .orElseGet(ClaimResponseV2::new);
                  return bulkClaimImportSummaryMapper.toSubmissionSummaryClaimMessage(
                      messages, claimResponse);
                })
            .toList();

    return new MessagesSummary(
        errorList,
        getTotalMessageCount(messagesResponse),
        getTotalClaims(messagesResponse),
        paginationUtil.fromValidationMessages(messagesResponse, page, size),
        getMessageSource(messagesResponse));
  }

  private ValidationMessagesResponse getMessageResponse(
      UUID submissionId,
      UUID claimId,
      ValidationMessageType type,
      Integer page,
      Integer size,
      String sort) {
    var submissionType = type != null ? type.toString() : null;
    return dataClaimsRestClient
        .getValidationMessages(submissionId, claimId, submissionType, null, page, size, sort)
        .block();
  }

  private static MessagesSource getMessageSource(ValidationMessagesResponse messagesResponse) {
    var messagesSource = MessagesSource.CLAIM;
    if (messagesResponse != null
        && messagesResponse.getContent() != null
        && !messagesResponse.getContent().isEmpty()
        && messagesResponse.getContent().getFirst().getClaimId() == null) {
      messagesSource = MessagesSource.SUBMISSION;
    }
    return messagesSource;
  }

  private static int getTotalMessageCount(ValidationMessagesResponse messagesResponse) {
    return Optional.ofNullable(messagesResponse)
        .map(ValidationMessagesResponse::getTotalElements)
        .orElse(0);
  }

  private static int getTotalClaims(ValidationMessagesResponse messagesResponse) {
    return Optional.ofNullable(messagesResponse)
        .map(ValidationMessagesResponse::getTotalClaims)
        .orElse(0);
  }
}
