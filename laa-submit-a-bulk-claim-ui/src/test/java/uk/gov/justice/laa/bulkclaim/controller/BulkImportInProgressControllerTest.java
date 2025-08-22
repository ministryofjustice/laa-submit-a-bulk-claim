package uk.gov.justice.laa.bulkclaim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.BULK_SUBMISSION_ID;

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
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;
import uk.gov.justice.laa.claims.model.GetSubmission200ResponseClaimsInner;
import uk.gov.justice.laa.claims.model.GetSubmission200ResponseClaimsInner.StatusEnum;
import uk.gov.justice.laa.claims.model.SubmissionFields;
import uk.gov.justice.laa.claims.model.SubmissionStatus;

@WebMvcTest(BulkImportInProgressController.class)
@AutoConfigureMockMvc
@Import(WebMvcTestConfig.class)
public class BulkImportInProgressControllerTest {

  @Autowired private MockMvcTester mockMvc;

  @MockitoBean private DataClaimsRestService dataClaimsRestService;

  @Nested
  @DisplayName("GET: /import-in-progress")
  class ImportInProgressTests {

    @Test
    @DisplayName("Should return expected result submission is not ready")
    void shouldReturnExpectedResult() {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(dataClaimsRestService.getSubmission(submissionId))
          .thenReturn(
              Mono.just(
                  GetSubmission200Response.builder()
                      .submission(
                          SubmissionFields.builder()
                              .status(SubmissionStatus.READY_FOR_VALIDATION)
                              .build())
                      .claims(
                          Collections.singletonList(
                              GetSubmission200ResponseClaimsInner.builder()
                                  .status(StatusEnum.VALID)
                                  .build()))
                      .build()));

      assertThat(
              mockMvc.perform(
                  get("/import-in-progress")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(BULK_SUBMISSION_ID, submissionId.toString())))
          .hasStatusOk()
          .hasViewName("pages/upload-in-progress");
    }

    @Test
    @DisplayName("Should return expected result when submission not found")
    void shouldReturnExpectedResultWhenSubmissionNotFound() {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(dataClaimsRestService.getSubmission(submissionId))
          .thenThrow(
              new WebClientResponseException(
                  HttpStatusCode.valueOf(404), "Submission not found", null, null, null, null));

      assertThat(
              mockMvc.perform(
                  get("/import-in-progress")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(BULK_SUBMISSION_ID, submissionId.toString())))
          .hasStatusOk()
          .hasViewName("pages/upload-in-progress");
    }

    @ParameterizedTest
    @EnumSource(
        value = SubmissionStatus.class,
        names = {"VALIDATION_SUCCEEDED", "VALIDATION_FAILED"})
    @DisplayName("Should redirect when complete")
    void shouldRedirectWhenMultipleClaimsHasImported(SubmissionStatus status) {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(dataClaimsRestService.getSubmission(submissionId))
          .thenReturn(
              Mono.just(
                  GetSubmission200Response.builder()
                      .submission(SubmissionFields.builder().status(status).build())
                      .claims(
                          Arrays.asList(
                              GetSubmission200ResponseClaimsInner.builder()
                                  .status(StatusEnum.VALID)
                                  .build(),
                              GetSubmission200ResponseClaimsInner.builder()
                                  .status(StatusEnum.INVALID)
                                  .build()))
                      .build()));

      assertThat(
              mockMvc.perform(
                  get("/import-in-progress")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(BULK_SUBMISSION_ID, submissionId.toString())))
          .hasStatus3xxRedirection()
          .hasRedirectedUrl("/view-submission-summary");
    }

    @ParameterizedTest
    @EnumSource(
        value = SubmissionStatus.class,
        names = {"VALIDATION_SUCCEEDED", "VALIDATION_FAILED"})
    @DisplayName("Should redirect when nil submission")
    void shouldRedirectWhenNilSubmission(SubmissionStatus status) {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(dataClaimsRestService.getSubmission(submissionId))
          .thenReturn(
              Mono.just(
                  GetSubmission200Response.builder()
                      .submission(
                          SubmissionFields.builder().isNilSubmission(true).status(status).build())
                      .build()));

      assertThat(
              mockMvc.perform(
                  get("/import-in-progress")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(BULK_SUBMISSION_ID, submissionId.toString())))
          .hasStatus3xxRedirection()
          .hasRedirectedUrl("/view-submission-summary");
    }

    @Test
    @DisplayName("Should throw error when submission has no fields")
    void shouldThrowErrorWhenSubmissionIsNull() {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(dataClaimsRestService.getSubmission(submissionId)).thenReturn(Mono.empty());

      assertThat(
              mockMvc.perform(
                  get("/import-in-progress")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr("bulkClaimSubmissionId", submissionId.toString())))
          .failure()
          .hasCauseInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Submission is null");
    }

    @Test
    @DisplayName("Should throw error when submission has no fields")
    void shouldThrowErrorWhenSubmissionHasNoFields() {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(dataClaimsRestService.getSubmission(submissionId))
          .thenReturn(
              Mono.just(
                  GetSubmission200Response.builder().claims(Collections.emptyList()).build()));

      assertThat(
              mockMvc.perform(
                  get("/import-in-progress")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(BULK_SUBMISSION_ID, submissionId.toString())))
          .failure()
          .hasCauseInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Submission fields is null");
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 401, 403, 500, 503})
    @DisplayName("Should throw error when exception thrown by claims rest service")
    void shouldThrowErrorWhenExceptionThrownByClaimsRestService(int statusCode) {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(dataClaimsRestService.getSubmission(submissionId))
          .thenThrow(new WebClientResponseException(statusCode, "Error", null, null, null, null));

      assertThat(
              mockMvc.perform(
                  get("/import-in-progress")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(BULK_SUBMISSION_ID, submissionId.toString())))
          .failure()
          .hasCauseInstanceOf(SubmitBulkClaimException.class)
          .hasMessageContaining("Claims API returned an error");
    }
  }
}
