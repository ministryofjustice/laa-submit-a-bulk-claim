package uk.gov.justice.laa.bulkclaim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.bulkclaim.service.ClaimsRestService;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;
import uk.gov.justice.laa.claims.model.GetSubmission200ResponseClaimsInner;

@WebMvcTest(ImportInProgressController.class)
@AutoConfigureMockMvc
@Import(WebMvcTestConfig.class)
public class ImportInProgressControllerTest {

  @Autowired private MockMvcTester mockMvc;

  @MockitoBean private ClaimsRestService claimsRestService;

  @Nested
  @DisplayName("GET: /import-in-progress")
  class ImportInProgressTests {

    @Test
    @DisplayName("Should return expected result when single claim not ready")
    void shouldReturnExpectedResult() {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(claimsRestService.getSubmission(submissionId))
          .thenReturn(
              Mono.just(
                  GetSubmission200Response.builder()
                      .claims(
                          Collections.singletonList(
                              GetSubmission200ResponseClaimsInner.builder()
                                  .status("SUBMITTED")
                                  .build()))
                      .build()));

      assertThat(
              mockMvc.perform(
                  get("/import-in-progress")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr("bulkSubmissionId", submissionId.toString())))
          .hasStatusOk()
          .hasViewName("pages/upload-in-progress");
    }

    @Test
    @DisplayName("Should return expected result when multiple claims not ready")
    void shouldReturnExpectedResultWhenMultipleClaimsNotReady() {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(claimsRestService.getSubmission(submissionId))
          .thenReturn(
              Mono.just(
                  GetSubmission200Response.builder()
                      .claims(
                          Arrays.asList(
                              GetSubmission200ResponseClaimsInner.builder()
                                  .status("SUBMITTED")
                                  .build(),
                              GetSubmission200ResponseClaimsInner.builder()
                                  .status("SUBMITTED")
                                  .build()))
                      .build()));

      assertThat(
              mockMvc.perform(
                  get("/import-in-progress")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr("bulkSubmissionId", submissionId.toString())))
          .hasStatusOk()
          .hasViewName("pages/upload-in-progress");
    }

    @Test
    @DisplayName("Should return expected result when partial claims not ready")
    void shouldReturnExpectedResultWhenPartialClaimsNotReady() {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(claimsRestService.getSubmission(submissionId))
          .thenReturn(
              Mono.just(
                  GetSubmission200Response.builder()
                      .claims(
                          Arrays.asList(
                              GetSubmission200ResponseClaimsInner.builder()
                                  .status("IMPORTED")
                                  .build(),
                              GetSubmission200ResponseClaimsInner.builder()
                                  .status("SUBMITTED")
                                  .build()))
                      .build()));

      assertThat(
              mockMvc.perform(
                  get("/import-in-progress")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr("bulkSubmissionId", submissionId.toString())))
          .hasStatusOk()
          .hasViewName("pages/upload-in-progress");
    }

    @Test
    @DisplayName("Should redirect when only claim has imported")
    void shouldRedirectWhenOnlyClaimHasImported() {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(claimsRestService.getSubmission(submissionId))
          .thenReturn(
              Mono.just(
                  GetSubmission200Response.builder()
                      .claims(
                          Collections.singletonList(
                              GetSubmission200ResponseClaimsInner.builder()
                                  .status("READY")
                                  .build()))
                      .build()));

      assertThat(
              mockMvc.perform(
                  get("/import-in-progress")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr("bulkSubmissionId", submissionId.toString())))
          .hasStatus3xxRedirection()
          // TODO: Redirect to imported page CCMSPUI-788
          .hasRedirectedUrl("/");
    }

    @Test
    @DisplayName("Should redirect when multiple claims has imported")
    void shouldRedirectWhenMultipleClaimsHasImported() {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(claimsRestService.getSubmission(submissionId))
          .thenReturn(
              Mono.just(
                  GetSubmission200Response.builder()
                      .claims(
                          Arrays.asList(
                              GetSubmission200ResponseClaimsInner.builder().status("READY").build(),
                              GetSubmission200ResponseClaimsInner.builder()
                                  .status("READY")
                                  .build()))
                      .build()));

      assertThat(
              mockMvc.perform(
                  get("/import-in-progress")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr("bulkSubmissionId", submissionId.toString())))
          .hasStatus3xxRedirection()
          // TODO: Redirect to imported page CCMSPUI-788
          .hasRedirectedUrl("/");
    }

    @Test
    @DisplayName("Should throw error when submission has no claims")
    void shouldThrowErrorWhenSubmissionHasNoClaims() {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(claimsRestService.getSubmission(submissionId))
          .thenReturn(
              Mono.just(
                  GetSubmission200Response.builder().claims(Collections.emptyList()).build()));

      assertThat(
              mockMvc.perform(
                  get("/import-in-progress")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr("bulkSubmissionId", submissionId.toString())))
          .failure()
          .hasCauseInstanceOf(SubmitBulkClaimException.class)
          .hasMessageContaining("No claims found for bulk submission: " + submissionId);
    }

    @Test
    @DisplayName("Should throw error when submission claims is null")
    void shouldThrowErrorWhenSubmissionClaimsIsNull() {
      // Given
      UUID submissionId = UUID.fromString("5933fc67-bac7-4f48-81ed-61c8c463f054");
      when(claimsRestService.getSubmission(submissionId))
          .thenReturn(Mono.just(GetSubmission200Response.builder().claims(null).build()));

      assertThat(
              mockMvc.perform(
                  get("/import-in-progress")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr("bulkSubmissionId", submissionId.toString())))
          .failure()
          .hasCauseInstanceOf(SubmitBulkClaimException.class)
          .hasMessageContaining("No claims found for bulk submission: " + submissionId);
    }
  }
}
