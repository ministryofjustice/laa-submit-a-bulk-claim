package uk.gov.justice.laa.bulkclaim.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.ui.Model;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionsSearchForm;
import uk.gov.justice.laa.bulkclaim.helper.ProviderHelper;
import uk.gov.justice.laa.bulkclaim.response.CwaUploadErrorResponseDto;
import uk.gov.justice.laa.bulkclaim.response.CwaUploadSummaryResponseDto;
import uk.gov.justice.laa.bulkclaim.service.CwaUploadService;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.claims.model.SubmissionFields;
import uk.gov.justice.laa.claims.model.SubmissionsResultSet;

@AutoConfigureMockMvc(addFilters = false)
class SearchControllerTest {

  private static final String PROVIDER = "1";
  private static final String SEARCH_TERM = "ref";
  private static final String TEST_USER = "TESTUSER";

  @Mock private CwaUploadService cwaUploadService;
  @Mock private ProviderHelper providerHelper;
  @Mock private Model model;
  @Mock private Principal principal;
  @Mock private DataClaimsRestService claimsRestService;

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
  @Disabled(value = "Disabled until switched to using ClaimsAPI")
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

  @Test
  @DisplayName("Search form should return submissions when parameters are valid.")
  void submissionsSearchShouldReturnSubmissionResults_whenNoErrors() {
    List<SubmissionFields> submissions = Collections.emptyList();
    String submissionId = "1234";

    SubmissionsResultSet response = new SubmissionsResultSet();
    response.content(submissions);
    when(claimsRestService.search(eq(List.of("1")), eq(submissionId), isNull(), isNull()))
        .thenReturn(Mono.just(response));

    String view =
        searchController.handleSearch(
            new SubmissionsSearchForm(submissionId, null, null), model, getDefaultOidcUser());

    verify(model).addAttribute("submissionId", submissionId);
    verify(model).addAttribute("submittedDateFrom", "");
    verify(model).addAttribute("submittedDateTo", "");
    verify(model).addAttribute(eq("submissions"), eq(submissions));
    assertEquals("pages/submissions-search-results", view);
  }
}
