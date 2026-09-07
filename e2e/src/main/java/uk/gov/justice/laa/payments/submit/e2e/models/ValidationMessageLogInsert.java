package uk.gov.justice.laa.payments.submit.e2e.models;

import java.util.Arrays;
import java.util.List;
import lombok.Builder;

@Builder
public record ValidationMessageLogInsert(
    String id, String submissionId, String claimId, String source, String displayMessage)
    implements Insert {

  @Override
  public String table() {
    return "validation_message_log";
  }

  @Override
  public List<Object> parameters() {
    return Arrays.asList(id, submissionId, claimId, source, displayMessage);
  }
}
