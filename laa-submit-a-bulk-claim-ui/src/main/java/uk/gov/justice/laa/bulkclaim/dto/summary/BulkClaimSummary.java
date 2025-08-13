package uk.gov.justice.laa.bulkclaim.dto.summary;

import java.util.List;

/**
 * Summary of submissions and claim errors. Used in the submission summary page between two tables.
 *
 * @param submissions the submissions part of the bulk claim
 * @param claimErrors the claim errors part of the bulk claim
 */
public record BulkClaimSummary(
    List<SubmissionSummaryRow> submissions, List<SubmissionSummaryClaimError> claimErrors) {

  public boolean containsErrors() {
    return claimErrors != null && !claimErrors.isEmpty();
  }

  public int totalErrors() {
    return claimErrors != null ? claimErrors.size() : 0;
  }
}
