package uk.gov.justice.laa.payments.submit.e2e.models;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import lombok.Builder;

@Builder
public record CalculatedFeeDetailInsert(
    String id, String claimSummaryFeeId, String claimId, BigDecimal totalAmount, String userId)
    implements Insert {

  @Override
  public String table() {
    return "calculated_fee_detail";
  }

  @Override
  public List<Object> parameters() {
    return Arrays.asList(id, claimSummaryFeeId, claimId, totalAmount, userId);
  }
}
