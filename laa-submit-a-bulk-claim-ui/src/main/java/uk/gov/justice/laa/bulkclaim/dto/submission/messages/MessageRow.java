package uk.gov.justice.laa.bulkclaim.dto.submission.messages;

import java.util.Optional;
import java.util.UUID;
import lombok.Builder;

/**
 * A record representing a claim message in a bulk submission.
 *
 * @param submissionReference the submission reference
 * @param claimReference the claim reference (empty when message is for Submission)
 * @param ufn the UFN of the claim
 * @param ucn the UCN of the claim
 * @param client the client of the claim
 * @param message the validation message
 * @param type the message type
 * @author Jamie Briggs
 */
@Builder
public record MessageRow(
    UUID submissionReference,
    Optional<UUID> claimReference,
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
