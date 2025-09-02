package uk.gov.justice.laa.bulkclaim.builder;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.dto.summary.BulkClaimImportSummary;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryClaimErrorRow;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryRow;
import uk.gov.justice.laa.bulkclaim.mapper.BulkClaimImportSummaryMapper;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.claims.model.ClaimValidationError;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;

@ExtendWith(MockitoExtension.class)
@DisplayName("Submission summary builder test")
class BulkClaimImportSummaryBuilderTest {

  @Mock DataClaimsRestService dataClaimsRestService;
  @Mock BulkClaimImportSummaryMapper bulkClaimImportSummaryMapper;

  BulkClaimSummaryBuilder builder;

  @BeforeEach
  void beforeEach() {
    builder = new BulkClaimSummaryBuilder(dataClaimsRestService, bulkClaimImportSummaryMapper);
  }

  @Nested
  @DisplayName("Build single submission")
  class SingleSubmission {

    @Test
    @DisplayName("Should build summary with no errors")
    void shouldBuildSummaryWithNoErrors() {
      // Given
      UUID submissionId = UUID.fromString("2528e557-6c24-4725-b659-6346399bf021");
      GetSubmission200Response submission200Response =
          GetSubmission200Response.builder().submissionId(submissionId).build();
      SubmissionSummaryRow expectedSubmissionSummaryRow =
          new SubmissionSummaryRow(
              submissionId, "Office Account", "Area of Law", LocalDate.of(2020, 5, 1), 1);
      when(bulkClaimImportSummaryMapper.toSubmissionSummaryRows(List.of(submission200Response)))
          .thenReturn(List.of(expectedSubmissionSummaryRow));
      when(dataClaimsRestService.getValidationErrors(submissionId)).thenReturn(Mono.empty());
      // When
      BulkClaimImportSummary result = builder.build(singletonList(submission200Response));
      // Then
      assertThat(result.submissions().getFirst()).isEqualTo(expectedSubmissionSummaryRow);
      assertThat(result.claimErrors().isEmpty()).isTrue();
    }

    @Test
    @DisplayName("Should build summary with errors")
    void shouldBuildSummaryWithErrors() {
      // Given
      UUID submissionId = UUID.fromString("2528e557-6c24-4725-b659-6346399bf021");
      GetSubmission200Response submission200Response =
          GetSubmission200Response.builder().submissionId(submissionId).build();
      SubmissionSummaryRow expectedSubmissionSummaryRow =
          new SubmissionSummaryRow(
              submissionId, "Office Account", "Area of Law", LocalDate.of(2020, 5, 1), 1);
      when(bulkClaimImportSummaryMapper.toSubmissionSummaryRows(List.of(submission200Response)))
          .thenReturn(List.of(expectedSubmissionSummaryRow));
      when(dataClaimsRestService.getValidationErrors(submissionId))
          .thenReturn(Mono.just(singletonList(new ClaimValidationError())));
      SubmissionSummaryClaimErrorRow claimError =
          new SubmissionSummaryClaimErrorRow(submissionId, "UFN", "UCN", "Client", "Error");
      when(bulkClaimImportSummaryMapper.toSubmissionSummaryClaimError(any(), any()))
          .thenReturn(claimError);
      // When
      BulkClaimImportSummary result = builder.build(singletonList(submission200Response));
      // Then
      assertThat(result.submissions().getFirst()).isEqualTo(expectedSubmissionSummaryRow);
      assertThat(result.claimErrors().getFirst()).isEqualTo(claimError);
    }
  }
}
