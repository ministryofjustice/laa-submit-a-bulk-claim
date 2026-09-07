package uk.gov.justice.laa.payments.submit.e2e.models;

import java.util.Arrays;
import java.util.List;
import lombok.Builder;

@Builder
public record ClaimInsert(
    String id,
    String submissionId,
    int lineNumber,
    String scheduleReference,
    String caseReferenceNumber,
    String uniqueFileNumber,
    String matterTypeCode,
    String feeCode,
    String userId)
    implements Insert {

  @Override
  public String table() {
    return "claim";
  }

  @Override
  public List<Object> parameters() {
    return Arrays.asList(
        id,
        submissionId,
        lineNumber,
        scheduleReference,
        caseReferenceNumber,
        uniqueFileNumber,
        matterTypeCode,
        feeCode,
        userId);
  }
}
