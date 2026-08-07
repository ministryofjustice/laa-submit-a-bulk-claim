package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels;

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
    return hasReportedValue() ? reported : NOT_APPLICABLE;
  }

  public Object getInitialCalculatedDisplay() {
    return hasInitialCalculatedValue() ? initialCalculated : NOT_APPLICABLE;
  }

  public Object getCurrentCalculatedDisplay() {
    return hasCurrentCalculatedValue() ? currentCalculated : NOT_APPLICABLE;
  }
}
