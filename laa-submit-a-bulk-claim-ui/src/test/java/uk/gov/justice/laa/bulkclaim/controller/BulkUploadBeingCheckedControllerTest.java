package uk.gov.justice.laa.bulkclaim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionClaim;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@WebMvcTest(BulkUploadBeingCheckedController.class)
@AutoConfigureMockMvc
@Import(WebMvcTestConfig.class)
public class BulkUploadBeingCheckedControllerTest {

  @Autowired private MockMvcTester mockMvc;

  @MockitoBean private DataClaimsRestClient dataClaimsRestClient;

  @Nested
  @DisplayName("GET: /upload-is-being-checked")
  class UploadIsBeingChecked {

    @Test
    @DisplayName("Should return expected result submission is not ready")
    void shouldReturnExpectedResult() {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(dataClaimsRestClient.getSubmission(submissionId))
          .thenReturn(
              Mono.just(
                  SubmissionResponse.builder()
                      .status(SubmissionStatus.READY_FOR_VALIDATION)
                      .claims(
                          Collections.singletonList(
                              SubmissionClaim.builder().status(ClaimStatus.VALID).build()))
                      .build()));

      assertThat(
              mockMvc.perform(
                  get("/upload-is-being-checked")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(SUBMISSION_ID, submissionId)))
          .hasStatusOk()
          .hasViewName("pages/upload-being-checked");
    }

    @Test
    @DisplayName("Should return expected result when submission not found")
    void shouldReturnExpectedResultWhenSubmissionNotFound() {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(dataClaimsRestClient.getSubmission(submissionId))
          .thenThrow(
              new WebClientResponseException(
                  HttpStatusCode.valueOf(404), "Submission not found", null, null, null, null));

      assertThat(
              mockMvc.perform(
                  get("/upload-is-being-checked")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(SUBMISSION_ID, submissionId)))
          .hasStatusOk()
          .hasViewName("pages/upload-being-checked");
    }

    @ParameterizedTest
    @EnumSource(
        value = SubmissionStatus.class,
        names = {"VALIDATION_SUCCEEDED", "VALIDATION_FAILED"})
    @DisplayName("Should redirect when complete")
    void shouldRedirectWhenMultipleClaimsHasImported(SubmissionStatus status) {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(dataClaimsRestClient.getSubmission(submissionId))
          .thenReturn(
              Mono.just(
                  SubmissionResponse.builder()
                      .status(status)
                      .claims(
                          Arrays.asList(
                              SubmissionClaim.builder().status(ClaimStatus.VALID).build(),
                              SubmissionClaim.builder().status(ClaimStatus.INVALID).build()))
                      .build()));

      assertThat(
              mockMvc.perform(
                  get("/upload-is-being-checked")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(SUBMISSION_ID, submissionId)))
          .hasStatus3xxRedirection()
          .hasRedirectedUrl("/submission/5933fc67-bac7-4f48-81ed-61c8c463f054");
    }

    @ParameterizedTest
    @EnumSource(
        value = SubmissionStatus.class,
        names = {"VALIDATION_SUCCEEDED", "VALIDATION_FAILED"})
    @DisplayName("Should redirect when nil submission")
    void shouldRedirectWhenNilSubmission(SubmissionStatus status) {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(dataClaimsRestClient.getSubmission(submissionId))
          .thenReturn(
              Mono.just(SubmissionResponse.builder().isNilSubmission(true).status(status).build()));

      assertThat(
              mockMvc.perform(
                  get("/upload-is-being-checked")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(SUBMISSION_ID, submissionId)))
          .hasStatus3xxRedirection()
          .hasRedirectedUrl("/submission/5933fc67-bac7-4f48-81ed-61c8c463f054");
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 500, 503})
    @DisplayName("Should throw error when exception thrown by claims rest service")
    void shouldThrowErrorWhenExceptionThrownByClaimsRestService(int statusCode) {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(dataClaimsRestClient.getSubmission(submissionId))
          .thenThrow(new WebClientResponseException(statusCode, "Error", null, null, null, null));

      assertThat(
              mockMvc.perform(
                  get("/upload-is-being-checked")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(SUBMISSION_ID, submissionId)))
          .failure()
          .hasCauseInstanceOf(SubmitBulkClaimException.class)
          .hasMessageContaining("Claims API returned an error");
    }
  }
}
