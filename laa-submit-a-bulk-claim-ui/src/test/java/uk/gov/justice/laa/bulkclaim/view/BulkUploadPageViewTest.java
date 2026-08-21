package uk.gov.justice.laa.bulkclaim.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper.OIDC_USER;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.ResourceAccessException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.justice.laa.bulkclaim.controller.BulkImportController;
import uk.gov.justice.laa.bulkclaim.metrics.BulkClaimMetricService;
import uk.gov.justice.laa.bulkclaim.service.VirusCheckService;
import uk.gov.justice.laa.bulkclaim.validation.BulkImportFileValidator;
import uk.gov.justice.laa.bulkclaim.validation.BulkImportFileVirusValidator;

@WebMvcTest(BulkImportController.class)
@Import({BulkImportFileValidator.class, BulkImportFileVirusValidator.class})
class BulkUploadPageViewTest extends ViewTestBase {

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
    assertPageHasContent(doc, "The maximum file size is");
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

    MockMultipartFile file =
        new MockMultipartFile("file", "claims.csv", "text/csv", "text".getBytes());

    var response =
        mockMvc
            .perform(
                multipart(mapping)
                    .file(file)
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
}
