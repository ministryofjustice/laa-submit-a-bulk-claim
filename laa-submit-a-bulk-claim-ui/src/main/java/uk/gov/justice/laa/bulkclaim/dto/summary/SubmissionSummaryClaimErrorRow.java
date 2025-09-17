package uk.gov.justice.laa.bulkclaim.dto.summary;

import java.util.UUID;

/**
 * A record representing a claim error in a bulk submission.
 *
 * @param submissionReference the submission reference
 * @param ufn the UFN of the claim
 * @param ucn the UCN of the claim
 * @param client the client of the claim
 * @param message the validation message
 * @author Jamie Briggs
 */
public record SubmissionSummaryClaimErrorRow(
    UUID submissionReference, String ufn, String ucn, String client, String message) {}
