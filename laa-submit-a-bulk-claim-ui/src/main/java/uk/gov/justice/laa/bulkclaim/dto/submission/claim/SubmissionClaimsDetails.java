package uk.gov.justice.laa.bulkclaim.dto.submission.claim;

import java.util.List;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionCostsSummary;

/**
 * Holds claim information about a submission created by a bulk submission, including a summary of
 * the costs, and a list of claims.
 *
 * @param costsSummary the summary of the costs attached to the submission
 * @param submissionClaims the claims attached to the submission
 * @author Jamie Briggs
 */
public record SubmissionClaimsDetails(
    SubmissionCostsSummary costsSummary, List<SubmissionClaimRow> submissionClaims) {}
