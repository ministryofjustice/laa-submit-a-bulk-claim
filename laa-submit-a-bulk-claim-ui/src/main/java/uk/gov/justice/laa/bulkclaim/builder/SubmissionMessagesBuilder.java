package uk.gov.justice.laa.bulkclaim.builder;

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
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessageRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSource;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.bulkclaim.mapper.BulkClaimImportSummaryMapper;
import uk.gov.justice.laa.bulkclaim.service.ClaimService;
import uk.gov.justice.laa.bulkclaim.util.PaginationUtil;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessageBase;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessageType;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessagesResponse;

/**
 * Builder class for constructing a {@link MessagesSummary} object used for displaying claim error
 * and warning details to the user.
 */
@Component
@RequiredArgsConstructor
public class SubmissionMessagesBuilder {

  private final ClaimService claimService;
  private final DataClaimsRestClient dataClaimsRestClient;
  private final BulkClaimImportSummaryMapper bulkClaimImportSummaryMapper;
  private final PaginationUtil paginationUtil;

  /** Builds a {@link MessagesSummary} for a given submission ID whilst only returning errors. */
  public MessagesSummary buildErrors(
      OidcUser oidcUser, UUID submissionId, int page, int size, String sort) {
    return build(oidcUser, submissionId, null, ValidationMessageType.ERROR, page, size, sort);
  }

  /** Builds a {@link MessagesSummary} for a given submission ID with both warnings and errors. */
  public MessagesSummary buildAllWarnings(OidcUser oidcUser, UUID submissionId, UUID claimId) {
    return build(oidcUser, submissionId, claimId, ValidationMessageType.WARNING, null, null, null);
  }

  public MessagesSummary build(
      OidcUser oidcUser,
      UUID submissionId,
      UUID claimId,
      ValidationMessageType type,
      Integer page,
      Integer size,
      String sort) {
    String submissionType = type != null ? type.toString() : null;
    final ValidationMessagesResponse messagesResponse =
        dataClaimsRestClient
            .getValidationMessages(submissionId, claimId, submissionType, null, page, size, sort)
            .block();

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

    final int totalMessageCount =
        Optional.ofNullable(messagesResponse)
            .map(ValidationMessagesResponse::getTotalElements)
            .orElse(0);

    final int totalClaims =
        Optional.ofNullable(messagesResponse)
            .map(ValidationMessagesResponse::getTotalClaims)
            .orElse(0);

    MessagesSource messagesSource = null;
    if (totalMessageCount > 0) {
      // Set message source to submission if first message has no claim ID (all claims are either
      // submission or claim).
      messagesSource =
          messagesResponse.getContent().getFirst().getClaimId() == null
              ? MessagesSource.SUBMISSION
              : MessagesSource.CLAIM;
    }

    return new MessagesSummary(
        errorList,
        totalMessageCount,
        totalClaims,
        paginationUtil.fromValidationMessages(messagesResponse, page, size),
        messagesSource);
  }
}
