package uk.gov.justice.laa.bulkclaim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static uk.gov.justice.laa.bulkclaim.config.SessionConstants.BULK_SUBMISSION;
import static uk.gov.justice.laa.bulkclaim.config.SessionConstants.BULK_SUBMISSION_ID;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
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
import uk.gov.justice.laa.bulkclaim.builder.SubmissionSummaryBuilder;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.dto.summary.BulkClaimSummary;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryClaimError;
import uk.gov.justice.laa.bulkclaim.dto.summary.SubmissionSummaryRow;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;
import uk.gov.justice.laa.claims.model.SubmissionFields;

@WebMvcTest(SubmissionController.class)
@AutoConfigureMockMvc
@Import(WebMvcTestConfig.class)
class SubmissionControllerTest {

  @Autowired private MockMvcTester mockMvc;

  @MockitoBean private DataClaimsRestService dataClaimsRestService;
  @MockitoBean private SubmissionSummaryBuilder submissionSummaryBuilder;

  @Nested
  @DisplayName("GET: /view-submission-summary")
  class ViewBulkClaimSummary {

    @Test
    @DisplayName("Should return expected result with submission present")
    void shouldRetuenExpectedResultWithSubmissionPresent() {
      // Given
      UUID bulkSubmissionId = UUID.fromString("314d1cac-ffb8-41b5-9013-bab4e47e23ca");
      GetSubmission200Response bulkSubmission =
          new GetSubmission200Response()
              .submission(SubmissionFields.builder().submissionId(bulkSubmissionId).build());
      BulkClaimSummary bulkClaimSummary = getTestSubmissionSummary(bulkSubmissionId);
      when(submissionSummaryBuilder.mapSubmissionSummary(bulkSubmission))
          .thenReturn(bulkClaimSummary);
      // When / Then
      assertThat(
              mockMvc.perform(
                  get("/view-submission-summary")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(BULK_SUBMISSION_ID, bulkSubmissionId)
                      .sessionAttr(BULK_SUBMISSION, bulkSubmission)))
          .hasStatusOk()
          .hasViewName("pages/view-submission-summary")
          .model()
          .hasFieldOrProperty(BULK_SUBMISSION)
          .hasFieldOrProperty(BULK_SUBMISSION_ID);
      verify(submissionSummaryBuilder, times(1)).mapSubmissionSummary(bulkSubmission);
      verify(dataClaimsRestService, times(0)).getSubmission(bulkSubmissionId);
    }

    @Test
    @DisplayName("Should return expected result without submission present")
    void shouldRetuenExpectedResultWithoutSubmissionPresent() {
      // Given
      UUID bulkSubmissionId = UUID.fromString("314d1cac-ffb8-41b5-9013-bab4e47e23ca");
      GetSubmission200Response bulkSubmission =
          new GetSubmission200Response()
              .submission(SubmissionFields.builder().submissionId(bulkSubmissionId).build());
      BulkClaimSummary bulkClaimSummary = getTestSubmissionSummary(bulkSubmissionId);
      when(dataClaimsRestService.getSubmission(bulkSubmissionId))
          .thenReturn(Mono.just(bulkSubmission));
      when(submissionSummaryBuilder.mapSubmissionSummary(bulkSubmission))
          .thenReturn(bulkClaimSummary);
      // When / Then
      assertThat(
              mockMvc.perform(
                  get("/view-submission-summary")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(BULK_SUBMISSION_ID, bulkSubmissionId)))
          .hasStatusOk()
          .hasViewName("pages/view-submission-summary")
          .model()
          .hasFieldOrProperty(BULK_SUBMISSION)
          .hasFieldOrProperty(BULK_SUBMISSION_ID);
      verify(submissionSummaryBuilder, times(1)).mapSubmissionSummary(bulkSubmission);
      verify(dataClaimsRestService, times(1)).getSubmission(bulkSubmissionId);
    }
  }

  @ParameterizedTest
  @ValueSource(ints = {400, 401, 403, 500, 503})
  @DisplayName("Should redirect to error when submission not found")
  void shouldReturnExpectedResultWithoutSubmissionPresent(int statusCode) {
    // Given
    UUID bulkSubmissionId = UUID.fromString("314d1cac-ffb8-41b5-9013-bab4e47e23ca");
    when(dataClaimsRestService.getSubmission(bulkSubmissionId))
        .thenThrow(new WebClientResponseException(statusCode, "Error", null, null, null));
    // When / Then
    assertThat(
            mockMvc.perform(
                get("/view-submission-summary")
                    .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                    .sessionAttr(BULK_SUBMISSION_ID, bulkSubmissionId)))
        .failure()
        .hasCauseInstanceOf(SubmitBulkClaimException.class)
        .hasMessageEndingWith("Error retrieving submission from data claims API.");
  }

  private static @NotNull BulkClaimSummary getTestSubmissionSummary(UUID submissionReference) {
    SubmissionSummaryRow summaryRow =
        new SubmissionSummaryRow(
            submissionReference, "AQB2C3", "Legal help", LocalDate.of(2025, 5, 10), 30, 1);
    List<SubmissionSummaryClaimError> errors =
        List.of(
            new SubmissionSummaryClaimError(
                "UFN1", "UCN2", "Client", "This is an error which is found on your claim!"));
    return new BulkClaimSummary(Collections.singletonList(summaryRow), errors);
  }
}
