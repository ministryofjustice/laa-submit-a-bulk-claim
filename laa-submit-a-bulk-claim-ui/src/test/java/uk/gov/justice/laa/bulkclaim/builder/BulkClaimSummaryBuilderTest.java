package uk.gov.justice.laa.bulkclaim.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.laa.bulkclaim.dto.summary.BulkClaimImportSummary;
import uk.gov.justice.laa.bulkclaim.dto.summary.ClaimErrorSummary;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryRow;
import uk.gov.justice.laa.bulkclaim.mapper.BulkClaimImportSummaryMapper;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@ExtendWith(MockitoExtension.class)
class BulkClaimSummaryBuilderTest {

  @Mock private BulkClaimImportSummaryMapper bulkClaimImportSummaryMapper;
  @Mock private SubmissionClaimErrorsBuilder submissionClaimErrorsBuilder;

  @InjectMocks private BulkClaimSummaryBuilder builder;

  @Test
  @DisplayName("should build bulk claim summary including summary rows and claim error summary")
  void shouldBuildBulkClaimSummary() {
    UUID submissionId = UUID.randomUUID();
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder().submissionId(submissionId).build();

    SubmissionSummaryRow summaryRow =
        new SubmissionSummaryRow(OffsetDateTime.now(), submissionId, "12345", "Civil", null, 5);

    when(bulkClaimImportSummaryMapper.toSubmissionSummaryRows(List.of(submissionResponse)))
        .thenReturn(List.of(summaryRow));

    ClaimErrorSummary errorSummary = new ClaimErrorSummary(List.of(), 0, 0);

    when(submissionClaimErrorsBuilder.build(submissionId, 0)).thenReturn(errorSummary);

    BulkClaimImportSummary result = builder.build(List.of(submissionResponse), 0);

    assertThat(result.submissions()).containsExactly(summaryRow);
    assertThat(result.claimErrorSummary()).isEqualTo(errorSummary);
  }
}
