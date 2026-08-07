package uk.gov.justice.laa.bulkclaim.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.CRIME_LOWER;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimsDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessageRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSource;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

class SubmissionDetailsSummaryViewTest extends SubmissionDetailsViewTestBase {

  @Test
  void acceptedSubmissionPageShowsHeadingAcceptedTagAndBanner() {
    var doc = renderAcceptedPage();

    assertPageHasHeading(doc, "Submission summary");
    assertThat(selectFirst(doc, ".govuk-tag--green").text()).isEqualTo("Accepted");
    assertPageHasSuccessBanner(
        doc,
        "Your submission has been accepted. You cannot make changes. See messages for any actions"
            + " you may want to take. For example, request an amendment(opens in a new tab).");
  }

  @Test
  void acceptedSubmissionPageShowsSummaryFields() {
    var doc = renderAcceptedPage();

    var summaryList = getFirstSummaryList(doc);
    assertThat(summaryList).hasSize(6);
    assertCommonSummaryFields(summaryList);
    assertRowContainsValues(summaryList.get(5), "Calculated bulk claim value", "£123.45");
  }

  @Test
  void acceptedSubmissionPageShowsDownloadAndPrintActions() {
    var doc = renderAcceptedPage();

    assertPageHasSecondaryButton(doc, "Download claims");

    var exportButton = selectFirst(doc, "#export-button");
    assertThat(exportButton.attr("href"))
        .contains("/submission/%s/export".formatted(submissionId))
        .contains("office=0P322F")
        .contains("areaOfLaw=");

    assertThat(selectFirst(doc, "[data-module=laa-print-button]").attr("data-print-action-container"))
        .isEqualTo("secondary-action-container");
  }

  @Test
  void rejectedSubmissionPageShowsErrorAlertAndRejectedTag() {
    var doc = renderRejectedPage();

    assertThat(doc.select(".moj-alert--error")).isNotEmpty();
    assertPageHasHeading(doc, "Submission summary");
    assertPageHasContent(doc, "2 claims have errors for missing or incorrect information");
    assertPageHasContent(doc, "Resolve the errors and upload the file again.");
    assertThat(selectFirst(doc, ".govuk-tag--red").text()).isEqualTo("Rejected");
  }

  @Test
  void rejectedSubmissionPageShowsSummaryFieldsWithoutCalculatedValueOrDownload() {
    var doc = renderRejectedPage();

    var summaryList = getFirstSummaryList(doc);
    assertThat(summaryList).hasSize(5);
    assertCommonSummaryFields(summaryList);

    assertThat(doc.select("#export-button")).isEmpty();
    assertThat(doc.text()).doesNotContain("Calculated bulk claim value");
  }

  @Test
  void rejectedSubmissionPageShowsPrintActionAndNoDownloadAction() {
    var doc = renderRejectedPage();

    assertThat(selectFirst(doc, "[data-module=laa-print-button]").attr("data-print-action-container"))
        .isEqualTo("secondary-action-container");
    assertThat(doc.select(".govuk-button--secondary").eachText()).doesNotContain("Download claims");
  }

  @Test
  void rejectedSubmissionPageShowsClaimErrorHeadersAndRows() {
    var doc = renderRejectedPage();

    assertThat(doc.select(".govuk-table__header").eachText())
        .containsExactly("Client surname", "Client initial", "UFN", "Messages");

    assertThat(selectFirst(doc, "#claim-message-error-0 td:nth-child(1)").text()).isEqualTo("Doe");
    assertThat(selectFirst(doc, "#claim-message-error-0 td:nth-child(2)").text()).isEqualTo("John");
    assertThat(selectFirst(doc, "#claim-message-error-0 td:nth-child(3)").text()).isEqualTo("UFN-001");
    assertThat(selectFirst(doc, "#claim-message-error-0 td:nth-child(4)").text())
        .isEqualTo("The provider is not contracted for the category of law associated with the fee code");
  }

  private void assertCommonSummaryFields(List<List<Element>> summaryList) {
    assertRowContainsValues(summaryList.get(0), "Submission date and time", "1 Jan 2025 at 10:10AM");
    assertRowContainsValues(summaryList.get(1), "Account", "0P322F");
    assertRowContainsValues(summaryList.get(2), "Area of law", "Crime lower");
    assertRowContainsValues(summaryList.get(3), "Submission period", "MAY-2025");
    assertRowContainsValues(summaryList.get(4), "Submission reference", submissionId.toString());
  }

  private Document renderAcceptedPage() {
    mockAcceptedSubmissionSummary();
    return renderDocument();
  }

  private Document renderRejectedPage() {
    mockRejectedSubmissionSummaryWithClaimErrors();
    return renderDocument();
  }

  private void mockAcceptedSubmissionSummary() {
    Page pagination = Page.builder().totalPages(1).totalElements(0).number(0).size(10).build();
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder()
            .submissionId(submissionId)
            .status(SubmissionStatus.VALIDATION_SUCCEEDED)
            .areaOfLaw(CRIME_LOWER)
            .build();
    when(dataClaimsRestClient.getSubmission(submissionId)).thenReturn(Mono.just(submissionResponse));
    when(submissionSummaryBuilder.build(any()))
        .thenReturn(
            new SubmissionSummary(
                submissionId,
                "Submitted",
                LocalDate.of(2025, 5, 1),
                "0P322F",
                BigDecimal.ZERO,
                CRIME_LOWER.getValue(),
                OffsetDateTime.of(2025, 1, 1, 10, 10, 0, 0, ZoneOffset.UTC)));
    when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt(), anyString()))
        .thenReturn(new SubmissionClaimsDetails(List.of(), pagination, new BigDecimal("123.45")));
    when(submissionMessagesBuilder.build(any(), any(), any(), anyInt(), anyInt(), any()))
        .thenReturn(new MessagesSummary(List.of(), 0, 0, pagination, MessagesSource.CLAIM));
  }

  private void mockRejectedSubmissionSummaryWithClaimErrors() {
    Optional<UUID> claimReference = Optional.of(UUID.randomUUID());
    Page pagination = Page.builder().totalPages(1).totalElements(2).number(0).size(10).build();
    SubmissionResponse submissionResponse =
        SubmissionResponse.builder()
            .submissionId(submissionId)
            .status(SubmissionStatus.VALIDATION_FAILED)
            .areaOfLaw(CRIME_LOWER)
            .build();
    when(dataClaimsRestClient.getSubmission(submissionId)).thenReturn(Mono.just(submissionResponse));
    when(submissionSummaryBuilder.build(any()))
        .thenReturn(
            new SubmissionSummary(
                submissionId,
                "Invalid",
                LocalDate.of(2025, 5, 1),
                "0P322F",
                BigDecimal.ZERO,
                CRIME_LOWER.getValue(),
                OffsetDateTime.of(2025, 1, 1, 10, 10, 0, 0, ZoneOffset.UTC)));
    when(submissionMessagesBuilder.buildErrors(any(), anyInt(), anyInt(), any()))
        .thenReturn(
            new MessagesSummary(
                List.of(
                    MessageRow.builder()
                        .claimReference(claimReference)
                        .clientSurname("Doe")
                        .clientForename("John")
                        .ufn("UFN-001")
                        .message("The provider is not contracted for the category of law associated with the fee code")
                        .build(),
                    MessageRow.builder()
                        .claimReference(claimReference)
                        .clientSurname("Doe")
                        .clientForename("John")
                        .ufn("UFN-001")
                        .message("A duplicate claim was found within the same submission")
                        .build()),
                2,
                2,
                pagination,
                MessagesSource.CLAIM));
  }
}
