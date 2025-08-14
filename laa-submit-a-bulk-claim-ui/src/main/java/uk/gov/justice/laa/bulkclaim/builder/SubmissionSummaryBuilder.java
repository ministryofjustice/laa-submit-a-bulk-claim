package uk.gov.justice.laa.bulkclaim.builder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.dto.summary.BulkClaimSummary;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryClaimError;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryRow;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;
import uk.gov.justice.laa.claims.model.SubmissionFields;

/**
 * Builder class for constructing a {@link BulkClaimSummary} object used for displaying submission
 * summary information to the user.
 *
 * @author Jamie Briggs
 */
@Component
public class SubmissionSummaryBuilder {

  private final Random random = new Random();

  /**
   * Maps a {@link GetSubmission200Response} to a {@link BulkClaimSummary}.
   *
   * @param submissionResponse The response to map.
   * @return The mapped {@link BulkClaimSummary}.
   */
  public BulkClaimSummary mapSubmissionSummary(GetSubmission200Response submissionResponse) {
    // TODO: Implement this method by mapping from submissionResponse
    SubmissionFields submission = submissionResponse.getSubmission();
    SubmissionSummaryRow summaryRow =
        new SubmissionSummaryRow(
            submission.getSubmissionId(),
            submission.getOfficeAccountNumber(),
            submission.getAreaOfLaw(),
            getSubmissionPeriod(submissionResponse), 0, 0);
    List<SubmissionSummaryClaimError> errors = getErrors();
    return new BulkClaimSummary(Collections.singletonList(summaryRow), errors);
  }

  private static LocalDate getSubmissionPeriod(GetSubmission200Response submissionResponse) {
    String[] periodArray = submissionResponse.getSubmission().getSubmissionPeriod().split("-");
    return LocalDate.of(Integer.parseInt(periodArray[0]), Integer.parseInt(periodArray[1]), 1);
  }

  private List<SubmissionSummaryClaimError> getErrors() {
    if (random.nextBoolean()) {
      return Collections.emptyList();
    } else {
      return Arrays.asList(
          new SubmissionSummaryClaimError(
              "UFN1",
              "UCN2",
              "Client",
              "This is an error which is found on your claim!"));
    }
  }
}
