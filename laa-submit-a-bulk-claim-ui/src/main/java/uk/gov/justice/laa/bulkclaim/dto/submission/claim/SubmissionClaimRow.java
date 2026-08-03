package uk.gov.justice.laa.bulkclaim.dto.submission.claim;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;

@Builder
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
    Boolean escapeCase,
    boolean inquestDetailsRequired,
    boolean inquestDetailsComplete) {

  public SubmissionClaimRow withInquestStatus(boolean required, boolean complete) {
    return new SubmissionClaimRow(
        id,
        lineNumber,
        ufn,
        ucn,
        clientForename,
        clientSurname,
        client2Forename,
        client2Surname,
        client2Ucn,
        category,
        matter,
        concludedOrClaimedDate,
        totalMessages,
        status,
        feeType,
        feeCode,
        costsDetails,
        escapeCase,
        required,
        complete);
  }
}
