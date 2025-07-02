package uk.gov.justice.laa.cwa.bulkupload.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaSubmissionResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadErrorResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadSummaryResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.service.CwaUploadService;

@WebMvcTest(SubmissionController.class)
@ExtendWith(MockitoExtension.class)
class SubmissionControllerTest {
  private static final String FILE_ID = "file123";
  private static final String PROVIDER = "provider1";
  private static final String TEST_USER = "test@example.com";
  private static final int DEFAULT_TIMEOUT = 30; // Default timeout in seconds

  @Autowired private MockMvc mockMvc;

  @Autowired private SubmissionController submissionController;

  @MockitoBean private CwaUploadService cwaUploadService;

  @BeforeEach
  void setUp() {
    // Reset the timeout to a reasonable value before each test
    ReflectionTestUtils.setField(submissionController, "cwaApiTimeout", DEFAULT_TIMEOUT);
  }

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
  void shouldReturnResultsViewOnSuccessfulSubmission() throws Exception {
    CwaSubmissionResponseDto validateResponse = new CwaSubmissionResponseDto();
    validateResponse.setStatus("success");
    validateResponse.setMessage("OK");
    List<CwaUploadSummaryResponseDto> summary = Collections.emptyList();

    when(cwaUploadService.processSubmission(FILE_ID, TEST_USER, PROVIDER))
        .thenReturn(validateResponse);
    when(cwaUploadService.getUploadSummary(FILE_ID, TEST_USER, PROVIDER)).thenReturn(summary);

    mockMvc
        .perform(
            post("/submit")
                .param("fileId", FILE_ID)
                .param("provider", PROVIDER)
                .param("username", TEST_USER)
                .with(csrf())
                .with(oidcLogin().oidcUser(getOidcUser())))
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

    when(cwaUploadService.processSubmission(FILE_ID, TEST_USER, PROVIDER))
        .thenReturn(validateResponse);
    when(cwaUploadService.getUploadSummary(FILE_ID, TEST_USER, PROVIDER)).thenReturn(summary);
    when(cwaUploadService.getUploadErrors(FILE_ID, TEST_USER, PROVIDER)).thenReturn(errors);

    mockMvc
        .perform(
            post("/submit")
                .param("fileId", FILE_ID)
                .param("provider", PROVIDER)
                .with(csrf())
                .with(oidcLogin().oidcUser(getOidcUser())))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/submission-results"))
        .andExpect(model().attribute("errors", errors));
  }

  @Test
  void shouldReturnFailedViewOnOtherException() throws Exception {
    when(cwaUploadService.processSubmission(FILE_ID, TEST_USER, PROVIDER))
        .thenThrow(new RuntimeException("Unexpected error"));

    mockMvc
        .perform(
            post("/submit")
                .param("fileId", FILE_ID)
                .param("provider", PROVIDER)
                .with(csrf())
                .with(oidcLogin().oidcUser(getOidcUser())))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/submission-failure"));
  }

  @Test
  void shouldReturnTimeoutViewOnSubmissionTimeout() throws Exception {
    when(cwaUploadService.processSubmission(FILE_ID, TEST_USER, PROVIDER))
        .thenAnswer(
            invocation -> {
              Thread.sleep(2000); // Simulate delay
              return new CwaSubmissionResponseDto();
            });

    // Set a very short timeout for the test using the autowired controller
    ReflectionTestUtils.setField(submissionController, "cwaApiTimeout", 0); // 0 seconds

    mockMvc
        .perform(
            post("/submit")
                .param("fileId", FILE_ID)
                .param("provider", PROVIDER)
                .with(csrf())
                .with(oidcLogin().oidcUser(getOidcUser())))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/submission-timeout"));
  }

  @Test
  void shouldReturnFailureViewWhenGetUploadSummaryThrows() throws Exception {
    CwaSubmissionResponseDto validateResponse = new CwaSubmissionResponseDto();
    validateResponse.setStatus("success");
    when(cwaUploadService.processSubmission(FILE_ID, TEST_USER, PROVIDER))
        .thenReturn(validateResponse);
    when(cwaUploadService.getUploadSummary(FILE_ID, TEST_USER, PROVIDER))
        .thenThrow(new RuntimeException("summary error"));

    mockMvc
        .perform(
            post("/submit")
                .param("fileId", FILE_ID)
                .param("provider", PROVIDER)
                .with(csrf())
                .with(oidcLogin().oidcUser(getOidcUser())))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/submission-failure"));
  }

  @Test
  void shouldReturnFailureViewWhenGetUploadErrorsThrows() throws Exception {
    CwaSubmissionResponseDto validateResponse = new CwaSubmissionResponseDto();
    validateResponse.setStatus("failure");
    when(cwaUploadService.processSubmission(FILE_ID, TEST_USER, PROVIDER))
        .thenReturn(validateResponse);
    when(cwaUploadService.getUploadSummary(FILE_ID, TEST_USER, PROVIDER))
        .thenReturn(Collections.emptyList());
    when(cwaUploadService.getUploadErrors(FILE_ID, TEST_USER, PROVIDER))
        .thenThrow(new RuntimeException("errors error"));

    mockMvc
        .perform(
            post("/submit")
                .param("fileId", FILE_ID)
                .param("provider", PROVIDER)
                .with(csrf())
                .with(oidcLogin().oidcUser(getOidcUser())))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/submission-failure"));
  }

  @Test
  void shouldReturnFailureViewWhenSubmissionResponseIsNull() throws Exception {
    when(cwaUploadService.processSubmission(FILE_ID, TEST_USER, PROVIDER)).thenReturn(null);
    when(cwaUploadService.getUploadSummary(FILE_ID, TEST_USER, PROVIDER))
        .thenReturn(Collections.emptyList());
    when(cwaUploadService.getUploadErrors(FILE_ID, TEST_USER, PROVIDER))
        .thenReturn(Collections.emptyList());

    mockMvc
        .perform(
            post("/submit")
                .param("fileId", FILE_ID)
                .param("provider", PROVIDER)
                .with(csrf())
                .with(oidcLogin().oidcUser(getOidcUser())))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/submission-results"));
  }
}
