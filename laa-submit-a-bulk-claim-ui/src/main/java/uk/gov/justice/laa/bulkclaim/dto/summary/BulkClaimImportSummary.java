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

  public boolean containsErrors() {
    return totalErrorCount > 0;
  }

  public int totalErrors() {
    return totalErrorCount;
  }

  public int totalClaimsWithErrors() {
    return totalClaimsWithErrors;
  }
}
