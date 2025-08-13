package uk.gov.justice.laa.bulkclaim.builder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.springframework.stereotype.Component;
import uk.gov.justice.laa.bulkclaim.dto.summary.BulkClaimSummary;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryClaimError;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryRow;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;

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
    SubmissionSummaryRow summaryRow =
        new SubmissionSummaryRow(
            UUID.randomUUID(), "AQB2C3", "Legal help", LocalDate.of(2025, 5, 10), 30, 1);
    List<SubmissionSummaryClaimError> errors = getErrors(summaryRow);
    return new BulkClaimSummary(Collections.singletonList(summaryRow), errors);
  }

  private List<SubmissionSummaryClaimError> getErrors(SubmissionSummaryRow submissionSummaryRow) {
    if (random.nextBoolean()) {
      return Collections.emptyList();
    } else {
      return Arrays.asList(
          new SubmissionSummaryClaimError(
              submissionSummaryRow,
              "UFN1",
              "UCN2",
              "Client",
              "This is an error which is found on your claim!"));
    }
  }
}
