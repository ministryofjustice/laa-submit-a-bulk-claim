package uk.gov.justice.laa.bulkclaim.dto.summary;

/**
 * A record representing a claim error in a bulk submission.
 *
 * @param ufn the UFN of the claim
 * @param ucn the UCN of the claim
 * @param client the client of the claim
 * @param errorDescription the error description
 * @author Jamie Briggs
 */
public record SubmissionSummaryClaimError(
    String ufn, String ucn, String client, String errorDescription) {}
