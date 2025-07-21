package uk.gov.justice.laa.cwa.bulkupload.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.ui.Model;
import uk.gov.justice.laa.cwa.bulkupload.helper.ProviderHelper;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadErrorResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadSummaryResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.service.CwaUploadService;

@AutoConfigureMockMvc(addFilters = false)
class SearchControllerTest {

  @Mock private CwaUploadService cwaUploadService;
  @Mock private ProviderHelper providerHelper;
  @Mock private Model model;
  @Mock private Principal principal;

  @InjectMocks private SearchController searchController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(principal.getName()).thenReturn("USER");
  }

  @Test
  void submitForm_shouldReturnError_whenProviderIsMissing() {
    String view = searchController.submitForm("", "ref", model, principal);
    verify(model)
        .addAttribute(
            eq("errors"), argThat(errors -> ((Map<?, ?>) errors).containsKey("provider")));
    assertEquals("pages/upload", view);
  }

  @Test
  void submitForm_shouldReturnError_whenSearchTermIsInvalid() {
    String view = searchController.submitForm("1", "", model, principal);
    verify(model)
        .addAttribute(
            eq("errors"), argThat(errors -> ((Map<?, ?>) errors).containsKey("searchTerm")));
    assertEquals("pages/upload", view);
  }

  @Test
  void submitForm_shouldReturnError_whenServiceThrowsException() {
    when(cwaUploadService.getUploadSummary(anyString(), anyString(), anyString()))
        .thenThrow(new RuntimeException("fail"));
    String view = searchController.submitForm("1", "ref", model, principal);
    verify(model)
        .addAttribute(eq("errors"), argThat(errors -> ((Map<?, ?>) errors).containsKey("search")));
    assertEquals("pages/upload", view);
  }

  @Test
  void submitForm_shouldReturnSubmissionResults_whenNoErrors() {
    List<CwaUploadSummaryResponseDto> summary = Collections.emptyList();
    List<CwaUploadErrorResponseDto> uploadErrors = Collections.emptyList();
    when(cwaUploadService.getUploadSummary(anyString(), anyString(), anyString()))
        .thenReturn(summary);
    when(cwaUploadService.getUploadErrors(anyString(), anyString(), anyString()))
        .thenReturn(uploadErrors);

    String view = searchController.submitForm("1", "ref", model, principal);

    verify(model).addAttribute("summary", summary);
    verify(model).addAttribute("errors", uploadErrors);
    assertEquals("pages/submission-results", view);
  }
}
