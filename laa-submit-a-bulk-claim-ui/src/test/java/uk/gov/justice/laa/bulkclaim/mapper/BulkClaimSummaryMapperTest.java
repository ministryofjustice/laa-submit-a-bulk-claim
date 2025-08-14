package uk.gov.justice.laa.bulkclaim.mapper;

import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryClaimError;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryRow;
import uk.gov.justice.laa.claims.model.ClaimFields;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;
import uk.gov.justice.laa.claims.model.SubmissionFields;

@ExtendWith(SpringExtension.class)
@DisplayName("Bulk claim summary mapper test")
class BulkClaimSummaryMapperTest {

  private BulkClaimSummaryMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new BulkClaimSummaryMapperImpl();
  }

  @Test
  @DisplayName("Should map submission summary row")
  void shouldMapSubmissionSummaryRow() {
    // Given
    GetSubmission200Response submission200Response = GetSubmission200Response.builder()
        .submission(SubmissionFields.builder()
            .submissionId(UUID.fromString("ee92c4ac-0ff9-4896-8bbe-c58fa04206e3"))
            .officeAccountNumber("1234567890")
            .areaOfLaw("Civil Law")
            .submissionPeriod("2020-05")
            .numberOfClaims(123)
            .build())
        .build();
    // When
    SubmissionSummaryRow result = mapper.toSubmissionSummaryRow(submission200Response);
    // Then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(result.submissionReference())
          .isEqualTo(UUID.fromString("ee92c4ac-0ff9-4896-8bbe-c58fa04206e3"));
      softly.assertThat(result.officeAccount())
          .isEqualTo("1234567890");
      softly.assertThat(result.areaOfLaw())
          .isEqualTo("Civil Law");
      softly.assertThat(result.submissionDate())
          .isEqualTo("2020-05-01");
      softly.assertThat(result.totalClaims())
          .isEqualTo(123);
      // TODO: Update once updated the specification
      softly.assertThat(result.totalErrors()).isEqualTo(100);
    });
  }

  @Test
  @DisplayName("Should map submission summary claim errors")
  void shouldMapSubmissionSummaryClaimErrors() {
    // Given
    ClaimFields claimFields = ClaimFields.builder()
        .uniqueFileNumber("F123")
        .uniqueClientNumber("C123")
        .clientForename("First")
        .clientSurname("Last")
        .build();
    // When
    SubmissionSummaryClaimError result = mapper.toSubmissionSummaryClaimError(claimFields);
    // Then
    SoftAssertions.assertSoftly(softly -> {
      softly.assertThat(result.ufn()).isEqualTo("F123");
      softly.assertThat(result.ucn()).isEqualTo("C123");
      softly.assertThat(result.client()).isEqualTo("First Last");
      // TODO: Update once updated the specification
      softly.assertThat(result.errorDescription()).isEqualTo("Error description");
    });
  }
}
