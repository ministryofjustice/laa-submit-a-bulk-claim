package uk.gov.justice.laa.bulkclaim.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper.getOidcUser;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.Errors;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.dto.FileUploadForm;
import uk.gov.justice.laa.bulkclaim.metrics.BulkClaimMetricService;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;
import uk.gov.justice.laa.bulkclaim.validation.BulkImportFileValidator;
import uk.gov.justice.laa.bulkclaim.validation.BulkImportFileVirusValidator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.CreateBulkSubmission201Response;

@WebMvcTest(BulkImportController.class)
@AutoConfigureMockMvc
@Import(WebMvcTestConfig.class)
class BulkImportControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private BulkImportFileValidator bulkImportFileValidator;
  @MockitoBean private BulkImportFileVirusValidator bulkImportFileVirusValidator;
  @MockitoBean private DataClaimsRestClient dataClaimsRestClient;
  @MockitoBean private OidcAttributeUtils oidcAttributeUtils;
  @MockitoBean private BulkClaimMetricService bulkClaimMetricService;

  @Nested
  @DisplayName("GET: /upload")
  class GetUploadTests {

    @Test
    @DisplayName("Should return expected view")
    void shouldReturnExpectedView() throws Exception {
      mockMvc
          .perform(get("/upload").with(oidcLogin().oidcUser(getOidcUser())))
          .andExpect(status().isOk())
          .andExpect(view().name("pages/upload"));
    }
  }

  @Nested
  @DisplayName("POST: /upload")
  class PostUploadTests {

    @Test
    @DisplayName("Should redirect when file has validation errors")
    void shouldRedirectWhenFileHasValidationErrors() throws Exception {
      MockMultipartFile file =
          new MockMultipartFile("fileUpload", "empty.txt", "text/plain", "text".getBytes());
      FileUploadForm input = new FileUploadForm(file);

      doAnswer(
              invocationOnMock -> {
                Errors errors = invocationOnMock.getArgument(1);
                errors.rejectValue("file", "bulkImport.validation.empty");
                return null;
              })
          .when(bulkImportFileValidator)
          .validate(any(FileUploadForm.class), any(Errors.class));
      mockMvc
          .perform(
              post("/upload")
                  .sessionAttr("fileUploadForm", input)
                  .with(csrf())
                  .with(oidcLogin().oidcUser(getOidcUser())))
          .andExpect(status().is3xxRedirection())
          .andExpect(view().name("redirect:/upload"));
    }

    @Test
    @DisplayName("Should redirect when file fails virus check")
    void shouldRedirectWhenFileFailsVirusCheck() throws Exception {
      MockMultipartFile file =
          new MockMultipartFile("fileUpload", "empty.txt", "text/plain", "text".getBytes());
      FileUploadForm input = new FileUploadForm(file);

      doAnswer(
              invocationOnMock -> {
                Errors errors = invocationOnMock.getArgument(1);
                errors.rejectValue("file", "bulkImport.validation.empty");
                return null;
              })
          .when(bulkImportFileVirusValidator)
          .validate(any(FileUploadForm.class), any(Errors.class));

      mockMvc
          .perform(
              post("/upload")
                  .sessionAttr("fileUploadForm", input)
                  .with(csrf())
                  .with(oidcLogin().oidcUser(getOidcUser())))
          .andExpect(status().is3xxRedirection())
          .andExpect(view().name("redirect:/upload"));
    }

    @Test
    @DisplayName("Should redirect when upload service fails")
    void shouldRedirectWhenUploadServiceFails() throws Exception {
      MockMultipartFile file =
          new MockMultipartFile("fileUpload", "test.csv", "text/csv", "text".getBytes());
      FileUploadForm input = new FileUploadForm(file);

      when(dataClaimsRestClient.upload(any(), any(), any()))
          .thenThrow(new RuntimeException("Unexpected error"));

      mockMvc
          .perform(
              post("/upload")
                  .sessionAttr("fileUploadForm", input)
                  .with(csrf())
                  .with(oidcLogin().oidcUser(getOidcUser())))
          .andExpect(status().is3xxRedirection())
          .andExpect(view().name("redirect:/upload"));
    }

    @Test
    @DisplayName("Should upload file successfully")
    void shouldUploadFileSuccessfully() throws Exception {
      MockMultipartFile file =
          new MockMultipartFile("fileUpload", "test.csv", "text/csv", "text".getBytes());
      FileUploadForm input = new FileUploadForm(file);

      when(dataClaimsRestClient.upload(any(), any(), any()))
          .thenReturn(
              Mono.just(
                  ResponseEntity.of(
                      Optional.of(
                          new CreateBulkSubmission201Response()
                              .bulkSubmissionId(UUID.randomUUID())
                              .submissionIds(List.of(UUID.randomUUID()))))));
      mockMvc
          .perform(
              post("/upload")
                  .flashAttr("fileUploadForm", input)
                  .with(csrf())
                  .with(oidcLogin().oidcUser(getOidcUser())))
          .andExpect(status().is3xxRedirection())
          .andExpect(view().name("redirect:/upload-is-being-checked"));
    }
  }
}
