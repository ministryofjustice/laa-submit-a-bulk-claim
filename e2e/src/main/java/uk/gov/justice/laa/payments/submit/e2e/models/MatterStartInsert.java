package uk.gov.justice.laa.payments.submit.e2e.models;

import java.util.Arrays;
import java.util.List;
import lombok.Builder;

@Builder
public record MatterStartInsert(
    String id, String submissionId, String categoryCode, String mediationType, String userId)
    implements Insert {

  @Override
  public String table() {
    return "matter_start";
  }

  @Override
  public List<Object> parameters() {
    return Arrays.asList(id, submissionId, 1, categoryCode, mediationType, userId);
  }
}
