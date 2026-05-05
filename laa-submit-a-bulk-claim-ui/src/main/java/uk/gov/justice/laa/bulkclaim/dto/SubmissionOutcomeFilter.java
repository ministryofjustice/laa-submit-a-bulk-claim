package uk.gov.justice.laa.bulkclaim.dto;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@Getter
public enum SubmissionOutcomeFilter {
  SUCCEEDED(List.of(SubmissionStatus.VALIDATION_SUCCEEDED)),
  FAILED(List.of(SubmissionStatus.VALIDATION_FAILED)),
  ALL(Arrays.asList(SubmissionStatus.values()));

  private final List<SubmissionStatus> statuses;

  SubmissionOutcomeFilter(List<SubmissionStatus> statuses) {
    this.statuses = statuses;
  }
}
