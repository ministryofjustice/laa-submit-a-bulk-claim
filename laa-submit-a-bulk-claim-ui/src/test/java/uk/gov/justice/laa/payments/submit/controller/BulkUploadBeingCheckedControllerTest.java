package uk.gov.justice.laa.payments.submit.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static uk.gov.justice.laa.payments.submit.constants.SessionConstants.BULK_SUBMISSION_ID;
import static uk.gov.justice.laa.payments.submit.constants.SessionConstants.SUBMISSION_ID;
import static uk.gov.justice.laa.payments.submit.controller.ControllerTestHelper.OIDC_USER;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.BulkSubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.GetBulkSubmissionStatusById200Response;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.payments.submit.client.DataClaimsRestClient;
import uk.gov.justice.laa.payments.submit.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.payments.submit.metrics.BulkClaimMetricService;

@WebMvcTest(BulkUploadBeingCheckedController.class)
@AutoConfigureMockMvc
public class BulkUploadBeingCheckedControllerTest extends BaseControllerTest {

  @Autowired private MockMvcTester mockMvc;

  @MockitoBean private BulkClaimMetricService bulkClaimMetricService;
  private static final String OFFICE_CODE = "123456";
  private static final OidcUser USER = OIDC_USER;

  @Nested
  @DisplayName("GET: /upload-is-being-checked")
  class UploadIsBeingChecked {

