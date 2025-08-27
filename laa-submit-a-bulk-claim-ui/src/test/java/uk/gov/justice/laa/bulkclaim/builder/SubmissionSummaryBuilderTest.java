package uk.gov.justice.laa.bulkclaim.builder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.bulkclaim.mapper.SubmissionSummaryMapper;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;
import uk.gov.justice.laa.claims.model.SubmissionFields;

@ExtendWith(MockitoExtension.class)
@DisplayName("Submission summary builder tests")
class SubmissionSummaryBuilderTest {

  SubmissionSummaryBuilder builder;

  @Mock SubmissionSummaryMapper submissionSummaryMapper;

  @BeforeEach
  void beforeEach() {
    builder = new SubmissionSummaryBuilder(submissionSummaryMapper);
  }

  @Test
  @DisplayName("Should return submission summary")
  void shouldReturnSubmissionSummary() {
    // Given
    UUID submissionReference = UUID.fromString("e20ca04b-09a4-4754-8e88-aea8820d1208");
    GetSubmission200Response submission200Response =
        GetSubmission200Response.builder()
            .submission(SubmissionFields.builder().submissionId(submissionReference).build())
            .build();
    SubmissionSummary expected =
        new SubmissionSummary(submissionReference, null, null, null, null, null, null);
    when(submissionSummaryMapper.toSubmissionSummary(submission200Response)).thenReturn(expected);
    // When
    SubmissionSummary result = builder.build(submission200Response);
    // Then
    assertThat(result).isEqualTo(expected);
  }
}
