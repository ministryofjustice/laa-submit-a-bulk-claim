package uk.gov.justice.laa.bulkclaim.mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.stream.Stream;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@DisplayName("Submission summary mapper test")
@ExtendWith(SpringExtension.class)
class SubmissionSummaryMapperTest {

  private SubmissionSummaryMapper mapper;

  @BeforeEach
  void setup() {
    mapper = new SubmissionSummaryMapperImpl();
  }

  static Stream<Arguments> statusMappings() {
    return Stream.of(
        Arguments.of(SubmissionStatus.VALIDATION_SUCCEEDED, "Submitted"),
        Arguments.of(SubmissionStatus.VALIDATION_FAILED, "Invalid"),
        Arguments.of(SubmissionStatus.CREATED, "In progress"),
        Arguments.of(SubmissionStatus.READY_FOR_VALIDATION, "In progress"),
        Arguments.of(SubmissionStatus.VALIDATION_IN_PROGRESS, "In progress"));
  }

  @ParameterizedTest(
      name =
          """
      GIVEN submission status {0}
      WHEN mapping to summary
      THEN status is "{1}"
      """)
  @MethodSource("statusMappings")
  void shouldMapSubmissionStatuses(SubmissionStatus inputStatus, String expectedMappedStatus) {
    // Given
    UUID submissionReference = UUID.fromString("e20ca04b-09a4-4754-8e88-aea8820d1208");
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder()
            .submissionId(submissionReference)
            .submissionPeriod("MAY-2025")
            .officeAccountNumber("1234567890")
            .status(inputStatus)
            .areaOfLaw("Civil Law")
            .submitted(OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
            .build();

    // When
    SubmissionSummary result = mapper.toSubmissionSummary(submissionResponse);

    // Then
    SoftAssertions.assertSoftly(
        softAssertions -> {
          softAssertions.assertThat(result.submissionReference()).isEqualTo(submissionReference);
          softAssertions.assertThat(result.status()).isEqualTo(expectedMappedStatus);
          softAssertions.assertThat(result.submissionPeriod()).isEqualTo(LocalDate.of(2025, 5, 1));
          softAssertions.assertThat(result.officeAccount()).isEqualTo("1234567890");
          softAssertions.assertThat(result.submissionValue()).isEqualTo(new BigDecimal("50.52"));
          softAssertions.assertThat(result.areaOfLaw()).isEqualTo("Civil Law");
          softAssertions
              .assertThat(result.submitted())
              .isEqualTo(OffsetDateTime.of(2025, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC));
        });
  }
}
