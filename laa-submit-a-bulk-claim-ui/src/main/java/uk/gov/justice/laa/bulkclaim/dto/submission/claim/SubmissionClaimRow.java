package uk.gov.justice.laa.bulkclaim.dto.submission.claim;

import java.time.LocalDate;
import java.util.UUID;

public record SubmissionClaimRow(
    UUID id,
    int lineNumber,
    String ufn,
    String ucn,
    String clientForename,
    String clientSurname,
    String client2Forename,
    String client2Surname,
    String client2Ucn,
    String category,
    String matter,
    LocalDate concludedOrClaimedDate,
    int totalMessages,
    String status,
    String feeType,
    String feeCode,
    SubmissionClaimRowCostsDetails costsDetails,
    Boolean escapeCase) {}
