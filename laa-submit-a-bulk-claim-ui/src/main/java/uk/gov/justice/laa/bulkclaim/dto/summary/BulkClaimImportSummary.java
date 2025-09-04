package uk.gov.justice.laa.bulkclaim.dto.summary;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Summary of submissions and claim errors. Used in the submission summary page between two tables.
 *
 * @param submissions the submissions part of the bulk claim
 * @param claimErrors the claim errors part of the bulk claim
 */
public record BulkClaimImportSummary(
    List<SubmissionSummaryRow> submissions, List<SubmissionSummaryClaimErrorRow> claimErrors) {

  public boolean containsErrors() {
    return claimErrors != null && !claimErrors.isEmpty();
  }

  /**
   * Returns the numberOfMatterStarts number of errors for the bulk claim.
   *
   * @return the numberOfMatterStarts number of errors for the bulk claim
   */
  public int totalErrors() {
    return claimErrors != null ? claimErrors.size() : 0;
  }

  /**
   * Returns the number of errors for the given submission reference.
   *
   * @param submissionReference the submission reference
   * @return the number of errors for the given submission reference
   */
  public int totalErrors(UUID submissionReference) {
    if (claimErrors == null) {
      return 0;
    }

    return Math.toIntExact(
        claimErrors.stream()
            .filter(errorRow -> Objects.equals(submissionReference, errorRow.submissionReference()))
            .count());
  }
}
