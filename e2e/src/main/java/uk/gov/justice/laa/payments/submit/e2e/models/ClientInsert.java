package uk.gov.justice.laa.payments.submit.e2e.models;

import java.util.Arrays;
import java.util.List;
import lombok.Builder;

@Builder
public record ClientInsert(String id, String claimId, String userId) implements Insert {

  @Override
  public String table() {
    return "client";
  }

  @Override
  public List<Object> parameters() {
    return Arrays.asList(id, claimId, userId);
  }
}
