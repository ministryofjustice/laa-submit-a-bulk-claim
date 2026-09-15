package uk.gov.justice.laa.payments.submit.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.justice.laa.payments.submit.controller.ControllerTestHelper.OIDC_USER;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.justice.laa.payments.submit.controller.BulkImportController;
import uk.gov.justice.laa.payments.submit.metrics.BulkClaimMetricService;
import uk.gov.justice.laa.payments.submit.service.VirusCheckService;
import uk.gov.justice.laa.payments.submit.validation.BulkImportFileValidator;
import uk.gov.justice.laa.payments.submit.validation.BulkImportFileVirusValidator;

@WebMvcTest(BulkImportController.class)
@Import({BulkImportFileValidator.class, BulkImportFileVirusValidator.class})
class BulkUploadPageViewTest extends ViewTestBase {

  private static final MockMultipartFile MOCK_MULTIPART_FILE =
      new MockMultipartFile("file", "claims.csv", "text/csv", "text".getBytes());
  @MockitoBean private VirusCheckService virusCheckService;
  @MockitoBean private BulkClaimMetricService bulkClaimMetricService;
  @MockitoBean private ObjectMapper objectMapper;

  BulkUploadPageViewTest() {
    this.mapping = "/upload";
  }

  @Test
  void uploadPageShowsExpectedContent() {
    var doc = renderDocument();

    assertPageHasTitle(doc, "Upload a bulk claim file");
    assertPageDoesNotHaveBackLink(doc);
    assertPageHasPrimaryButton(doc, "Continue");
    assertPageHasLabel(doc, "file-input", "Upload an XML, CSV, or TXT file");
    assertPageHasContent(doc, "The file must be 10MB or smaller.");
    assertPageHasContent(doc, "We will check your file on the next screen.");
  }

  @Test
  void uploadPageShowsNilSubmissionLinkWhenEnabled() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);

    var doc = renderDocument();

    assertPageHasContent(doc, "Create a nil submission");
  }

  @Test
  void uploadPageShowsFileInputField() {
    var doc = renderDocument();

    var fileInput = doc.selectFirst("input[type=file]");
    assertThat(fileInput).isNotNull();
    assertThat(fileInput.id()).isEqualTo("file-input");
    assertThat(fileInput.attr("name")).isEqualTo("file");
  }

  @Test
  void uploadPageHidesNilSubmissionContentWhenFlagDisabled() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(false);

    var doc = renderDocument();

    assertThat(doc.text()).doesNotContain("Create a nil submission");
  }

  @Test
  void uploadPageShowsHeaderProviderAndSignedInUser() {
    var doc = renderDocument();

    assertPageHasContent(doc, "Legal Aid Agency");
    assertPageHasContent(doc, "test@example.com");
  }

  @Test
  void uploadPageShowsErrorSummaryWhenVirusCheckServiceFails() throws Exception {
    doThrow(new ResourceAccessException("SDS unavailable"))
        .when(virusCheckService)
        .checkVirus(any());
    var response =
        mockMvc
            .perform(
                multipart(mapping)
                    .file(MOCK_MULTIPART_FILE)
                    .with(csrf())
                    .with(oidcLogin().oidcUser(OIDC_USER))
                    .session(session))
            .andReturn()
            .getResponse();

    assertThat(response.getStatus()).isEqualTo(200);
    var doc = Jsoup.parse(response.getContentAsString());
    assertThat(selectFirst(doc, ".govuk-error-summary__title").text())
        .isEqualTo("There is a problem");
    assertPageHasContent(doc, "Something went wrong. The error has been logged. Please try again.");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "VAT Applicable must only include Y or N",
        "Case Start Date must be a valid date in the format DD/MM/YYYY"
      })
  void uploadPageShowsErrorOnValueValidationFailure(String errorDetails) throws Exception {
    var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorDetails);
    byte[] responseBody = ("{\"detail\":\"" + errorDetails + "\"}").getBytes();
    when(objectMapper.writeValueAsBytes(problemDetail)).thenReturn(responseBody);
    when(objectMapper.readValue(anyString(), eq(ProblemDetail.class))).thenReturn(problemDetail);
    when(dataClaimsRestClient.upload(
            eq(MOCK_MULTIPART_FILE), eq(OIDC_USER.getPreferredUsername()), any()))
        .thenThrow(
            new WebClientResponseException(
                HttpStatus.BAD_REQUEST.value(), "bad request", null, responseBody, null));

    var response =
        mockMvc
            .perform(
                multipart(mapping)
                    .file(MOCK_MULTIPART_FILE)
                    .with(csrf())
                    .with(oidcLogin().oidcUser(OIDC_USER))
                    .session(session))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();

    assertThat(response.getStatus()).isEqualTo(200);
    var doc = Jsoup.parse(response.getContentAsString());

    assertThat(selectFirst(doc, ".govuk-error-summary__title").text())
        .isEqualTo("There is a problem");
    assertThat(selectFirst(doc, ".govuk-error-message").text()).isEqualTo("Error: " + errorDetails);
  }

  @Test
  void uploadPageShowsErrorOnUnHandledFailure() throws Exception {
    when(dataClaimsRestClient.upload(any(), any(), any()))
        .thenThrow(new RuntimeException("Unhandled exception"));

    var response =
        mockMvc
            .perform(
                multipart(mapping)
                    .file(MOCK_MULTIPART_FILE)
                    .with(csrf())
                    .with(oidcLogin().oidcUser(OIDC_USER))
                    .session(session))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();

    assertThat(response.getStatus()).isEqualTo(200);
    var doc = Jsoup.parse(response.getContentAsString());

    assertThat(selectFirst(doc, ".govuk-error-summary__title").text())
        .isEqualTo("There is a problem");
    assertPageHasContent(doc, "The selected file could not be uploaded - try again");
  }

  @Test
  void uploadPageShowsErrorOnMetricUnHandledFailure() throws Exception {
    var errorDetails = "Invalid data in file";
    var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorDetails);
    byte[] responseBody = ("{\"detail\":\"" + errorDetails + "\"}").getBytes();
    when(objectMapper.writeValueAsBytes(problemDetail)).thenReturn(responseBody);
    when(objectMapper.readValue(anyString(), eq(ProblemDetail.class))).thenReturn(problemDetail);
    when(dataClaimsRestClient.upload(
            eq(MOCK_MULTIPART_FILE), eq(OIDC_USER.getPreferredUsername()), any()))
        .thenThrow(
            new WebClientResponseException(
                HttpStatus.BAD_REQUEST.value(), "bad request", null, responseBody, null));

    doThrow(new IllegalArgumentException("Something went wrong"))
        .when(bulkClaimMetricService)
        .recordFailedFileUploadSize(anyLong(), anyString());

    var response =
        mockMvc
            .perform(
                multipart(mapping)
                    .file(MOCK_MULTIPART_FILE)
                    .with(csrf())
                    .with(oidcLogin().oidcUser(OIDC_USER))
                    .session(session))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();

    verify(bulkClaimMetricService).recordFailedFileUploadSize(anyLong(), eq(errorDetails));

    assertThat(response.getStatus()).isEqualTo(200);
    var doc = Jsoup.parse(response.getContentAsString());

    assertThat(selectFirst(doc, ".govuk-error-summary__title").text())
        .isEqualTo("There is a problem");
    assertPageHasContent(doc, "The selected file could not be uploaded - try again");
  }
}
