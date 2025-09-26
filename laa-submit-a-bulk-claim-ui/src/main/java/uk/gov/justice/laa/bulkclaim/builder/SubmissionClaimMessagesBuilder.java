package uk.gov.justice.laa.bulkclaim.builder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.summary.ClaimMessagesSummary;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryClaimMessageRow;
import uk.gov.justice.laa.bulkclaim.mapper.BulkClaimImportSummaryMapper;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessageType;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessagesResponse;

/**
 * Builder class for constructing a {@link ClaimMessagesSummary} object used for displaying claim
 * error and warning details to the user.
 */
@Component
@RequiredArgsConstructor
public class SubmissionClaimMessagesBuilder {

  private final DataClaimsRestClient dataClaimsRestClient;
  private final BulkClaimImportSummaryMapper bulkClaimImportSummaryMapper;

  /**
   * Builds a {@link ClaimMessagesSummary} for a given submission ID whilst only returning errors.
   *
   * @param submissionId The submission ID to fetch errors for.
   * @param page The page number to fetch errors for.
   * @return The built {@link ClaimMessagesSummary}.
   */
  public ClaimMessagesSummary buildErrors(UUID submissionId, int page) {
    return build(submissionId, null, page, ValidationMessageType.ERROR);
  }

  /**
   * Builds a {@link ClaimMessagesSummary} for a given submission ID with both warnings and errors.
   *
   * @param submissionId The submission ID to fetch errors for.
   * @param claimId The claim ID to fetch errors for.
   * @param page The page number to fetch errors for.
   * @return The built {@link ClaimMessagesSummary}.
   */
  public ClaimMessagesSummary build(UUID submissionId, UUID claimId, int page) {
    return build(submissionId, claimId, page, null);
  }

  /**
   * Builds a {@link ClaimMessagesSummary} for a given submission ID and claim ID.
   *
   * @param submissionId the submission ID to fetch messages for.
   * @param claimId the claim ID to fetch messages for.
   * @param page the page number to fetch messages for.
   * @return the built {@link ClaimMessagesSummary}.
   */
  public ClaimMessagesSummary build(
      UUID submissionId, UUID claimId, int page, ValidationMessageType type) {
    String submissionType = type != null ? type.toString() : null;
    final ValidationMessagesResponse messagesResponse =
        dataClaimsRestClient
            .getValidationMessages(submissionId, claimId, submissionType, null, page)
            .block();

    // Loop through an error map and add claims
    final List<SubmissionSummaryClaimMessageRow> errorList =
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
                                  dataClaimsRestClient
                                      .getSubmissionClaim(messages.getSubmissionId(), claimRef)
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

    return new ClaimMessagesSummary(errorList, totalMessageCount, totalClaims);
  }
}
