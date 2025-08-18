package uk.gov.justice.laa.bulkclaim.dto.submission;

import java.util.List;

/**
 * Holds claim information about a submission created by a bulk submission, including a summary
 * of the costs, and a list of claims.
 *
 * @param costsSummary the summary of the costs attached to the submission
 * @param submissionClaims the claims attached to the submission
 * @author Jamie Briggs
 */
public record SubmissionClaimDetails(
    SubmissionCostsSummary costsSummary, List<SubmissionClaimRow> submissionClaims) {}
