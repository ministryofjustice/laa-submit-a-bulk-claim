package uk.gov.justice.laa.bulkclaim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.CLAIM_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

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
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.SubmissionClaimDetails;
import uk.gov.justice.laa.bulkclaim.helper.TestObjectCreator;
import uk.gov.justice.laa.bulkclaim.mapper.SubmissionClaimDetailsMapper;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;

@WebMvcTest(ClaimDetailController.class)
@AutoConfigureMockMvc
@Import(WebMvcTestConfig.class)
@DisplayName("Claim detail controller test")
class ClaimDetailControllerTest {

  @Autowired private MockMvcTester mockMvc;

  @MockitoBean private DataClaimsRestClient dataClaimsRestClient;
  @Autowired @MockitoBean private SubmissionClaimDetailsMapper submissionClaimDetailsMapper;

  @Nested
  @DisplayName("GET: /submission/claim/{claimReference}")
  class GetClaimReference {

    @Test
    @DisplayName("Should expect redirect")
    void shouldExpectRedirect() {
      // Given
      UUID claimId = UUID.fromString("244fcb9f-50ab-4af8-b635-76bd30e0e97d");
      // When / Then
      assertThat(
              mockMvc.perform(
                  get("/submission/claim/" + claimId)
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))))
          .hasStatus3xxRedirection()
          .hasRedirectedUrl("/view-claim-detail");
    }
  }

  @Nested
  @DisplayName("GET: /view-claim-detail")
  class GetClaimDetail {

    @Test
    @DisplayName("Should return expected result")
    void shouldReturnExpectedResult() {
      // Given
      UUID claimId = UUID.fromString("244fcb9f-50ab-4af8-b635-76bd30e0e97d");
      UUID submissionId = UUID.fromString("244fcb9f-50ab-4af8-b635-76bd30e0e97d");
      ClaimResponse claimResponse = TestObjectCreator.buildClaimResponse();
      when(dataClaimsRestClient.getSubmissionClaim(submissionId, claimId))
          .thenReturn(Mono.just(claimResponse));
      SubmissionClaimDetails submissionClaimDetails = TestObjectCreator.buildClaimDetails();
      when(submissionClaimDetailsMapper.toSubmissionClaimDetails(claimResponse))
          .thenReturn(submissionClaimDetails);

      // When / Then
      assertThat(
              mockMvc.perform(
                  get("/view-claim-detail")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(SUBMISSION_ID, submissionId)
                      .sessionAttr(CLAIM_ID, claimId)))
          .hasStatusOk()
          .hasViewName("pages/view-claim-detail");
    }

    @Test
    @DisplayName("Should throw exception when submissionId is missing")
    void shouldThrowExceptionWhenSubmissionIdIsMissing() {
      // Given
      UUID claimId = UUID.fromString("244fcb9f-50ab-4af8-b635-76bd30e0e97d");

      // When / Then
      assertThat(
              mockMvc.perform(
                  get("/view-claim-detail")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(CLAIM_ID, claimId)))
          .failure()
          .hasMessageContaining("Expected session attribute 'submissionId'");
    }

    @Test
    @DisplayName("Should throw exception when claimId is missing")
    void shouldThrowExceptionWhenClaimIdIsMissing() {
      // Given
      UUID submissionId = UUID.fromString("244fcb9f-50ab-4af8-b635-76bd30e0e97d");

      // When / Then
      assertThat(
              mockMvc.perform(
                  get("/view-claim-detail")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(SUBMISSION_ID, submissionId)))
          .failure()
          .hasMessageContaining("Expected session attribute 'claimId'");
    }

    @Test
    @DisplayName("Should throw exception when claim was not found")
    void shouldThrowExceptionWhenClaimWasNotFound() {
      // Given
      UUID claimId = UUID.fromString("59930faa-3f38-4ee1-b5bd-08dce5a4fdbc");
      UUID submissionId = UUID.fromString("244fcb9f-50ab-4af8-b635-76bd30e0e97d");
      ClaimResponse claimResponse = TestObjectCreator.buildClaimResponse();
      when(dataClaimsRestClient.getSubmissionClaim(submissionId, claimId)).thenReturn(Mono.empty());

      // When / Then
      assertThat(
              mockMvc.perform(
                  get("/view-claim-detail")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(SUBMISSION_ID, submissionId)
                      .sessionAttr(CLAIM_ID, claimId)))
          .failure()
          .hasMessageEndingWith(
              "Claim 59930faa-3f38-4ee1-b5bd-08dce5a4fdbc does not exist for submission 244fcb9f-50ab-4af8-b635-76bd30e0e97d");
    }
  }
}
