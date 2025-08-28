package uk.gov.justice.laa.bulkclaim.builder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.bulkclaim.mapper.SubmissionSummaryMapper;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;

/**
 * Builder class for constructing a {@link SubmissionSummary} object used for displaying surface
 * level information about a submission to the user.
 *
 * @author Jamie Briggs
 */
@Component
@RequiredArgsConstructor
public class SubmissionSummaryBuilder {

  private final SubmissionSummaryMapper submissionSummaryMapper;

  /**
   * Builds a {@link SubmissionSummary} object.
   *
   * @param submissionResponse the source submission response.
   * @return the built {@link SubmissionSummary}.
   */
  public SubmissionSummary build(GetSubmission200Response submissionResponse) {
    return submissionSummaryMapper.toSubmissionSummary(submissionResponse);
  }
}
