package uk.gov.justice.laa.bulkclaim.builder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.summary.ClaimErrorSummary;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryClaimErrorRow;
import uk.gov.justice.laa.bulkclaim.mapper.BulkClaimImportSummaryMapper;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessageType;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessagesResponse;

/**
 * Builder class for constructing a {@link
 * uk.gov.justice.laa.bulkclaim.dto.summary.ClaimErrorSummary} object used for displaying claim
 * error details to the user.
 */
@Component
@RequiredArgsConstructor
public class SubmissionClaimErrorsBuilder {

  private final DataClaimsRestClient dataClaimsRestClient;
  private final BulkClaimImportSummaryMapper bulkClaimImportSummaryMapper;

  /**
   * Builds a {@link uk.gov.justice.laa.bulkclaim.dto.summary.ClaimErrorSummary} for a given
   * submission ID.
   *
   * @param submissionId The submission ID to fetch errors for.
   * @return The built {@link uk.gov.justice.laa.bulkclaim.dto.summary.ClaimErrorSummary}.
   */
  public ClaimErrorSummary build(UUID submissionId, int page) {
    ValidationMessagesResponse errorResponse =
        dataClaimsRestClient
            .getValidationMessages(
                submissionId, null, ValidationMessageType.ERROR.toString(), null, page)
            .block();

    // Loop through an error map and add claims
    List<SubmissionSummaryClaimErrorRow> errorList =
        Optional.ofNullable(errorResponse)
            .map(ValidationMessagesResponse::getContent)
            .orElseGet(List::of)
            .stream()
            .map(
                messages -> {
                  ClaimResponse claimResponse;
                  if (messages.getClaimId() == null) {
                    // no call if claimId is missing
                    claimResponse = new ClaimResponse();
                  } else {
                    claimResponse =
                        dataClaimsRestClient
                            .getSubmissionClaim(messages.getSubmissionId(), messages.getClaimId())
                            .onErrorResume(ex -> Mono.just(new ClaimResponse()))
                            .switchIfEmpty(Mono.just(new ClaimResponse()))
                            .block();
                  }
                  return bulkClaimImportSummaryMapper.toSubmissionSummaryClaimMessage(
                      messages, claimResponse);
                })
            .toList();

    int totalErrorCount =
        Optional.ofNullable(errorResponse)
            .map(ValidationMessagesResponse::getTotalElements)
            .orElse(0);

    int totalClaimsWithErrors =
        Optional.ofNullable(errorResponse)
            .map(ValidationMessagesResponse::getTotalClaims)
            .orElse(0);

    return new ClaimErrorSummary(errorList, totalErrorCount, totalClaimsWithErrors);
  }
}
