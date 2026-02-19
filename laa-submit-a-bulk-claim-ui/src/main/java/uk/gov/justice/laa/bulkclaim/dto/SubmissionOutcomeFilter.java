package uk.gov.justice.laa.bulkclaim.dto;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

/**
 * Enum representing filters for submission outcomes in a bulk submission process. It categorizes
 * submissions based on their statuses.
 *
 * <ul>
 *   <li>SUCCEEDED: Includes submissions with statuses VALIDATION_SUCCEEDED.
 *   <li>FAILED: Includes submissions with statuses VALIDATION_FAILED.
 *   <li>ALL: Includes submissions with any status defined in SubmissionStatus.
 * </ul>
 *
 * @author Jamie Briggs
 */
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
