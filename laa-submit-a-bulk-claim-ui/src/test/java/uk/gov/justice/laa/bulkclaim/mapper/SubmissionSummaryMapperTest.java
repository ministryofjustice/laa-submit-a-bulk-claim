package uk.gov.justice.laa.bulkclaim.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;
import uk.gov.justice.laa.claims.model.SubmissionFields;

@DisplayName("Submission summary mapper test")
@ExtendWith(SpringExtension.class)
class SubmissionSummaryMapperTest {

  private SubmissionSummaryMapper mapper;

  @BeforeEach
  void setup() {
    mapper = new SubmissionSummaryMapperImpl();
  }

  @Test
  @DisplayName("Should map submission summary")
  void shouldMapSubmissionSummary() {
    // Given
    UUID submissionReference = UUID.fromString("e20ca04b-09a4-4754-8e88-aea8820d1208");
    GetSubmission200Response getSubmissionResponse =
        GetSubmission200Response.builder()
            .submissionId(submissionReference)
            .submissionPeriod("2025-05")
            .officeAccountNumber("1234567890")
            // TODO: Add submission value to specification
            .areaOfLaw("Civil Law")
            .submitted(LocalDate.of(2025, 1, 1))
            .build();

    // When
    SubmissionSummary result = mapper.toSubmissionSummary(getSubmissionResponse);
    // Then

    SoftAssertions.assertSoftly(
        softAssertions -> {
          softAssertions.assertThat(result.submissionReference()).isEqualTo(submissionReference);
          softAssertions.assertThat(result.status()).isEqualTo("Submitted");
          softAssertions.assertThat(result.submissionPeriod()).isEqualTo(LocalDate.of(2025, 5, 1));
          softAssertions.assertThat(result.officeAccount()).isEqualTo("1234567890");
          softAssertions.assertThat(result.submissionValue()).isEqualTo(new BigDecimal("50.52"));
          softAssertions.assertThat(result.areaOfLaw()).isEqualTo("Civil Law");
          softAssertions
              .assertThat(result.submitted())
              .isEqualTo(LocalDateTime.of(2025, 1, 1, 0, 0, 0));
        });
  }
}
