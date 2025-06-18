package uk.gov.justice.laa.cwa.bulkupload.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa.cwa.bulkupload.helper.ProviderHelper;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadErrorResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadSummaryResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.service.CwaUploadService;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(SearchController.class)
@AutoConfigureMockMvc(addFilters = false)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CwaUploadService cwaUploadService;

    @MockitoBean
    private ProviderHelper providerHelper;

    @Mock
    private Principal principal;

    @Test
    void shouldReturnErrorWhenProviderMissing() throws Exception {
        doNothing().when(providerHelper).populateProviders(any(), any());
        when(principal.getName()).thenReturn("TestUser");
        mockMvc.perform(post("/search").param("searchTerm", "file123").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/upload"))
                .andExpect(model().attribute("error", "Please select a provider"));
    }

    @Test
    void shouldReturnErrorWhenSearchTermMissing() throws Exception {
        doNothing().when(providerHelper).populateProviders(any(), any());
        when(principal.getName()).thenReturn("TestUser");
        mockMvc.perform(post("/search").param("provider", "TestProvider").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/upload"))
                .andExpect(model().attribute("error", "File reference must be between 1 to 10 characters long"));
    }

    @Test
    void shouldReturnSubmissionResultsOnSuccess() throws Exception {
        List<CwaUploadSummaryResponseDto> summary = Collections.emptyList();
        List<CwaUploadErrorResponseDto> errors = Collections.emptyList();
        when(principal.getName()).thenReturn("TestUser");
        when(cwaUploadService.getUploadSummary("file123", "TestUser", "TestProvider")).thenReturn(summary);
        when(cwaUploadService.getUploadErrors("file123", "TESTUSER", "TestProvider")).thenReturn(errors);

        mockMvc.perform(post("/search")
                        .param("provider", "TestProvider")
                        .param("searchTerm", "file123")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/submission-results"))
                .andExpect(model().attribute("summary", summary))
                .andExpect(model().attribute("errors", errors));
    }

    @Test
    void shouldReturnErrorWhenSearchTermIsEmpty() throws Exception {
        doNothing().when(providerHelper).populateProviders(any(), any());
        when(principal.getName()).thenReturn("TestUser");
        mockMvc.perform(post("/search").param("provider", "TestProvider").param("searchTerm", "").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/upload"))
                .andExpect(model().attribute("error", "File reference must be between 1 to 10 characters long"));
    }

    @Test
    void shouldReturnErrorWhenSearchTermIsTooLong() throws Exception {
        doNothing().when(providerHelper).populateProviders(any(), any());
        when(principal.getName()).thenReturn("TestUser");
        mockMvc.perform(post("/search").param("provider", "TestProvider").param("searchTerm", "12345678901").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/upload"))
                .andExpect(model().attribute("error", "File reference must be between 1 to 10 characters long"));
    }

    @Test
    void shouldReturnFailureViewWhenGetUploadSummaryThrows() throws Exception {
        when(principal.getName()).thenReturn("TestUser");
        when(cwaUploadService.getUploadSummary(any(), any(), any())).thenThrow(new RuntimeException("summary error"));
        mockMvc.perform(post("/search").param("provider", "TestProvider").param("searchTerm", "file123").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/upload"));
    }

    @Test
    void shouldReturnFailureViewWhenGetUploadErrorsThrows() throws Exception {
        when(principal.getName()).thenReturn("TestUser");
        when(cwaUploadService.getUploadSummary(any(), any(), any())).thenReturn(Collections.emptyList());
        when(cwaUploadService.getUploadErrors(any(), any(), any())).thenThrow(new RuntimeException("errors error"));
        mockMvc.perform(post("/search").param("provider", "TestProvider").param("searchTerm", "file123").principal(principal))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/upload"));
    }
}