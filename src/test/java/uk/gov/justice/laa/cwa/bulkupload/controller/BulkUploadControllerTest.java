package uk.gov.justice.laa.cwa.bulkupload.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa.cwa.bulkupload.response.UploadResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.service.TokenService;
import uk.gov.justice.laa.cwa.bulkupload.service.VirusCheckService;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
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
    private TokenService tokenService;  // Added TokenService mock

    @Test
    void shouldReturnUploadPage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/upload"))
                .andExpect(content().string(containsString("Select a file to upload")));
    }

    @Test
    void shouldUploadFile() throws Exception {

        MockMultipartFile uploadFile = new MockMultipartFile("fileUpload", "test.pdf", "text/plain", "test".getBytes());

        when(virusCheckService.checkVirus(any()))
                .thenReturn(new UploadResponseDto());

        mockMvc.perform(multipart("/upload")
                        .file(uploadFile))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/upload-success"));
    }

    @Test
    void shouldRejectEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "fileUpload",
                "empty.txt",
                "text/plain",
                new byte[0]
        );

        mockMvc.perform(multipart("/upload")
                        .file(emptyFile))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/upload-failure"));
    }
}