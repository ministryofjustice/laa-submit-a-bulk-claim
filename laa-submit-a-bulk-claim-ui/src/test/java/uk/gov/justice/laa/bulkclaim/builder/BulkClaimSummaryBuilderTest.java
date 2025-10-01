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
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummaryRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.ClaimMessagesSummary;
import uk.gov.justice.laa.bulkclaim.dto.summary.BulkClaimImportSummary;
import uk.gov.justice.laa.bulkclaim.mapper.BulkClaimImportSummaryMapper;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@ExtendWith(MockitoExtension.class)
class BulkClaimSummaryBuilderTest {

  @Mock private BulkClaimImportSummaryMapper bulkClaimImportSummaryMapper;
  @Mock private SubmissionClaimMessagesBuilder submissionClaimMessagesBuilder;

  @InjectMocks private BulkClaimSummaryBuilder builder;

  @Test
  @DisplayName("should build bulk claim summary including summary rows and claim error summary")
  void shouldBuildBulkClaimSummary() {
    UUID submissionId = UUID.randomUUID();
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder().submissionId(submissionId).build();

    SubmissionSummaryRow summaryRow =
        new SubmissionSummaryRow(OffsetDateTime.now(), submissionId, "12345", "Civil", null, 5);

    Page pagination = Page.builder().totalPages(1).totalElements(0).number(0).size(10).build();

    when(bulkClaimImportSummaryMapper.toSubmissionSummaryRows(List.of(submissionResponse)))
        .thenReturn(List.of(summaryRow));

    ClaimMessagesSummary errorSummary = new ClaimMessagesSummary(List.of(), 0, 0, pagination);

    when(submissionClaimMessagesBuilder.buildErrors(submissionId, 0, 10)).thenReturn(errorSummary);

    BulkClaimImportSummary result = builder.build(List.of(submissionResponse), 0);

    assertThat(result.submissions()).containsExactly(summaryRow);
    assertThat(result.claimMessagesSummary()).isEqualTo(errorSummary);
  }
}
