package uk.gov.justice.laa.cwa.bulkupload.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.security.Principal;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.laa.cwa.bulkupload.helper.ProviderHelper;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.service.CwaUploadService;
import uk.gov.justice.laa.cwa.bulkupload.service.VirusCheckService;

@WebMvcTest(BulkUploadController.class)
@AutoConfigureMockMvc(addFilters = false)
class BulkUploadControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private VirusCheckService virusCheckService;

  @MockitoBean private CwaUploadService cwaUploadService;

  @MockitoBean private ProviderHelper providerHelper;

  @Mock private Principal principal;

  @Test
  void shouldReturnUploadPage() throws Exception {
    mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("pages/upload"));
  }

  @Test
  void shouldReturnUploadForbiddenWhenProviderHelperThrowsForbidden() throws Exception {
    doThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN))
        .when(providerHelper)
        .populateProviders(any(), any());
    mockMvc
        .perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/upload-forbidden"));
  }

  @Test
  void shouldReturnErrorViewWhenProviderHelperThrowsOtherException() throws Exception {
    doThrow(new RuntimeException("Unexpected error"))
        .when(providerHelper)
        .populateProviders(any(), any());
    mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("error"));
  }

  @Test
  void shouldReturnErrorWhenProviderMissing() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("fileUpload", "test.pdf", "application/pdf", "test".getBytes());
    mockMvc
        .perform(multipart("/upload").file(file))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/upload"))
        .andExpect(content().string(containsString("Please select a provider")));
  }

  @Test
  void shouldReturnErrorWhenFileIsEmpty() throws Exception {
    MockMultipartFile emptyFile =
        new MockMultipartFile("fileUpload", "empty.txt", "text/plain", new byte[0]);
    mockMvc
        .perform(multipart("/upload").file(emptyFile).param("provider", "123"))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/upload"))
        .andExpect(content().string(containsString("Please select a file to upload")));
  }

  @Test
  void shouldReturnErrorWhenFileSizeExceedsLimit() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("fileUpload", "big.csv", "text/csv", new byte[11 * 1024 * 1024]);
    mockMvc
        .perform(multipart("/upload").file(file).param("provider", "123"))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/upload"))
        .andExpect(content().string(containsString("File size must not exceed 10MB")));
  }

  @Test
  void shouldReturnErrorWhenVirusCheckFails() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("fileUpload", "test.csv", "text/csv", "test".getBytes());
    doThrow(new RuntimeException("Virus detected")).when(virusCheckService).checkVirus(any());
    mockMvc
        .perform(multipart("/upload").file(file).param("provider", "123"))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/upload"))
        .andExpect(
            content()
                .string(
                    containsString("The file failed the virus scan. Please upload a clean file.")));
  }

  @Test
  void shouldReturnErrorWhenUploadServiceFails() throws Exception {
    when(virusCheckService.checkVirus(any())).thenReturn(null);
    when(principal.getName()).thenReturn("TestUser");
    doThrow(new RuntimeException("Upload failed"))
        .when(cwaUploadService)
        .uploadFile(any(), any(), any());
    MockMultipartFile file =
        new MockMultipartFile("fileUpload", "test.csv", "text/csv", "test".getBytes());
    mockMvc
        .perform(multipart("/upload").file(file).param("provider", "123").principal(principal))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/upload"))
        .andExpect(content().string(containsString("An error occurred while uploading the file.")));
  }

  @Test
  void shouldUploadFileSuccessfully() throws Exception {
    when(virusCheckService.checkVirus(any())).thenReturn(null);
    when(principal.getName()).thenReturn("TestUser");
    CwaUploadResponseDto response = new CwaUploadResponseDto();
    response.setFileId("file123");
    when(cwaUploadService.uploadFile(any(), any(), any())).thenReturn(response);
    MockMultipartFile file =
        new MockMultipartFile("fileUpload", "test.csv", "text/csv", "test".getBytes());
    mockMvc
        .perform(multipart("/upload").file(file).param("provider", "123").principal(principal))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/submission"));
  }

  @Test
  void shouldAddVendorIdToModelWhenProviderIsInteger() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("fileUpload", "test.csv", "text/csv", "test".getBytes());
    mockMvc
        .perform(multipart("/upload").file(file).param("provider", "123"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("selectedProvider", 123));
  }
}
