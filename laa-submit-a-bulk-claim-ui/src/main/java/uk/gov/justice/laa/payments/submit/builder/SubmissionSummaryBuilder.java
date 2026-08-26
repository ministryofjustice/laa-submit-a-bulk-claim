package uk.gov.justice.laa.payments.submit.builder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.payments.submit.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.payments.submit.mapper.SubmissionSummaryMapper;

@Component
@RequiredArgsConstructor
public class SubmissionSummaryBuilder {

  private final SubmissionSummaryMapper submissionSummaryMapper;

  public SubmissionSummary build(SubmissionResponse submissionResponse) {
    return submissionSummaryMapper.toSubmissionSummary(submissionResponse);
  }
}
