package uk.gov.justice.laa.payments.submit.dto;

import java.util.List;
import lombok.Getter;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@Getter
public enum SubmissionOutcomeFilter {
  SUCCEEDED(List.of(SubmissionStatus.VALIDATION_SUCCEEDED)),
  FAILED(List.of(SubmissionStatus.VALIDATION_FAILED)),
  NOT_SUBMITTED(List.of(SubmissionStatus.VALIDATED_PENDING_APPROVAL));

  private final List<SubmissionStatus> statuses;

  SubmissionOutcomeFilter(List<SubmissionStatus> statuses) {
    this.statuses = statuses;
  }
}
