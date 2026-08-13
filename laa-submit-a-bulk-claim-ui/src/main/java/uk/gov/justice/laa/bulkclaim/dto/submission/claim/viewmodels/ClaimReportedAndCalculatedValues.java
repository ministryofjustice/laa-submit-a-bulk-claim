package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record ClaimReportedAndCalculatedValues(Object reported, Object initialCalculated) {

  public ClaimReportedAndCalculatedValues(Object reported, Object initialCalculated) {
    this.reported = reported;
    this.initialCalculated = initialCalculated;
  }

  public boolean hasReportedValue() {
    return reported != null;
  }

  public boolean hasInitialCalculatedValue() {
    return initialCalculated != null;
  }
}
