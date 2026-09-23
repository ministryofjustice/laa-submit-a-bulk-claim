package uk.gov.justice.laa.payments.submit.view.submissiondetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.payments.submit.controller.ControllerTestHelper.OIDC_USER;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.payments.submit.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.SubmissionClaimsDetails;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessagesSource;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessagesSummary;

class SubmissionDetailsExportViewTest extends SubmissionDetailsViewTestBase {

  @ParameterizedTest
  @CsvSource({
    "LEGAL_HELP, LEGAL%20HELP",
    "CRIME_LOWER, CRIME%20LOWER",
    "MEDIATION, MEDIATION",
  })
  void acceptedSubmissionShowsExportActionForAreaOfLaw(
      AreaOfLaw areaOfLaw, String expectedAreaOfLawParam) {
    mockAcceptedSubmission(areaOfLaw);

    var doc = renderDocument();

    assertPageHasSecondaryButton(doc, "Download claims");

    var exportButton = selectFirst(doc, "#export-button");
    assertThat(exportButton.text()).isEqualTo("Download claims");
    assertThat(exportButton.attr("href"))
        .contains("/submissions/%s/export".formatted(submissionId))
        .contains("office=%s".formatted(OFFICE_CODE))
        .contains("areaOfLaw=%s".formatted(expectedAreaOfLawParam));
  }

  @ParameterizedTest
  @EnumSource(AreaOfLaw.class)
  void exportActionIsHiddenFromPrintOutput(AreaOfLaw areaOfLaw) {
    mockAcceptedSubmission(areaOfLaw);

    var doc = renderDocument();

    assertThat(selectFirst(doc, "#export-button").attr("data-module"))
        .isEqualTo("laa-hide-on-print-button");
  }

  @ParameterizedTest
  @EnumSource(
      value = SubmissionStatus.class,
      names = {"VALIDATION_SUCCEEDED", "VALIDATION_IN_PROGRESS"},
      mode = EnumSource.Mode.EXCLUDE)
  void ineligibleSubmissionDoesNotShowExportAction(SubmissionStatus status) {
    mockSubmission(AreaOfLaw.LEGAL_HELP, status);

    var doc = renderDocument();

    assertThat(doc.select("#export-button")).isEmpty();
    assertThat(doc.select(".govuk-button--secondary").eachText()).doesNotContain("Download claims");
  }

  private void mockAcceptedSubmission(AreaOfLaw areaOfLaw) {
    mockSubmission(areaOfLaw, SubmissionStatus.VALIDATION_SUCCEEDED);
  }

  private void mockSubmission(AreaOfLaw areaOfLaw, SubmissionStatus status) {
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder()
            .submissionId(submissionId)
            .status(status)
            .officeAccountNumber(OFFICE_CODE)
            .areaOfLaw(areaOfLaw)
            .build();
    when(submissionService.getSubmission(submissionId, OIDC_USER)).thenReturn(submissionResponse);

    when(submissionSummaryBuilder.build(any()))
        .thenReturn(
            new SubmissionSummary(
                submissionId,
                "Submitted",
                LocalDate.of(2025, 5, 1),
                OFFICE_CODE,
                BigDecimal.ZERO,
                areaOfLaw.getValue(),
                OffsetDateTime.of(2025, 1, 1, 10, 10, 0, 0, ZoneOffset.UTC)));

    Page pagination = pagination(0, 1);
    when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt(), anyString()))
        .thenReturn(new SubmissionClaimsDetails(List.of(), pagination, BigDecimal.ZERO));

    MessagesSummary messagesSummary =
        new MessagesSummary(List.of(), 0, 0, pagination, MessagesSource.CLAIM);
    when(submissionMessagesBuilder.build(any(), any(), any(), any(), anyInt(), anyInt(), any()))
        .thenReturn(messagesSummary);
    when(submissionMessagesBuilder.buildErrors(any(), any(), anyInt(), anyInt(), any()))
        .thenReturn(messagesSummary);

    when(submissionMatterStartsDetailsBuilder.build(any())).thenReturn(List.of());
  }
}
