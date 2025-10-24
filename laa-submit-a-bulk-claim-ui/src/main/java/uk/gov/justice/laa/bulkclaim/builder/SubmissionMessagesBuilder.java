package uk.gov.justice.laa.bulkclaim.builder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessageRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSource;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.bulkclaim.mapper.BulkClaimImportSummaryMapper;
import uk.gov.justice.laa.bulkclaim.util.PaginationUtil;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
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

  private final DataClaimsRestClient dataClaimsRestClient;
  private final BulkClaimImportSummaryMapper bulkClaimImportSummaryMapper;
  private final PaginationUtil paginationUtil;

  /**
   * Builds a {@link MessagesSummary} for a given submission ID whilst only returning errors.
   *
   * @param submissionId The submission ID to fetch errors for.
   * @param page         The page number to fetch errors for.
   * @return The built {@link MessagesSummary}.
   */
  public MessagesSummary buildErrors(UUID submissionId, int page, int size) {
    return build(submissionId, null, ValidationMessageType.ERROR, page, size);
  }

  /**
   * Builds a {@link MessagesSummary} for a given submission ID with both warnings and errors.
   *
   * @param submissionId The submission ID to fetch errors for.
   * @param claimId      The claim ID to fetch errors for.
   * @return The built {@link MessagesSummary}.
   */
  public MessagesSummary buildAllWarnings(UUID submissionId, UUID claimId) {
    return build(submissionId, claimId, ValidationMessageType.WARNING, null, null);
  }

  /**
   * Builds a {@link MessagesSummary} for a given submission ID and claim ID.
   *
   * @param submissionId the submission ID to fetch messages for.
   * @param claimId      the claim ID to fetch messages for.
   * @param page         the page number to fetch messages for.
   * @return the built {@link MessagesSummary}.
   */
  public MessagesSummary build(
      UUID submissionId, UUID claimId, ValidationMessageType type, Integer page, Integer size) {
    String submissionType = type != null ? type.toString() : null;
    final ValidationMessagesResponse messagesResponse =
        dataClaimsRestClient
            .getValidationMessages(submissionId, claimId, submissionType, null, page, size)
            .block();

    // Get all claims from data claims service
    List<UUID> claimRefs =
        Optional.ofNullable(messagesResponse)
            .map(ValidationMessagesResponse::getContent)
            .orElse(Collections.emptyList())
            .stream()
            .map(ValidationMessageBase::getClaimId)
            .toList();

    // Collate all possible claim responses which messagesResponse could have
    Map<UUID, Mono<ClaimResponse>> claims = claimRefs.stream()
        .filter(Objects::nonNull)
        .collect(Collectors.toMap(
            x -> x,
            x -> dataClaimsRestClient.getSubmissionClaim(submissionId, x)));

    // Loop through an error map and add claims
    final List<MessageRow> errorList =
        Optional.ofNullable(messagesResponse)
            .map(ValidationMessagesResponse::getContent)
            .orElseGet(List::of)
            .stream()
            .map(
                messages -> {
                  ClaimResponse claimResponse =
                      Optional.ofNullable(messages.getClaimId())
                          .map(
                              claimRef ->
                                  claims.get(claimRef)
                                      .onErrorResume(ex -> Mono.just(new ClaimResponse()))
                                      .switchIfEmpty(Mono.just(new ClaimResponse()))
                                      .block())
                          .orElseGet(ClaimResponse::new);
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
