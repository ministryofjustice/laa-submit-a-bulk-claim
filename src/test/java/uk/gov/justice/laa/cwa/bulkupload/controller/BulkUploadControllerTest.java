package uk.gov.justice.laa.cwa.bulkupload.controller;

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
import uk.gov.justice.laa.cwa.bulkupload.service.TokenService;
import uk.gov.justice.laa.cwa.bulkupload.service.VirusCheckService;

import java.security.Principal;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(BulkUploadController.class)
@AutoConfigureMockMvc(addFilters = false)
class BulkUploadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VirusCheckService virusCheckService;

    @MockitoBean
    private CwaUploadService cwaUploadService;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private ProviderHelper providerHelper;

    @Mock
    private Principal principal;

    @Test
    void shouldReturnUploadPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/upload"))
                .andExpect(content().string(containsString("Select a file to upload")));
    }

    @Test
    void shouldReturnErrorWhenFetchingProvidersFailsWithForbidden() throws Exception {
        doThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN)).when(providerHelper).populateProviders(any(), any());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/upload-forbidden"))
                .andExpect(content().string(containsString("You do not have permission to access this application")));
    }

    @Test
    void shouldReturnErrorWhenFetchingProvidersFailsWithUnexpectedError() throws Exception {
        doThrow(new RuntimeException("Unexpected error")).when(providerHelper).populateProviders(any(), any());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(content().string(containsString("Something went wrong. The error has been logged")));
    }

    @Test
    void shouldReturnErrorWhenProviderMissing() throws Exception {
        MockMultipartFile file = new MockMultipartFile("fileUpload", "test.pdf", "application/pdf", "test".getBytes());
        mockMvc.perform(multipart("/upload").file(file))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/upload"))
                .andExpect(content().string(containsString("Please select a provider")));
    }

    @Test
    void shouldReturnErrorWhenFileIsEmpty() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("fileUpload", "empty.txt", "text/plain", new byte[0]);
        mockMvc.perform(multipart("/upload").file(emptyFile).param("provider", "TestProvider"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/upload"))
                .andExpect(content().string(containsString("Please select a file to upload")));
    }

    @Test
    void shouldHandleExceptionDuringUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile("fileUpload", "test.pdf", "application/pdf", "test".getBytes());
        when(virusCheckService.checkVirus(any())).thenThrow(new RuntimeException("Virus check failed"));
        mockMvc.perform(multipart("/upload").file(file).param("provider", "TestProvider"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/upload"))
                .andExpect(content().string(containsString("An error occurred while uploading the file")));
    }

    @Test
    void shouldUploadFileSuccessfully() throws Exception {
        when(virusCheckService.checkVirus(any())).thenReturn(null);
        when(principal.getName()).thenReturn("TestUser");
        CwaUploadResponseDto response = new CwaUploadResponseDto();
        response.setFileId("file123");
        when(cwaUploadService.uploadFile(any(), any(), any())).thenReturn(response);
        MockMultipartFile file = new MockMultipartFile("fileUpload", "test.csv", "text/csv", "test".getBytes());
        mockMvc.perform(multipart("/upload").file(file).param("provider", "TestProvider").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/submission"));
    }
}