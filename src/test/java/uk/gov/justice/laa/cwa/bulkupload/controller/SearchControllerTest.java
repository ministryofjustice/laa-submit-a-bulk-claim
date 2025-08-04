package uk.gov.justice.laa.cwa.bulkupload.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doNothing;
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
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.ui.Model;
import uk.gov.justice.laa.cwa.bulkupload.helper.ProviderHelper;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadErrorResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadSummaryResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.service.CwaUploadService;

@AutoConfigureMockMvc(addFilters = false)
class SearchControllerTest {

  private static final String PROVIDER = "1";
  private static final String SEARCH_TERM = "ref";
  private static final String TEST_USER = "TESTUSER";

  @Mock private CwaUploadService cwaUploadService;
  @Mock private ProviderHelper providerHelper;
  @Mock private Model model;
  @Mock private Principal principal;

  @InjectMocks private SearchController searchController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(principal.getName()).thenReturn(TEST_USER);
    doNothing().when(providerHelper).populateProviders(any(Model.class), eq(TEST_USER));
    when(cwaUploadService.getProviders(TEST_USER)).thenReturn(Collections.emptyList());
    when(model.asMap()).thenReturn(Collections.emptyMap());
  }

  @Test
  void submitForm_shouldReturnError_whenProviderIsMissing() {

    String view = searchController.submitForm("", SEARCH_TERM, model, getDefaultOidcUser());

    verify(model)
        .addAttribute(
            eq("errors"), argThat(errors -> ((Map<?, ?>) errors).containsKey("provider")));
    assertEquals("pages/upload", view);
  }

  private static DefaultOidcUser getDefaultOidcUser() {
    OidcUserInfo userInfo = OidcUserInfo.builder().email(TEST_USER).build();
    OidcIdToken token = OidcIdToken.withTokenValue("token").claim("email", TEST_USER).build();
    return new DefaultOidcUser(Collections.emptyList(), token, userInfo, "email");
  }

  @Test
  void submitForm_shouldReturnError_whenSearchTermIsInvalid() {
    String view = searchController.submitForm(PROVIDER, "", model, getDefaultOidcUser());

    verify(model)
        .addAttribute(
            eq("errors"), argThat(errors -> ((Map<?, ?>) errors).containsKey("searchTerm")));
    assertEquals("pages/upload", view);
  }

  @Test
  void submitForm_shouldReturnError_whenServiceThrowsException() {
    when(cwaUploadService.getUploadSummary(SEARCH_TERM, TEST_USER, PROVIDER))
        .thenThrow(new RuntimeException("fail"));

    String view = searchController.submitForm(PROVIDER, SEARCH_TERM, model, getDefaultOidcUser());

    verify(model)
        .addAttribute(eq("errors"), argThat(errors -> ((Map<?, ?>) errors).containsKey("search")));
    assertEquals("pages/upload", view);
  }

  @Test
  void submitForm_shouldReturnSubmissionResults_whenNoErrors() {
    List<CwaUploadSummaryResponseDto> summary = Collections.emptyList();
    List<CwaUploadErrorResponseDto> uploadErrors = Collections.emptyList();
    when(cwaUploadService.getUploadSummary(SEARCH_TERM, TEST_USER, PROVIDER)).thenReturn(summary);
    when(cwaUploadService.getUploadErrors(SEARCH_TERM, TEST_USER, PROVIDER))
        .thenReturn(uploadErrors);

    String view = searchController.submitForm(PROVIDER, SEARCH_TERM, model, getDefaultOidcUser());

    verify(model).addAttribute("summary", summary);
    verify(model).addAttribute("errors", uploadErrors);
    assertEquals("pages/submission-results", view);
  }
}
