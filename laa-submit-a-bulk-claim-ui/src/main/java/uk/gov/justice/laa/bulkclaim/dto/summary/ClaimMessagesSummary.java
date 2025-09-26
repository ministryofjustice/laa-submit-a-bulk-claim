package uk.gov.justice.laa.bulkclaim.dto.summary;

import java.util.List;

/**
 * Summary of claim errors.
 *
 * @param claimMessages list of claim messages rows
 * @param totalMessageCount total number of errors found
 * @param totalClaimsWithErrors total number of unique claims with errors
 */
public record ClaimMessagesSummary(
    List<SubmissionSummaryClaimMessageRow> claimMessages,
    int totalMessageCount,
    int totalClaimsWithErrors) {

  /**
   * Returns true if there are any errors in the bulk claim.
   *
   * @return true if total error count is greater than zero
   */
  public boolean containsErrors() {
    return totalErrors() > 0;
  }

  /**
   * Returns the total number of errors found in the bulk claim.
   *
   * @return the total error count
   */
  public long totalErrors() {
    return claimMessages.stream().filter(x -> "error".equalsIgnoreCase(x.type())).count();
  }

  /**
   * Returns the number of claims that have one or more errors.
   *
   * @return the total number of unique claims with errors
   */
  public int totalClaimsWithErrors() {
    return totalClaimsWithErrors;
  }
}
