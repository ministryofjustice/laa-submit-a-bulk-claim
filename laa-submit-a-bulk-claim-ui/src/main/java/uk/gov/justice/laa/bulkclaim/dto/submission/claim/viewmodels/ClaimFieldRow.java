package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record ClaimFieldRow(Object reported, Object initialCalculated, Object currentCalculated) {

  public static final String NOT_APPLICABLE = "Not applicable";

  public ClaimFieldRow(Object reported, Object initialCalculated) {
    this(reported, initialCalculated, null);
  }

  public boolean hasReportedValue() {
    return reported != null;
  }

  public boolean hasInitialCalculatedValue() {
    return initialCalculated != null;
  }

  public boolean hasCurrentCalculatedValue() {
    return currentCalculated != null;
  }

  public Object getReportedDisplay() {
    return hasReportedValue() ? display(reported) : NOT_APPLICABLE;
  }

  public Object getInitialCalculatedDisplay() {
    return hasInitialCalculatedValue() ? display(initialCalculated) : NOT_APPLICABLE;
  }

  public Object getCurrentCalculatedDisplay() {
    return hasCurrentCalculatedValue() ? display(currentCalculated) : NOT_APPLICABLE;
  }

  private static Object display(Object value) {
    try{

    return value instanceof Boolean bool
        ? (bool ? "Yes" : "No")
        : "£"
            + BigDecimal.valueOf(((Number) value).doubleValue()).setScale(2, RoundingMode.HALF_UP);
    }catch (Exception e){
      log.error("Error displaying value", e);
      return NOT_APPLICABLE;
    }
  }
}
