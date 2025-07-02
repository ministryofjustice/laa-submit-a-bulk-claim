package uk.gov.justice.laa.cwa.bulkupload.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.justice.laa.cwa.bulkupload.helper.ProviderHelper;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.service.CwaUploadService;
import uk.gov.justice.laa.cwa.bulkupload.service.VirusCheckService;

@WebMvcTest(BulkUploadController.class)
@ExtendWith(MockitoExtension.class)
class BulkUploadControllerTest {

  private static final String PROVIDER = "123";
  private static final String TEST_USER = "test@example.com";

  @Autowired private MockMvc mockMvc;

  @MockitoBean private VirusCheckService virusCheckService;

  @MockitoBean private CwaUploadService cwaUploadService;

  @MockitoBean private ProviderHelper providerHelper;

  private OidcUser getOidcUser() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("sub", "1234567890");
    claims.put("email", "test@example.com");

    OidcIdToken oidcIdToken =
        new OidcIdToken("token123", Instant.now(), Instant.now().plusSeconds(60), claims);
    OidcUserInfo oidcUserInfo = new OidcUserInfo(claims);

    return new DefaultOidcUser(
        List.of(new SimpleGrantedAuthority("ROLE_USER")), oidcIdToken, oidcUserInfo, "email");
  }

  @Test
  void shouldReturnUploadForbiddenWhenProviderHelperThrowsForbidden() throws Exception {
    doThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN))
        .when(providerHelper)
        .populateProviders(any(Model.class), eq(TEST_USER));

    mockMvc
        .perform(get("/").with(oidcLogin().oidcUser(getOidcUser())))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/upload-forbidden"));
  }

  @Test
  void shouldReturnErrorViewWhenProviderHelperThrowsOtherException() throws Exception {
    doThrow(new RuntimeException("Unexpected error"))
        .when(providerHelper)
        .populateProviders(any(Model.class), eq(TEST_USER));

    mockMvc
        .perform(get("/").with(oidcLogin().oidcUser(getOidcUser())))
        .andExpect(status().isOk())
        .andExpect(view().name("error"));
  }

  @Test
  void shouldReturnErrorWhenProviderMissing() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("fileUpload", "test.pdf", "application/pdf", "test".getBytes());

    mockMvc
        .perform(
            multipart("/upload").file(file).with(csrf()).with(oidcLogin().oidcUser(getOidcUser())))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/upload"))
        .andExpect(content().string(containsString("Please select a provider")));
  }

  @Test
  void shouldReturnErrorWhenFileIsEmpty() throws Exception {
    MockMultipartFile emptyFile =
        new MockMultipartFile("fileUpload", "empty.txt", "text/plain", new byte[0]);

    mockMvc
        .perform(
            multipart("/upload")
                .file(emptyFile)
                .param("provider", PROVIDER)
                .with(csrf())
                .with(oidcLogin().oidcUser(getOidcUser())))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/upload"))
        .andExpect(content().string(containsString("Please select a file to upload")));
  }

  @Test
  void shouldReturnErrorWhenFileSizeExceedsLimit() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("fileUpload", "big.csv", "text/csv", new byte[11 * 1024 * 1024]);

    mockMvc
        .perform(
            multipart("/upload")
                .file(file)
                .param("provider", PROVIDER)
                .with(csrf())
                .with(oidcLogin().oidcUser(getOidcUser())))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/upload"))
        .andExpect(content().string(containsString("File size must not exceed 10MB")));
  }

  @Test
  void shouldReturnErrorWhenVirusCheckFails() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("fileUpload", "test.csv", "text/csv", "test".getBytes());
    doThrow(new RuntimeException("Virus detected"))
        .when(virusCheckService)
        .checkVirus(any(MultipartFile.class));
    mockMvc
        .perform(
            multipart("/upload")
                .file(file)
                .param("provider", PROVIDER)
                .with(csrf())
                .with(oidcLogin().oidcUser(getOidcUser())))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/upload"))
        .andExpect(
            content()
                .string(
                    containsString("The file failed the virus scan. Please upload a clean file.")));
  }

  @Test
  void shouldReturnErrorWhenUploadServiceFails() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("fileUpload", "test.csv", "text/csv", "test".getBytes());
    doNothing().when(virusCheckService).checkVirus(any(MultipartFile.class));
    doThrow(new RuntimeException("Upload failed"))
        .when(cwaUploadService)
        .uploadFile(any(MultipartFile.class), eq(PROVIDER), any(String.class));

    mockMvc
        .perform(
            multipart("/upload")
                .file(file)
                .param("provider", PROVIDER)
                .with(csrf())
                .with(oidcLogin().oidcUser(getOidcUser())))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/upload"))
        .andExpect(content().string(containsString("An error occurred while uploading the file.")));
  }

  @Test
  void shouldUploadFileSuccessfully() throws Exception {
    doNothing().when(virusCheckService).checkVirus(any(MultipartFile.class));
    CwaUploadResponseDto response = new CwaUploadResponseDto();
    response.setFileId("file123");
    when(cwaUploadService.uploadFile(any(MultipartFile.class), eq(PROVIDER), any(String.class)))
        .thenReturn(response);
    MockMultipartFile file =
        new MockMultipartFile("fileUpload", "test.csv", "text/csv", "test".getBytes());

    mockMvc
        .perform(
            multipart("/upload")
                .file(file)
                .param("provider", PROVIDER)
                .with(csrf())
                .with(oidcLogin().oidcUser(getOidcUser())))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/submission"));
  }

  @Test
  void shouldAddVendorIdToModelWhenProviderIsInteger() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("fileUpload", "test.csv", "text/csv", "test".getBytes());

    mockMvc
        .perform(
            multipart("/upload")
                .file(file)
                .param("provider", PROVIDER)
                .with(csrf())
                .with(oidcLogin().oidcUser(getOidcUser())))
        .andExpect(status().isOk())
        .andExpect(model().attribute("selectedProvider", 123));
  }
}
