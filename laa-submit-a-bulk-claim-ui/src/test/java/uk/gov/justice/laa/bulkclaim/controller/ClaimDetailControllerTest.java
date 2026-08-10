package uk.gov.justice.laa.bulkclaim.controller;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.CLAIM_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.builder.ClaimStatusBannerBuilder;
import uk.gov.justice.laa.bulkclaim.builder.LatestAssessmentResolver;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMessagesBuilder;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClientV2;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.ClaimFeeCalculationBreakdown;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.ClaimSummary;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessageRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.bulkclaim.helper.TestObjectCreator;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimFeeCalculationBreakdownMapper;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimSummaryMapper;
import uk.gov.justice.laa.bulkclaim.service.claimdetail.ClaimDetailView;
import uk.gov.justice.laa.bulkclaim.service.claimdetail.ClaimDetailViewFactory;
import uk.gov.justice.laa.bulkclaim.util.ThymeleafHrefUtils;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.DerivedClaimStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@WebMvcTest(ClaimDetailController.class)
@AutoConfigureMockMvc
@Import({ThymeleafHrefUtils.class})
@DisplayName("Claim detail controller test")
class ClaimDetailControllerTest extends BaseControllerTest {

  @Autowired private MockMvcTester mockMvc;

  @MockitoBean private DataClaimsRestClient dataClaimsRestClient;
  @MockitoBean private DataClaimsRestClientV2 dataClaimsRestClientV2;
  @MockitoBean private ClaimSummaryMapper claimSummaryMapper;
  @MockitoBean private ClaimFeeCalculationBreakdownMapper claimFeeCalculationBreakdownMapper;
  @MockitoBean private SubmissionMessagesBuilder submissionMessagesBuilder;
  @MockitoBean private ClaimDetailViewFactory claimDetailViewFactory;
  @MockitoBean private ClaimStatusBannerBuilder claimStatusBannerBuilder;
  @MockitoBean private LatestAssessmentResolver latestAssessmentResolver;

  @Nested
  @DisplayName("GET: /submission/claim/{claimReference}")
  class GetClaimReference {

