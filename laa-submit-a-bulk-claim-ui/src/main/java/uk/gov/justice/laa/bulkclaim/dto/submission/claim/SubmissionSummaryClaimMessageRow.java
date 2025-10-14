package uk.gov.justice.laa.bulkclaim.dto.submission.claim;

import java.util.UUID;

/**
 * A record representing a claim message in a bulk submission.
 *
 * @param submissionReference the submission reference
 * @param ufn the UFN of the claim
 * @param ucn the UCN of the claim
 * @param client the client of the claim
 * @param message the validation message
 * @param type the message type
 * @author Jamie Briggs
 */
public record SubmissionSummaryClaimMessageRow(
    UUID submissionReference,
    String ufn,
    String ucn,
    String client,
    String clientForename,
    String clientSurname,
    String client2Forename,
    String client2Surname,
    String client2Ucn,
    String crimeMatterTypeCode,
    String message,
    String type) {}
