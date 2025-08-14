package uk.gov.justice.laa.bulkclaim.builder;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.dto.summary.BulkClaimSummary;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryClaimError;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryRow;
import uk.gov.justice.laa.bulkclaim.mapper.BulkClaimSummaryMapper;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;

/**
 * Builder class for constructing a {@link BulkClaimSummary} object used for displaying submission
 * summary information to the user.
 *
 * @author Jamie Briggs
 */
@Component
@RequiredArgsConstructor
public class SubmissionSummaryBuilder {

  private final DataClaimsRestService dataClaimsRestService;
  private final BulkClaimSummaryMapper bulkClaimSummaryMapper;

  /**
   * Maps a {@link GetSubmission200Response} to a {@link BulkClaimSummary}.
   *
   * @param submissionResponse The response to map.
   * @return The mapped {@link BulkClaimSummary}.
   */
  public BulkClaimSummary mapSubmissionSummary(GetSubmission200Response submissionResponse) {
    // Map submission response to summary row
    SubmissionSummaryRow summaryRow =
        bulkClaimSummaryMapper.toSubmissionSummaryRow(submissionResponse);

    // Get only failed claims, and map to claim error object using data claims API to get further
    //  information regarding the claim.
    List<SubmissionSummaryClaimError> claimErrors =
        submissionResponse.getClaims().stream()
            .filter(x -> "VALIDATION_FAILED".equals(x.getStatus()))
            .map(
                x ->
                    dataClaimsRestService.getSubmissionClaim(
                        submissionResponse.getSubmission().getSubmissionId(), x.getClaimId()))
            .map(x -> bulkClaimSummaryMapper.toSubmissionSummaryClaimError(x.block()))
            .toList();
    return new BulkClaimSummary(Collections.singletonList(summaryRow), claimErrors);
  }
}
