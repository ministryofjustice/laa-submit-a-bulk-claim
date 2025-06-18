package uk.gov.justice.laa.cwa.bulkupload.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaSubmissionResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadErrorResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadSummaryResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.service.CwaUploadService;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(SubmissionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SubmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CwaUploadService cwaUploadService;

    @Mock
    private Principal principal;

    @Test
    void shouldReturnResultsViewOnSuccessfulSubmission() throws Exception {
        CwaSubmissionResponseDto validateResponse = new CwaSubmissionResponseDto();
        validateResponse.setStatus("success");
        validateResponse.setMessage("OK");
        List<CwaUploadSummaryResponseDto> summary = Collections.emptyList();

        when(cwaUploadService.processSubmission(eq("file123"), any(), eq("provider1"))).thenReturn(validateResponse);
        when(cwaUploadService.getUploadSummary(eq("file123"), any(), eq("provider1"))).thenReturn(summary);
        when(principal.getName()).thenReturn("TestUser");
        mockMvc.perform(post("/submit").param("fileId", "file123").param("provider", "provider1").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/submission-results"))
                .andExpect(model().attribute("summary", summary));
    }

    @Test
    void shouldReturnResultsViewWithErrorsOnValidationFailure() throws Exception {
        CwaSubmissionResponseDto validateResponse = new CwaSubmissionResponseDto();
        validateResponse.setStatus("failure");
        validateResponse.setMessage("Validation failed");
        List<CwaUploadSummaryResponseDto> summary = Collections.emptyList();
        List<CwaUploadErrorResponseDto> errors = List.of(new CwaUploadErrorResponseDto());

        when(cwaUploadService.processSubmission(eq("file123"), any(), eq("provider1"))).thenReturn(validateResponse);
        when(cwaUploadService.getUploadSummary(eq("file123"), any(), eq("provider1"))).thenReturn(summary);
        when(cwaUploadService.getUploadErrors(eq("file123"), any(), eq("provider1"))).thenReturn(errors);
        when(principal.getName()).thenReturn("TestUser");
        mockMvc.perform(post("/submit").param("fileId", "file123").param("provider", "provider1").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/submission-results"))
                .andExpect(model().attribute("errors", errors));
    }

    @Test
    void shouldReturnFailedViewOnOtherException() throws Exception {
        when(cwaUploadService.processSubmission(eq("file123"), any(), eq("provider1")))
                .thenThrow(new RuntimeException("Unexpected error"));
        when(principal.getName()).thenReturn("TestUser");

        mockMvc.perform(post("/submit").param("fileId", "file123").param("provider", "provider1").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/submission-failure"));
    }

    @Test
    void shouldReturnTimeoutViewOnSubmissionTimeout() throws Exception {
        when(cwaUploadService.processSubmission(eq("file123"), any(), eq("provider1")))
                .thenAnswer(invocation -> {
                    Thread.sleep(2000); // Simulate delay
                    return new CwaSubmissionResponseDto();
                });
        when(principal.getName()).thenReturn("TestUser");

        // Set a very short timeout for the test
        SubmissionController controller = new SubmissionController(cwaUploadService);
        ReflectionTestUtils.setField(controller, "cwaApiTimeout", 0); // 0 seconds

        mockMvc.perform(post("/submit").param("fileId", "file123").param("provider", "provider1").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/submission-timeout"));
    }

    @Test
    void shouldReturnFailureViewWhenGetUploadSummaryThrows() throws Exception {
        CwaSubmissionResponseDto validateResponse = new CwaSubmissionResponseDto();
        validateResponse.setStatus("success");
        when(cwaUploadService.processSubmission(eq("file123"), any(), eq("provider1"))).thenReturn(validateResponse);
        when(cwaUploadService.getUploadSummary(eq("file123"), any(), eq("provider1"))).thenThrow(new RuntimeException("summary error"));
        when(principal.getName()).thenReturn("TestUser");

        mockMvc.perform(post("/submit").param("fileId", "file123").param("provider", "provider1").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/submission-failure"));
    }

    @Test
    void shouldReturnFailureViewWhenGetUploadErrorsThrows() throws Exception {
        CwaSubmissionResponseDto validateResponse = new CwaSubmissionResponseDto();
        validateResponse.setStatus("failure");
        when(cwaUploadService.processSubmission(eq("file123"), any(), eq("provider1"))).thenReturn(validateResponse);
        when(cwaUploadService.getUploadSummary(eq("file123"), any(), eq("provider1"))).thenReturn(Collections.emptyList());
        when(cwaUploadService.getUploadErrors(eq("file123"), any(), eq("provider1"))).thenThrow(new RuntimeException("errors error"));
        when(principal.getName()).thenReturn("TestUser");

        mockMvc.perform(post("/submit").param("fileId", "file123").param("provider", "provider1").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/submission-failure"));
    }

    @Test
    void shouldReturnFailureViewWhenSubmissionResponseIsNull() throws Exception {
        when(cwaUploadService.processSubmission(eq("file123"), any(), eq("provider1"))).thenReturn(null);
        when(cwaUploadService.getUploadSummary(eq("file123"), any(), eq("provider1"))).thenReturn(Collections.emptyList());
        when(cwaUploadService.getUploadErrors(eq("file123"), any(), eq("provider1"))).thenReturn(Collections.emptyList());
        when(principal.getName()).thenReturn("TestUser");

        mockMvc.perform(post("/submit").param("fileId", "file123").param("provider", "provider1").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/submission-results"));
    }
}