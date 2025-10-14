package uk.gov.justice.laa.bulkclaim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.builder.BulkClaimSummaryBuilder;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionSummaryRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.ClaimMessagesSummary;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionSummaryClaimMessageRow;
import uk.gov.justice.laa.bulkclaim.dto.summary.BulkClaimImportSummary;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@Disabled("Will be removed when BulkSubmissionImportedController is removed")
@WebMvcTest(BulkSubmissionImportedController.class)
@AutoConfigureMockMvc
@Import(WebMvcTestConfig.class)
class BulkSubmissionImportedControllerTest {

  @Autowired private MockMvcTester mockMvc;

  @MockitoBean private DataClaimsRestClient dataClaimsRestClient;
  @MockitoBean private BulkClaimSummaryBuilder bulkClaimSummaryBuilder;

  @Nested
  @DisplayName("GET: /view-submission-summary")
  class ViewBulkClaimImportSummary {

    @Test
    @DisplayName("Should return expected result with submission present")
    void shouldRetuenExpectedResultWithSubmissionPresent() {
      // Given
      UUID submissionId = UUID.fromString("314d1cac-ffb8-41b5-9013-bab4e47e23ca");
      SubmissionResponse submissionResponse =
          SubmissionResponse.builder().submissionId(submissionId).build();
      BulkClaimImportSummary bulkClaimImportSummary = getTestSubmissionSummary(submissionId);
      when(bulkClaimSummaryBuilder.build(List.of(submissionResponse), 0))
          .thenReturn(bulkClaimImportSummary);
      // When / Then
      assertThat(
              mockMvc.perform(
                  get("/view-submission-summary")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(SUBMISSION_ID, submissionId)
                      .sessionAttr(SUBMISSION, submissionResponse)))
          .hasStatusOk()
          .hasViewName("pages/view-submission-imported-summary")
          .model()
          .hasFieldOrProperty(SUBMISSION)
          .hasFieldOrProperty(SUBMISSION_ID);
      verify(bulkClaimSummaryBuilder, times(1)).build(List.of(submissionResponse), 0);
      verify(dataClaimsRestClient, times(0)).getSubmission(submissionId);
    }

    @Test
    @DisplayName("Should return expected result without submission present")
    void shouldReturnExpectedResultWithoutSubmissionPresent() {
      // Given
      UUID submissionId = UUID.fromString("314d1cac-ffb8-41b5-9013-bab4e47e23ca");
      SubmissionResponse submissionResponse =
          SubmissionResponse.builder().submissionId(submissionId).build();
      BulkClaimImportSummary bulkClaimImportSummary = getTestSubmissionSummary(submissionId);
      when(dataClaimsRestClient.getSubmission(submissionId))
          .thenReturn(Mono.just(submissionResponse));
      when(bulkClaimSummaryBuilder.build(List.of(submissionResponse), 0))
          .thenReturn(bulkClaimImportSummary);
      // When / Then
      assertThat(
              mockMvc.perform(
                  get("/view-submission-summary")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(SUBMISSION_ID, submissionId)))
          .hasStatusOk()
          .hasViewName("pages/view-submission-imported-summary")
          .model()
          .hasFieldOrProperty(SUBMISSION)
          .hasFieldOrProperty(SUBMISSION_ID);
      verify(bulkClaimSummaryBuilder, times(1)).build(List.of(submissionResponse), 0);
      verify(dataClaimsRestClient, times(1)).getSubmission(submissionId);
    }
  }

  @ParameterizedTest
  @ValueSource(ints = {400, 401, 403, 500, 503})
  @DisplayName("Should redirect to error when submission not found")
  void shouldReturnExpectedResultWithoutSubmissionPresent(int statusCode) {
    // Given
    UUID submissionId = UUID.fromString("314d1cac-ffb8-41b5-9013-bab4e47e23ca");
    when(dataClaimsRestClient.getSubmission(submissionId))
        .thenThrow(new WebClientResponseException(statusCode, "Error", null, null, null));
    // When / Then
    assertThat(
            mockMvc.perform(
                get("/view-submission-summary")
                    .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                    .sessionAttr(SUBMISSION_ID, submissionId)))
        .failure()
        .hasCauseInstanceOf(SubmitBulkClaimException.class)
        .hasMessageEndingWith("Error retrieving submission from data claims API.");
  }

  private static @NotNull BulkClaimImportSummary getTestSubmissionSummary(
      UUID submissionReference) {
    SubmissionSummaryRow summaryRow =
        new SubmissionSummaryRow(
            OffsetDateTime.of(2025, 1, 1, 10, 10, 10, 0, ZoneOffset.UTC),
            submissionReference,
            "AQB2C3",
            "Legal help",
            LocalDate.of(2025, 5, 10),
            30);
    List<SubmissionSummaryClaimMessageRow> errors =
        List.of(
            new SubmissionSummaryClaimMessageRow(
                submissionReference,
                "UFN1",
                "UCN2",
                "Client",
                "Client",
                null,
                null,
                null,
                null,
                null,
                "This is an error which is found on your claim!",
                "ERROR"));

    Page pagination = Page.builder().totalPages(1).totalElements(0).number(0).size(10).build();

    return new BulkClaimImportSummary(
        Collections.singletonList(summaryRow), new ClaimMessagesSummary(errors, 1, 1, pagination));
  }
}