    @Test
    @DisplayName("Should expect redirect")
    void shouldExpectRedirect() {
      UUID claimId = UUID.fromString("244fcb9f-50ab-4af8-b635-76bd30e0e97d");

      assertThat(
              mockMvc.perform(
                  get("/submission/claim/" + claimId)
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))))
          .hasStatus3xxRedirection()
          .hasRedirectedUrl("/view-claim-detail-old?page=0&messagesPage=0&navTab=CLAIM_DETAILS");
    }
  }

  @Nested
  @DisplayName("GET: /view-claim-detail-old")
  class GetClaimDetail {

    @Test
    @DisplayName("Should return expected result")
    void shouldReturnExpectedResultWithDefaultTab() {
      UUID claimId = UUID.fromString("244fcb9f-50ab-4af8-b635-76bd30e0e97d");
      UUID submissionId = UUID.fromString("244fcb9f-50ab-4af8-b635-76bd30e0e97d");
      SubmissionResponse submissionResponse =
          SubmissionResponse.builder().areaOfLaw(AreaOfLaw.LEGAL_HELP).build();
      when(dataClaimsRestClient.getSubmission(submissionId))
          .thenReturn(Mono.just(submissionResponse));

      ClaimResponse claimResponse = TestObjectCreator.buildClaimResponse();
      when(dataClaimsRestClient.getSubmissionClaim(submissionId, claimId))
          .thenReturn(Mono.just(claimResponse));

      when(claimSummaryMapper.toClaimSummary(claimResponse, AreaOfLaw.LEGAL_HELP.getValue()))
          .thenReturn(ClaimSummary.builder().build());
      when(claimFeeCalculationBreakdownMapper.toClaimFeeCalculationBreakdown(claimResponse))
          .thenReturn(ClaimFeeCalculationBreakdown.builder().build());

      when(submissionMessagesBuilder.buildAllWarnings(submissionId, claimId))
          .thenReturn(
              MessagesSummary.builder()
                  .messages(singletonList(MessageRow.builder().build()))
                  .build());

      assertThat(
              mockMvc.perform(
                  get("/view-claim-detail-old")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(SUBMISSION_ID, submissionId)
                      .sessionAttr(CLAIM_ID, claimId)))
          .hasStatusOk()
          .hasViewName("pages/view-claim-detail-old");

      verify(claimSummaryMapper, times(1))
          .toClaimSummary(claimResponse, AreaOfLaw.LEGAL_HELP.getValue());
    }

    @Test
    @DisplayName("Should throw exception when submissionId is missing")
    void shouldThrowExceptionWhenSubmissionIdIsMissing() {
      UUID claimId = UUID.fromString("244fcb9f-50ab-4af8-b635-76bd30e0e97d");

      assertThat(
              mockMvc.perform(
                  get("/view-claim-detail-old")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(CLAIM_ID, claimId)))
          .failure()
          .hasMessageContaining("Expected session attribute 'submissionId'");
    }

    @Test
    @DisplayName("Should throw exception when claimId is missing")
    void shouldThrowExceptionWhenClaimIdIsMissing() {
      UUID submissionId = UUID.fromString("244fcb9f-50ab-4af8-b635-76bd30e0e97d");

      assertThat(
              mockMvc.perform(
                  get("/view-claim-detail-old")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(SUBMISSION_ID, submissionId)))
          .failure()
          .hasMessageContaining("Expected session attribute 'claimId'");
    }

    @Test
    @DisplayName("Should throw exception when claim was not found")
    void shouldThrowExceptionWhenClaimWasNotFound() {
      UUID claimId = UUID.fromString("59930faa-3f38-4ee1-b5bd-08dce5a4fdbc");
      UUID submissionId = UUID.fromString("244fcb9f-50ab-4af8-b635-76bd30e0e97d");

      when(dataClaimsRestClient.getSubmissionClaim(submissionId, claimId)).thenReturn(Mono.empty());

      assertThat(
              mockMvc.perform(
                  get("/view-claim-detail-old")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(SUBMISSION_ID, submissionId)
                      .sessionAttr(CLAIM_ID, claimId)))
          .failure()
          .hasMessageEndingWith(
              "Claim 59930faa-3f38-4ee1-b5bd-08dce5a4fdbc does not exist for submission "
                  + "244fcb9f-50ab-4af8-b635-76bd30e0e97d");
    }
  }

  @Nested
  @DisplayName("GET: /view-claim-detail")
  class GetClaimDetailV2 {

    private final UUID claimId = UUID.fromString("244fcb9f-50ab-4af8-b635-76bd30e0e97d");
    private final UUID submissionId = UUID.fromString("244fcb9f-50ab-4af8-b635-76bd30e0e97d");

    private void stubCommonDependencies() {
      when(dataClaimsRestClient.getClaimHistory(eq(claimId), any()))
          .thenReturn(Mono.just(ClaimHistoryResultSet.builder().events(List.of()).build()));
      when(claimStatusBannerBuilder.build(any(), any())).thenReturn(Optional.empty());
      when(claimDetailViewFactory.build(any(), any()))
          .thenReturn(
              new ClaimDetailView.CrimeLower(
                  CrimeLowerClaimDetails.builder().build(), List.of(), List.of()));
    }

    @Test
    @DisplayName(
        "Should resolve and pass the latest assessment when the claim is AMENDED or ASSESSED")
    void shouldResolveAssessmentWhenCurrentCalculatedIsShown() {
      stubCommonDependencies();
      ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.CRIME_LOWER);
      claimResponse.setDerivedClaimStatus(DerivedClaimStatus.ASSESSED);
      when(dataClaimsRestClientV2.getSubmissionClaim(submissionId, claimId))
          .thenReturn(Mono.just(claimResponse));

      AssessmentGet assessment = new AssessmentGet().fixedFeeAmount(new BigDecimal("50.00"));
      when(latestAssessmentResolver.resolveLatestNonVoid(claimId))
          .thenReturn(Optional.of(assessment));

      assertThat(
              mockMvc.perform(
                  get("/view-claim-detail")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(SUBMISSION_ID, submissionId)
                      .sessionAttr(CLAIM_ID, claimId)))
          .hasStatusOk();

      verify(latestAssessmentResolver, times(1)).resolveLatestNonVoid(claimId);
      verify(claimDetailViewFactory, times(1)).build(claimResponse, assessment);
    }

    @Test
    @DisplayName("Should not resolve an assessment when the claim is neither AMENDED nor ASSESSED")
    void shouldNotResolveAssessmentWhenCurrentCalculatedIsHidden() {
      stubCommonDependencies();
      ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.CRIME_LOWER);
      claimResponse.setDerivedClaimStatus(DerivedClaimStatus.READY_TO_PROCESS);
      when(dataClaimsRestClientV2.getSubmissionClaim(submissionId, claimId))
          .thenReturn(Mono.just(claimResponse));

      assertThat(
              mockMvc.perform(
                  get("/view-claim-detail")
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                      .sessionAttr(SUBMISSION_ID, submissionId)
                      .sessionAttr(CLAIM_ID, claimId)))
          .hasStatusOk();

      verify(latestAssessmentResolver, never()).resolveLatestNonVoid(any());
      verify(claimDetailViewFactory, times(1)).build(claimResponse, null);
    }
  }
}
