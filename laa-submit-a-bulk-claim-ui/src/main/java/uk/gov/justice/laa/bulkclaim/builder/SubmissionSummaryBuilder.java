package uk.gov.justice.laa.bulkclaim.builder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.bulkclaim.mapper.SubmissionSummaryMapper;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@Component
@RequiredArgsConstructor
public class SubmissionSummaryBuilder {

  private final SubmissionSummaryMapper submissionSummaryMapper;

  public SubmissionSummary build(SubmissionResponse submissionResponse) {
    return submissionSummaryMapper.toSubmissionSummary(submissionResponse);
  }
}
