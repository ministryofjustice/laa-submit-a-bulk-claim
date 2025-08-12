package uk.gov.justice.laa.bulkclaim.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper.getOidcUser;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.dto.FileUploadForm;
import uk.gov.justice.laa.bulkclaim.helper.ProviderHelper;
import uk.gov.justice.laa.bulkclaim.service.ClaimsRestService;
import uk.gov.justice.laa.bulkclaim.validation.BulkImportFileValidator;
import uk.gov.justice.laa.bulkclaim.validation.BulkImportFileVirusValidator;
import uk.gov.justice.laa.claims.model.CreateBulkSubmission201Response;

@WebMvcTest(BulkImportController.class)
@AutoConfigureMockMvc
@Import(WebMvcTestConfig.class)
class BulkImportControllerTest {

  private static final String PROVIDER = "123";
  private static final String TEST_USER = "test@example.com";

  @Autowired private MockMvc mockMvc;

  @MockitoBean private ProviderHelper providerHelper;
  @MockitoBean private BulkImportFileValidator bulkImportFileValidator;
  @MockitoBean private BulkImportFileVirusValidator bulkImportFileVirusValidator;
  @MockitoBean private ClaimsRestService claimsRestService;

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

    @Test
    @DisplayName("Should return forbidden view when provider helper throws forbidden")
    void shouldReturnUploadForbiddenWhenProviderHelperThrowsForbidden() throws Exception {
      doThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN))
          .when(providerHelper)
          .populateProviders(any(Model.class), eq(TEST_USER));

      mockMvc
          .perform(get("/upload").with(oidcLogin().oidcUser(getOidcUser())))
          .andExpect(status().isOk())
          .andExpect(view().name("pages/upload-forbidden"));
    }

    @Test
    @DisplayName("Should return error view when helper generic exception")
    void shouldReturnErrorViewWhenProviderHelperThrowsOtherException() throws Exception {
      doThrow(new RuntimeException("Unexpected error"))
          .when(providerHelper)
          .populateProviders(any(Model.class), eq(TEST_USER));

      mockMvc
          .perform(get("/upload").with(oidcLogin().oidcUser(getOidcUser())))
          .andExpect(status().isOk())
          .andExpect(view().name("error"));
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

      when(claimsRestService.upload(any())).thenThrow(new RuntimeException("Unexpected error"));

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

      when(claimsRestService.upload(any()))
          .thenReturn(
              Mono.just(ResponseEntity.of(Optional.of(new CreateBulkSubmission201Response()))));
      mockMvc
          .perform(
              post("/upload")
                  .sessionAttr("fileUploadForm", input)
                  .with(csrf())
                  .with(oidcLogin().oidcUser(getOidcUser())))
          .andExpect(status().isOk())
          .andExpect(view().name("pages/submission"));
    }
  }
}
