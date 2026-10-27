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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.payments.submit.dto.submission.SubmissionMatterStartsRow;
import uk.gov.justice.laa.payments.submit.dto.submission.SubmissionSummary;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.SubmissionClaimRow;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.SubmissionClaimsDetails;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessageRow;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessagesSource;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessagesSummary;

class SubmissionDetailsPreSubmissionViewTest extends SubmissionDetailsViewTestBase {

  private static final UUID CLAIM_ID = UUID.randomUUID();

  @BeforeEach
  void beforeEachPreSubmissionViewTest() {
    when(featureFlagsConfig.getIsPreSubmissionEnabled()).thenReturn(true);
    mockSubmission(AreaOfLaw.LEGAL_HELP);
    var total = new BigDecimal("100.50");
    var pagination =
        Page.builder().number(0).totalPages(1).size(PAGE_SIZE).totalElements(1).build();
    var claim =
        SubmissionClaimRow.builder()
            .id(CLAIM_ID)
            .clientForename("Forename")
            .clientSurname("Surname")
            .status("VALIDATED_PENDING_APPROVAL")
            .calculatedValue(total)
            .build();
    when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt(), anyString()))
        .thenReturn(new SubmissionClaimsDetails(List.of(claim), pagination, total));
    var messages =
        new MessagesSummary(
            List.of(
                MessageRow.builder()
                    .claimReference(Optional.of(CLAIM_ID))
                    .clientForename("Forename")
                    .clientSurname("Surname")
                    .message("Check the claim details")
                    .build()),
            1,
            1,
            pagination,
            MessagesSource.CLAIM);
    when(submissionMessagesService.getMessageCounts(any(), any(), any())).thenReturn(messages);
    when(submissionMessagesService.getMessagesWithClaimSummary(
            any(), any(), any(), any(), anyInt(), anyInt(), any()))
        .thenReturn(messages);
    when(submissionMatterStartsDetailsService.getAll(any()))
        .thenReturn(List.of(new SubmissionMatterStartsRow("Category AAP", 3)));
  }

  @Test
  void rendersPreSubmissionBannerAndStatus() {
    var doc = renderDocument();

    assertPageHasTitle(doc, "Your bulk claim is ready for submission");
    assertPageHasHeading(doc, "Submission summary");
    assertThat(doc.select(".moj-alert--information")).isNotEmpty();
    assertPageHasContent(
        doc,
        "Your bulk claim has been uploaded. You must submit or delete it before you can create another one with the same details.");
    assertThat(selectFirst(doc, ".govuk-tag--grey").text()).isEqualTo("Not submitted");
    assertThat(selectFirst(doc, ".moj-alert--information a").attr("href"))
        .isEqualTo("/submissions/search");
  }

  @Test
  void rendersSummaryFieldsWithUploadDateAndCalculatedValue() {
    var doc = renderDocument();

    var summaryList = getFirstSummaryList(doc);
    assertThat(summaryList).hasSize(6);
    assertRowContainsValues(summaryList.get(0), "Upload date and time", "1 Jan 2025 at 10:10AM");
    assertRowContainsValues(summaryList.get(1), "Account", OFFICE_CODE);
    assertRowContainsValues(summaryList.get(2), "Area of law", "Legal help");
    assertRowContainsValues(summaryList.get(3), "Submission period", "MAY-2025");
    assertRowContainsValues(summaryList.get(4), "Submission reference", submissionId.toString());
    assertRowContainsValues(summaryList.get(5), "Calculated bulk claim value", "£100.50");
  }

  @Test
  void rendersMessagesTabWithWarning() {
    var doc = renderDocumentWithParams(Map.of("navTab", "CLAIM_MESSAGES"));

    var rows = doc.select(".govuk-table__body tr");
    assertThat(rows).hasSize(1);
    assertThat(rows.first().text()).contains("Surname", "Forename", "Check the claim details");
  }

  @Test
  void rendersMatterStartsTabWithCounts() {
    var doc = renderDocumentWithParams(Map.of("navTab", "MATTER_STARTS"));

    assertThat(doc.select(".moj-sub-navigation__item").eachText())
        .contains("Claims (1)", "Messages (1)", "Matter starts (3)");
    assertRowContainsValues(
        doc.select("#matter-starts-list .govuk-summary-list__row").first().children(),
        "Category AAP",
        "3");
  }

  @Test
  void hidesMatterStartsTabForCrimeLower() {
    mockSubmission(AreaOfLaw.CRIME_LOWER);

    var doc = renderDocument();

    assertThat(doc.select("#matter-starts-tab")).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(AreaOfLaw.class)
  void rendersPendingClaimTagAndLink(AreaOfLaw areaOfLaw) {
    mockSubmission(areaOfLaw);

    var doc = renderDocument();

    var rows = doc.select("#claims-table tbody tr");
    assertThat(rows).hasSize(1);
    assertThat(selectFirst(rows.first(), ".govuk-tag--grey").text()).isEqualTo("Not submitted");
    assertThat(selectFirst(rows.first(), ".govuk-tag--grey").hasClass("app-tag--not-submitted"))
        .isTrue();
    assertThat(selectFirst(doc, ".govuk-tag--grey").hasClass("app-tag--not-submitted")).isFalse();
    assertThat(selectFirst(rows.first(), "a").attr("href"))
        .isEqualTo(
            "/submissions/%s/claims/%s?page=0&navTab=CLAIM_DETAILS"
                .formatted(submissionId, CLAIM_ID));
  }

  @Test
  void doesNotRenderPrintAction() {
    var doc = renderDocument();

    assertThat(doc.select("[data-module=laa-print-button]")).isEmpty();
    assertThat(doc.select("#print-button")).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(AreaOfLaw.class)
  void rendersDownloadClaimsActionOnTheRight(AreaOfLaw areaOfLaw) {
    mockSubmission(areaOfLaw);

    var doc = renderDocument();

    assertPageHasSecondaryButton(doc, "Download claims");
    var exportButton =
        selectFirst(doc, "#secondary-action-container.sabc-container__align_right #export-button");
    assertThat(exportButton.text()).isEqualTo("Download claims");
    assertThat(exportButton.attr("href"))
        .isEqualTo("/submissions/%s/export".formatted(submissionId));
    assertThat(exportButton.attr("data-module")).isEqualTo("laa-hide-on-print-button");
  }

  @Test
  void rendersSubmitAndDeleteActions() {
    var doc = renderDocument();

    assertThat(selectFirst(doc, "#submit-submission-button").text()).isEqualTo("Submit");
    assertThat(selectFirst(doc, "#delete-submission-button").text()).isEqualTo("Delete");
    assertThat(selectFirst(doc, "#delete-submission-button").hasClass("govuk-button--secondary"))
        .isTrue();
  }

  @Test
  void rendersNilSubmissionWithoutClaimsAndWithZeroValue() {
    var submission = mockSubmission(AreaOfLaw.LEGAL_HELP);
    submission.setIsNilSubmission(true);
    submission.setCalculatedTotalAmount(BigDecimal.ZERO);
    var pagination =
        Page.builder().number(0).totalPages(1).size(PAGE_SIZE).totalElements(0).build();
    when(submissionClaimDetailsBuilder.build(any(), anyInt(), anyInt(), anyString()))
        .thenReturn(new SubmissionClaimsDetails(List.of(), pagination, BigDecimal.ZERO));
    var messages = new MessagesSummary(List.of(), 0, 0, pagination, MessagesSource.CLAIM);
    when(submissionMessagesService.getMessageCounts(any(), any(), any())).thenReturn(messages);
    when(submissionMessagesService.getMessagesWithClaimSummary(
            any(), any(), any(), any(), anyInt(), anyInt(), any()))
        .thenReturn(messages);
    when(submissionMatterStartsDetailsService.getAll(any())).thenReturn(List.of());

    var doc = renderDocument();

    assertThat(doc.select("#claims-table")).isEmpty();
    assertThat(doc.select(".moj-sub-navigation__item").eachText())
        .contains("Claims (0)", "Messages (0)", "Matter starts (0)");
    assertRowContainsValues(
        getFirstSummaryList(doc).get(5), "Calculated bulk claim value", "£0.00");
    assertThat(selectFirst(doc, ".govuk-tag--grey").text()).isEqualTo("Not submitted");
  }

  private SubmissionResponse mockSubmission(AreaOfLaw areaOfLaw) {
    var total = new BigDecimal("100.50");
    var submission =
        SubmissionResponse.builder()
            .submissionId(submissionId)
            .status(SubmissionStatus.VALIDATED_PENDING_APPROVAL)
            .officeAccountNumber(OFFICE_CODE)
            .areaOfLaw(areaOfLaw)
            .isNilSubmission(false)
            .calculatedTotalAmount(total)
            .build();
    when(submissionService.getSubmission(submissionId, OIDC_USER)).thenReturn(submission);
    when(submissionSummaryBuilder.build(submission))
        .thenReturn(
            new SubmissionSummary(
                submissionId,
                "Not submitted",
                LocalDate.of(2025, 5, 1),
                OFFICE_CODE,
                total,
                areaOfLaw.getValue(),
                OffsetDateTime.of(2025, 1, 1, 10, 10, 10, 0, ZoneOffset.UTC)));
    return submission;
  }
}
