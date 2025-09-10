package uk.gov.justice.laa.bulkclaim.builder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.mapper.BulkClaimImportSummaryMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("Submission summary builder test")
class BulkClaimImportSummaryBuilderTest {

  @Mock DataClaimsRestClient dataClaimsRestClient;
  @Mock BulkClaimImportSummaryMapper bulkClaimImportSummaryMapper;

  BulkClaimSummaryBuilder builder;

  @BeforeEach
  void beforeEach() {
    builder = new BulkClaimSummaryBuilder(dataClaimsRestClient, bulkClaimImportSummaryMapper);
  }

  @Nested
  @DisplayName("Build single submission")
  class SingleSubmission {

    //    @Test
    //    @DisplayName("Should build summary with no errors")
    //    void shouldBuildSummaryWithNoErrors() {
    //      // Given
    //      UUID submissionId = UUID.fromString("2528e557-6c24-4725-b659-6346399bf021");
    //      SubmissionResponse submissionResponse =
    //          SubmissionResponse.builder().submissionId(submissionId).build();
    //      SubmissionSummaryRow expectedSubmissionSummaryRow =
    //          new SubmissionSummaryRow(
    //              LocalDateTime.of(2020, 5, 1, 12, 0, 0),
    //              submissionId,
    //              "Office Account",
    //              "Area of Law",
    //              LocalDate.of(2020, 5, 1),
    //              1);
    //      when(bulkClaimImportSummaryMapper.toSubmissionSummaryRows(List.of(submissionResponse)))
    //          .thenReturn(List.of(expectedSubmissionSummaryRow));
    //      when(dataClaimsRestClient.getValidationErrors(submissionId)).thenReturn(Mono.empty());
    //      // When
    //      BulkClaimImportSummary result = builder.build(singletonList(submissionResponse));
    //      // Then
    //      assertThat(result.submissions().getFirst()).isEqualTo(expectedSubmissionSummaryRow);
    //      assertThat(result.claimErrors().isEmpty()).isTrue();
    //    }
    //
    //    @Test
    //    @DisplayName("Should build summary with errors")
    //    void shouldBuildSummaryWithErrors() {
    //      // Given
    //      UUID submissionId = UUID.fromString("2528e557-6c24-4725-b659-6346399bf021");
    //      SubmissionResponse submissionResponse =
    //          SubmissionResponse.builder().submissionId(submissionId).build();
    //      SubmissionSummaryRow expectedSubmissionSummaryRow =
    //          new SubmissionSummaryRow(
    //              LocalDateTime.of(2020, 5, 1, 12, 0, 0),
    //              submissionId,
    //              "Office Account",
    //              "Area of Law",
    //              LocalDate.of(2020, 5, 1),
    //              1);
    //      when(bulkClaimImportSummaryMapper.toSubmissionSummaryRows(List.of(submissionResponse)))
    //          .thenReturn(List.of(expectedSubmissionSummaryRow));
    //      when(dataClaimsRestClient.getValidationErrors(submissionId))
    //          .thenReturn(Mono.just(singletonList(new ClaimValidationError())));
    //      SubmissionSummaryClaimErrorRow claimError =
    //          new SubmissionSummaryClaimErrorRow(submissionId, "UFN", "UCN", "Client", "Error");
    //      when(bulkClaimImportSummaryMapper.toSubmissionSummaryClaimError(any(), any()))
    //          .thenReturn(claimError);
    //      // When
    //      BulkClaimImportSummary result = builder.build(singletonList(submissionResponse));
    //      // Then
    //      assertThat(result.submissions().getFirst()).isEqualTo(expectedSubmissionSummaryRow);
    //      assertThat(result.claimErrors().getFirst()).isEqualTo(claimError);
    //    }
  }
}
