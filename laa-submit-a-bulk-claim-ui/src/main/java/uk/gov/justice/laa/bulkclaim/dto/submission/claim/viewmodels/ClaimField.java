package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels;

public record ClaimField(Object reported, Object initialCalculated, Object assessed) {

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
