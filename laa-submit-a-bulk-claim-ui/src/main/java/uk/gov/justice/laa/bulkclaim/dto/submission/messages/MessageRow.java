package uk.gov.justice.laa.bulkclaim.dto.submission.messages;

import java.util.Optional;
import java.util.UUID;
import lombok.Builder;

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
