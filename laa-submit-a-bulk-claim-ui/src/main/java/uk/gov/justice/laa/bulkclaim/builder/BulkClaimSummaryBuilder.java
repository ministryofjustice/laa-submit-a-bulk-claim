package uk.gov.justice.laa.bulkclaim.builder;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummaryRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.ClaimMessagesSummary;
import uk.gov.justice.laa.bulkclaim.dto.summary.BulkClaimImportSummary;
import uk.gov.justice.laa.bulkclaim.mapper.BulkClaimImportSummaryMapper;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

/**
 * Builder class for constructing a {@link BulkClaimImportSummary} object used for displaying
 * submission summary information to the user.
 *
 * @author Jamie Briggs
 */
@Component
@Deprecated(since = "Will be removed when BulkSubmissionImportedController is removed", forRemoval = true)
@RequiredArgsConstructor
public class BulkClaimSummaryBuilder {

  private final BulkClaimImportSummaryMapper bulkClaimImportSummaryMapper;
  private final SubmissionClaimMessagesBuilder submissionClaimMessagesBuilder;

  /**
   * Builds a {@link BulkClaimImportSummary} using a {@link SubmissionResponse}.
   *
   * @param submissionResponse The source submission response..
   * @return The built {@link BulkClaimImportSummary}.
   */
  public BulkClaimImportSummary build(List<SubmissionResponse> submissionResponse, int page) {

    // Get all summary rows
    List<SubmissionSummaryRow> summaryRows =
        bulkClaimImportSummaryMapper.toSubmissionSummaryRows(submissionResponse);

    ClaimMessagesSummary claimErrorSummary =
        submissionClaimMessagesBuilder.buildErrors(
            submissionResponse.getFirst().getSubmissionId(), page);

    return new BulkClaimImportSummary(summaryRows, claimErrorSummary);
  }
}
