package uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record ClaimFieldRow(Object reported, Object initialCalculated, Object currentCalculated) {

  public ClaimFieldRow(ClaimReportedAndCalculatedValues initialValues, Object initialCalculated) {
    // Some values are not passed via the user, this handles such cases
    this(
        initialValues != null ? initialValues.reported() : null,
        initialValues != null ? initialValues.initialCalculated() : null,
        initialCalculated);
  }

  public boolean hasCurrentCalculatedValue() {
    return currentCalculated != null;
  }
}
