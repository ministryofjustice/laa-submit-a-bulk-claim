package uk.gov.justice.laa.bulkclaim.dto.summary;

import java.util.List;

/**
 * Summary of submissions and claim errors. Used in the submission summary page between two tables.
 *
 * @param submissions the submissions part of the bulk claim
 * @param claimErrors the claim errors part of the bulk claim
 */
public record BulkClaimImportSummary(
    List<SubmissionSummaryRow> submissions,
    List<SubmissionSummaryClaimErrorRow> claimErrors,
    int totalErrorCount,
    int totalClaimsWithErrors) {

  /**
   * Returns true if there are any errors in the bulk claim.
   *
   * @return true if total error count is greater than zero
   */
  public boolean containsErrors() {
    return totalErrorCount > 0;
  }

  /**
   * Returns the total number of errors found in the bulk claim.
   *
   * @return the total error count
   */
  public int totalErrors() {
    return totalErrorCount;
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
