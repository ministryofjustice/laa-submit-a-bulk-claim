package uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels;

public record ClaimFieldRow(Object reported, Object initialCalculated, Object assessed) {

  public boolean hasReportedValue() {
    return reported != null;
  }

  public boolean hasInitialCalculatedValue() {
    return initialCalculated != null;
  }

  public boolean hasAssessedValue() {
    return assessed != null;
  }
}
