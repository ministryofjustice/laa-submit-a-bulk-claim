package uk.gov.justice.laa.bulkclaim.dto.summary;

/**
 * A record representing a claim error in a bulk submission.
 *
 * @param parentSubmission the parent submission which this error belongs to
 * @param ufn the UFN of the claim
 * @param ucn the UCN of the claim
 * @param client the client of the claim
 * @param errorDescription the error description
 * @author Jamie Briggs
 */
public record SubmissionSummaryClaimError(
    SubmissionSummaryRow parentSubmission,
    String ufn,
    String ucn,
    String client,
    String errorDescription) {}
