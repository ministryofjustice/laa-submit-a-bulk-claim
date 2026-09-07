package uk.gov.justice.laa.payments.submit.e2e.models;

import java.util.Arrays;
import java.util.List;
import lombok.Builder;

@Builder
public record BulkSubmissionInsert(String id, String userId) implements Insert {

  @Override
  public String table() {
    return "bulk_submission";
  }

  @Override
  public List<Object> parameters() {
    return Arrays.asList(id, userId);
  }
}
