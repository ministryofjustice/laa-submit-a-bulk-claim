package uk.gov.justice.laa.payments.submit.e2e.models;

import java.util.Arrays;
import java.util.List;
import lombok.Builder;

@Builder
public record ClaimCaseInsert(String id, String claimId, String userId) implements Insert {

  @Override
  public String table() {
    return "claim_case";
  }

  @Override
  public List<Object> parameters() {
    return Arrays.asList(id, claimId, userId);
  }
}
