package uk.gov.justice.laa.bulkclaim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.BULK_SUBMISSION_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.bulkclaim.metrics.BulkClaimMetricService;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.BulkSubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.GetBulkSubmission200Response;

@WebMvcTest(BulkUploadBeingCheckedController.class)
@AutoConfigureMockMvc
@Import(WebMvcTestConfig.class)
public class BulkUploadBeingCheckedControllerTest {

  @Autowired private MockMvcTester mockMvc;

  @MockitoBean private DataClaimsRestClient dataClaimsRestClient;

  @MockitoBean private BulkClaimMetricService bulkClaimMetricService;

  @Nested
  @DisplayName("GET: /upload-is-being-checked")
  class UploadIsBeingChecked {

    @Test
    @DisplayName("Should return expected result bulk submission is not ready")
    void shouldReturnExpectedResult() {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      UUID bulkSubmissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f056");
      when(dataClaimsRestClient.getBulkSubmission(bulkSubmissionId))
          .thenReturn(
              Mono.just(
                  GetBulkSubmission200Response.builder()
                      .status(BulkSubmissionStatus.READY_FOR_PARSING)
                      .build()));
      assertThat(
              mockMvc.perform(
                  get("/upload-is-being-checked")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(SUBMISSION_ID, submissionId)
                      .sessionAttr(BULK_SUBMISSION_ID, bulkSubmissionId)))
          .hasStatusOk()
          .hasViewName("pages/upload-being-checked");
    }

    @Test
    @DisplayName("Should return expected result when bulk submission not found")
    void shouldReturnExpectedResultWhenBulkSubmissionNotFound() {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      UUID bulkSubmissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f056");

      when(dataClaimsRestClient.getBulkSubmission(bulkSubmissionId))
          .thenThrow(
              new WebClientResponseException(
                  HttpStatusCode.valueOf(404), "Submission not found", null, null, null, null));

      assertThat(
              mockMvc.perform(
                  get("/upload-is-being-checked")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(SUBMISSION_ID, submissionId)
                      .sessionAttr(BULK_SUBMISSION_ID, bulkSubmissionId)))
          .hasStatusOk()
          .hasViewName("pages/upload-being-checked");
    }

    @ParameterizedTest
    @EnumSource(
        value = BulkSubmissionStatus.class,
        names = {"VALIDATION_SUCCEEDED", "VALIDATION_FAILED", "PARSING_COMPLETED"})
    @DisplayName("Should redirect when complete")
    void shouldRedirectWhenSubmissionHasBeenCreated(BulkSubmissionStatus status) {
      // Given
      UUID bulkSubmissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f056");
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");

      when(dataClaimsRestClient.getBulkSubmission(bulkSubmissionId))
          .thenReturn(Mono.just(GetBulkSubmission200Response.builder().status(status).build()));
      assertThat(
              mockMvc.perform(
                  get("/upload-is-being-checked")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(SUBMISSION_ID, submissionId)
                      .sessionAttr(BULK_SUBMISSION_ID, bulkSubmissionId)))
          .hasStatus3xxRedirection()
          .hasRedirectedUrl("/submission/5933fc67-bac7-4f48-81ed-61c8c463f054");
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 500, 503})
    @DisplayName("Should throw error when exception thrown by claims rest service")
    void shouldThrowErrorWhenExceptionThrownByClaimsRestService(int statusCode) {
      // Given
      UUID bulkSubmissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f056");
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(dataClaimsRestClient.getBulkSubmission(bulkSubmissionId))
          .thenThrow(new WebClientResponseException(statusCode, "Error", null, null, null, null));

      assertThat(
              mockMvc.perform(
                  get("/upload-is-being-checked")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(SUBMISSION_ID, submissionId)
                      .sessionAttr(BULK_SUBMISSION_ID, bulkSubmissionId)))
          .failure()
          .hasCauseInstanceOf(SubmitBulkClaimException.class)
          .hasMessageContaining("Claims API returned an error");
    }

    @Test
    @DisplayName("Should throw error when parsing fails")
    void shouldThrowErrorWhenExceptionWhenParsingFailed() {
      // Given
      UUID bulkSubmissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f056");
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(dataClaimsRestClient.getBulkSubmission(bulkSubmissionId))
          .thenReturn(
              Mono.just(
                  GetBulkSubmission200Response.builder()
                      .status(BulkSubmissionStatus.PARSING_FAILED)
                      .build()));
      assertThat(
              mockMvc.perform(
                  get("/upload-is-being-checked")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(SUBMISSION_ID, submissionId)
                      .sessionAttr(BULK_SUBMISSION_ID, bulkSubmissionId)))
          .failure()
          .hasCauseInstanceOf(SubmitBulkClaimException.class)
          .hasMessageContaining("Bulk submission parsing failed for: " + bulkSubmissionId);
    }

    @Test
    @DisplayName("Should throw error when status is unexpected")
    void shouldThrowErrorWhenExceptionWhenUnexpectedBulkSubmissionStatus() {
      // Given
      UUID bulkSubmissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f056");
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(dataClaimsRestClient.getBulkSubmission(bulkSubmissionId))
          .thenReturn(
              Mono.just(
                  GetBulkSubmission200Response.builder()
                      .status(BulkSubmissionStatus.UNAUTHORISED)
                      .build()));
      assertThat(
              mockMvc.perform(
                  get("/upload-is-being-checked")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(SUBMISSION_ID, submissionId)
                      .sessionAttr(BULK_SUBMISSION_ID, bulkSubmissionId)))
          .failure()
          .hasCauseInstanceOf(SubmitBulkClaimException.class)
          .hasMessageContaining(
              "Unexpected bulk submission status returned for: " + bulkSubmissionId);
    }
  }
}
