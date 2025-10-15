package uk.gov.justice.laa.bulkclaim.dto.submission.claim;

import java.util.List;
import java.util.Optional;
import lombok.Builder;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;

/**
 * Summary of claim errors.
 *
 * @param claimMessages list of claim messages rows
 * @param totalMessageCount calculatedTotal number of errors found
 * @param totalClaimsWithErrors calculatedTotal number of unique claims with errors
 */
@Builder
public record ClaimMessagesSummary(
    List<SubmissionSummaryClaimMessageRow> claimMessages,
    int totalMessageCount,
    int totalClaimsWithErrors,
    Page pagination) {

  /**
   * Returns true if there are any errors in the bulk claim.
   *
   * @return true if calculatedTotal error count is greater than zero
   */
  public boolean containsErrors() {
    return totalErrors() > 0;
  }

  /**
   * Returns the calculatedTotal number of messages found in the bulk claim.
   *
   * @return the calculatedTotal error count
   */
  public long totalErrors() {
    return Optional.ofNullable(claimMessages).stream()
        .flatMap(List::stream)
        .filter(x -> "error".equalsIgnoreCase(x.type()))
        .count();
  }

  /**
   * Returns the number of claims that have one or more errors.
   *
   * @return the calculatedTotal number of unique claims with errors
   */
  public int totalClaimsWithErrors() {
    return totalClaimsWithErrors;
  }
}