    @ParameterizedTest
    @EnumSource(
        value = BulkSubmissionStatus.class,
        names = {"READY_FOR_PARSING", "PARSING_COMPLETED"})
    @DisplayName("Should return expected result bulk submission is not ready")
    void shouldReturnExpectedResult(BulkSubmissionStatus status) {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      UUID bulkSubmissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f056");
      when(dataClaimsRestClient.getBulkSubmissionSummary(bulkSubmissionId))
          .thenReturn(
              Mono.just(GetBulkSubmissionStatusById200Response.builder().status(status).build()));
      assertThat(
              mockMvc.perform(
                  get("/upload-is-being-checked")
                      .with(oidcLogin().oidcUser(OIDC_USER))
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

      when(dataClaimsRestClient.getBulkSubmissionSummary(bulkSubmissionId))
          .thenThrow(
              new WebClientResponseException(
                  HttpStatusCode.valueOf(404),
                  "Bulk Submission not found",
                  null,
                  null,
                  null,
                  null));

      assertThat(
              mockMvc.perform(
                  get("/upload-is-being-checked")
                      .with(oidcLogin().oidcUser(OIDC_USER))
                      .sessionAttr(SUBMISSION_ID, submissionId)
                      .sessionAttr(BULK_SUBMISSION_ID, bulkSubmissionId)))
          .hasStatusOk()
          .hasViewName("pages/upload-being-checked");
    }

    @ParameterizedTest
    @EnumSource(
        value = BulkSubmissionStatus.class,
        names = {"VALIDATION_SUCCEEDED", "VALIDATION_FAILED"})
    @DisplayName("Should redirect when complete")
    void shouldRedirectWhenSubmissionHasBeenCreated(BulkSubmissionStatus status) {
      // Given
      UUID bulkSubmissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f056");
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");

      when(dataClaimsRestClient.getBulkSubmissionSummary(bulkSubmissionId))
          .thenReturn(
              Mono.just(GetBulkSubmissionStatusById200Response.builder().status(status).build()));
      assertThat(
              mockMvc.perform(
                  get("/upload-is-being-checked")
                      .with(oidcLogin().oidcUser(OIDC_USER))
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
      when(dataClaimsRestClient.getBulkSubmissionSummary(bulkSubmissionId))
          .thenThrow(new WebClientResponseException(statusCode, "Error", null, null, null, null));

      assertThat(
              mockMvc.perform(
                  get("/upload-is-being-checked")
                      .with(oidcLogin().oidcUser(OIDC_USER))
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
      when(dataClaimsRestClient.getBulkSubmissionSummary(bulkSubmissionId))
          .thenReturn(
              Mono.just(
                  GetBulkSubmissionStatusById200Response.builder()
                      .status(BulkSubmissionStatus.PARSING_FAILED)
                      .build()));
      assertThat(
              mockMvc.perform(
                  get("/upload-is-being-checked")
                      .with(oidcLogin().oidcUser(OIDC_USER))
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
      when(dataClaimsRestClient.getBulkSubmissionSummary(bulkSubmissionId))
          .thenReturn(
              Mono.just(
                  GetBulkSubmissionStatusById200Response.builder()
                      .status(BulkSubmissionStatus.UNAUTHORISED)
                      .build()));
      assertThat(
              mockMvc.perform(
                  get("/upload-is-being-checked")
                      .with(oidcLogin().oidcUser(OIDC_USER))
                      .sessionAttr(SUBMISSION_ID, submissionId)
                      .sessionAttr(BULK_SUBMISSION_ID, bulkSubmissionId)))
          .failure()
          .hasCauseInstanceOf(SubmitBulkClaimException.class)
          .hasMessageContaining(
              "Unexpected bulk submission status returned for: " + bulkSubmissionId);
    }
  }

  @Nested
  @DisplayName("GET: /submission/{submissionId}/status")
  class IsSubmissionDoneEndpoint {

    @ParameterizedTest
    @EnumSource(
        value = SubmissionStatus.class,
        names = {"VALIDATION_SUCCEEDED", "VALIDATION_FAILED", "READY_FOR_SUBMISSION"})
    void shouldReturnTrueWhenSubmissionStatusReady(SubmissionStatus status) {
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      SubmissionResponse response =
          SubmissionResponse.builder()
              .submissionId(submissionId)
              .status(status)
              .officeAccountNumber(OFFICE_CODE)
              .build();
      when(dataClaimsRestClient.getSubmission(submissionId)).thenReturn(Mono.just(response));
      when(oidcAttributeUtils.getUserOffices(org.mockito.ArgumentMatchers.any()))
          .thenReturn(List.of(OFFICE_CODE));
      assertThat(
              mockMvc.perform(
                  get("/submission/{submissionId}/status", submissionId)
                      .with(oidcLogin().oidcUser(USER))))
          .hasStatusOk()
          .hasBodyTextEqualTo("true");
    }

    @ParameterizedTest
    @EnumSource(
        value = SubmissionStatus.class,
        names = {"VALIDATION_SUCCEEDED", "VALIDATION_FAILED", "READY_FOR_SUBMISSION"},
        mode = Mode.EXCLUDE)
    void shouldReturnFalseWhenSubmissionStatusNotReady(SubmissionStatus status) {
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      SubmissionResponse response =
          SubmissionResponse.builder()
              .submissionId(submissionId)
              .status(status)
              .officeAccountNumber(OFFICE_CODE)
              .build();
      when(dataClaimsRestClient.getSubmission(submissionId)).thenReturn(Mono.just(response));
      when(oidcAttributeUtils.getUserOffices(USER)).thenReturn(List.of(OFFICE_CODE));
      assertThat(
              mockMvc.perform(
                  get("/submission/{submissionId}/status", submissionId)
                      .with(oidcLogin().oidcUser(USER))))
          .hasStatusOk()
          .hasBodyTextEqualTo("false");
    }

    @Test
    void shouldReturn404WhenApiReturnsNull() {
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(dataClaimsRestClient.getSubmission(submissionId))
          .thenThrow(new WebClientResponseException(404, "Not found", null, null, null));
      assertThat(
              mockMvc.perform(
                  get("/submission/{submissionId}/status", submissionId)
                      .with(oidcLogin().oidcUser(USER))))
          .hasStatus(404);
    }

    @Test
    void shouldReturn404WhenUnknownException() {
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(dataClaimsRestClient.getSubmission(submissionId))
          .thenThrow(new RuntimeException("Something wrong"));
      assertThat(
              mockMvc.perform(
                  get("/submission/{submissionId}/status", submissionId)
                      .with(oidcLogin().oidcUser(USER))))
          .hasStatus(404);
    }

    @ParameterizedTest
    @EnumSource(value = SubmissionStatus.class)
    void shouldReturn403WhenOfficeCodeNotMatching(SubmissionStatus status) {
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      SubmissionResponse response =
          SubmissionResponse.builder()
              .submissionId(submissionId)
              .status(status)
              .officeAccountNumber("54321")
              .build();
      when(dataClaimsRestClient.getSubmission(submissionId)).thenReturn(Mono.just(response));
      when(oidcAttributeUtils.getUserOffices(USER)).thenReturn(List.of(OFFICE_CODE));
      assertThat(
              mockMvc.perform(
                  get("/submission/{submissionId}/status", submissionId)
                      .with(oidcLogin().oidcUser(USER))))
          .hasStatus(403);
    }
  }
}
