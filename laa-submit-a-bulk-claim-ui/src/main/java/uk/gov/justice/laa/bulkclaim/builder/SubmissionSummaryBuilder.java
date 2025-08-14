package uk.gov.justice.laa.bulkclaim.builder;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.dto.summary.BulkClaimSummary;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryClaimError;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryRow;
import uk.gov.justice.laa.bulkclaim.mapper.BulkClaimSummaryMapper;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.claims.model.ClaimValidationError;
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
   * Builds a {@link BulkClaimSummary} using a {@link GetSubmission200Response}.
   *
   * @param submissionResponse The source submission response..
   * @return The built {@link BulkClaimSummary}.
   */
  public BulkClaimSummary build(GetSubmission200Response submissionResponse) {
    // Map submission response to summary row
    SubmissionSummaryRow summaryRow =
        bulkClaimSummaryMapper.toSubmissionSummaryRow(submissionResponse);

    // Get the submission reference
    UUID submissionReference = submissionResponse.getSubmission().getSubmissionId();
    // Get only failed claims and map to a claim error object using data claims API to get further
    //  information regarding the claim.
    List<ClaimValidationError> errors =
        dataClaimsRestService.getValidationErrors(submissionReference).block();

    List<SubmissionSummaryClaimError> submissionSummaryClaimErrors =
        errors.stream()
            .map(x -> bulkClaimSummaryMapper.toSubmissionSummaryClaimError(submissionReference, x))
            .toList();
    return new BulkClaimSummary(
        Collections.singletonList(summaryRow), submissionSummaryClaimErrors);
  }
}
