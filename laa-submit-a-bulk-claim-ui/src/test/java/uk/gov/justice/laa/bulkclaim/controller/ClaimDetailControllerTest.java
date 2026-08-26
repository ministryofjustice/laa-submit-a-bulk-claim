package uk.gov.justice.laa.bulkclaim.controller;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper.OIDC_USER;

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
import uk.gov.justice.laa.bulkclaim.builder.ClaimStatusBannerBuilder;
import uk.gov.justice.laa.bulkclaim.builder.LatestAssessmentResolver;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMessagesBuilder;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.ClaimFeeCalculationBreakdown;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.ClaimSummary;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.CrimeLowerClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessageRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.bulkclaim.helper.TestObjectCreator;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimFeeCalculationBreakdownMapper;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimSummaryMapper;
import uk.gov.justice.laa.bulkclaim.service.ClaimService;
import uk.gov.justice.laa.bulkclaim.service.SubmissionService;
import uk.gov.justice.laa.bulkclaim.util.ThymeleafHrefUtils;
import uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails.ClaimDetailPageData;
import uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails.ClaimDetailViewFactory;
import uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails.CrimeClaimDetailsView;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@WebMvcTest(ClaimDetailController.class)
@AutoConfigureMockMvc
@Import({ThymeleafHrefUtils.class})
@DisplayName("Claim detail controller test")
class ClaimDetailControllerTest extends BaseControllerTest {

  @Autowired private MockMvcTester mockMvc;

  @MockitoBean private ClaimSummaryMapper claimSummaryMapper;
  @MockitoBean private ClaimFeeCalculationBreakdownMapper claimFeeCalculationBreakdownMapper;
  @MockitoBean private SubmissionMessagesBuilder submissionMessagesBuilder;
  @MockitoBean private ClaimService claimService;
  @MockitoBean private ClaimDetailViewFactory claimDetailViewFactory;
  @MockitoBean private ClaimStatusBannerBuilder claimStatusBannerBuilder;
  @MockitoBean private LatestAssessmentResolver latestAssessmentResolver;
  @MockitoBean private SubmissionService submissionService;

  @Nested
  @DisplayName("GET: /submissions/{submissionId}/claims/{claimId}")
  class GetClaimReference {

    @Nested
    @DisplayName(
        "GET: /submissions/{submissionId}/claims/{claimId} (isAlternativeClaimViewEnabled=false)")
    class GetClaimDetail {

      @Test
      @DisplayName("Should return expected result")
      void shouldReturnExpectedResultWithDefaultTab() {
        UUID claimId = UUID.fromString("244fcb9f-50ab-4af8-b635-76bd30e0e97d");
        UUID submissionId = UUID.fromString("244fcb9f-50ab-4af8-b635-76bd30e0e97d");
        SubmissionResponse submissionResponse =
            SubmissionResponse.builder().areaOfLaw(AreaOfLaw.LEGAL_HELP).build();
        when(submissionService.getSubmission(submissionId, OIDC_USER))
            .thenReturn(submissionResponse);

        ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2();
        when(claimService.getClaimV2(submissionId, claimId, OIDC_USER)).thenReturn(claimResponse);

        when(claimSummaryMapper.toClaimSummary(claimResponse, AreaOfLaw.LEGAL_HELP.getValue()))
            .thenReturn(ClaimSummary.builder().build());
        when(claimFeeCalculationBreakdownMapper.toClaimFeeCalculationBreakdown(claimResponse))
            .thenReturn(ClaimFeeCalculationBreakdown.builder().build());

        when(submissionMessagesBuilder.buildAllWarnings(OIDC_USER, submissionId, claimId))
            .thenReturn(
                MessagesSummary.builder()
                    .messages(singletonList(MessageRow.builder().build()))
                    .build());

        assertThat(
                mockMvc.perform(
                    get("/submissions/%s/claims/%s".formatted(submissionId, claimId))
                        .with(oidcLogin().oidcUser(OIDC_USER))))
            .hasStatusOk()
            .hasViewName("pages/view-claim-detail-old");

        verify(claimSummaryMapper, times(1))
            .toClaimSummary(claimResponse, AreaOfLaw.LEGAL_HELP.getValue());
      }
    }

    @Nested
    @DisplayName(
        "GET: /submissions/{submissionId}/claims/{claimId} (isAlternativeClaimViewEnabled=true)")
    class GetClaimDetailV2 {

      private final UUID claimId = UUID.fromString("244fcb9f-50ab-4af8-b635-76bd30e0e97d");
      private final UUID submissionId = UUID.fromString("244fcb9f-50ab-4af8-b635-76bd30e0e97d");

      private void stubCommonDependencies() {
        CrimeLowerClaimDetails details = new CrimeLowerClaimDetails();
        ClaimFieldRow emptyField = new ClaimFieldRow(null, null, null);
        details.setFixedFee(emptyField);
        details.setProfitCosts(emptyField);
        details.setDisbursements(emptyField);
        details.setDisbursementsVat(emptyField);
        details.setVat(emptyField);
        details.setTotalVat(emptyField);
        details.setTotalIncludingVat(emptyField);
        details.setTravelCosts(emptyField);
        details.setWaitingCosts(emptyField);

        when(claimService.getClaimDetailPageData(submissionId, claimId, OIDC_USER))
            .thenReturn(
                new ClaimDetailPageData(
                    AreaOfLaw.CRIME_LOWER, false, new CrimeClaimDetailsView(details), null));
      }

      @Test
      @DisplayName("Should return the template provided by claim service")
      void shouldReturnTemplateFromClaimService() {
        when(featureFlagsConfig.getIsAlternativeClaimViewEnabled()).thenReturn(true);
        stubCommonDependencies();

        assertThat(
                mockMvc.perform(
                    get("/submissions/%s/claims/%s".formatted(submissionId, claimId))
                        .with(oidcLogin().oidcUser(OIDC_USER))))
            .hasStatusOk()
            .hasViewName("pages/view-claim-detail");

        verify(claimService, times(1)).getClaimDetailPageData(submissionId, claimId, OIDC_USER);
      }

      @Test
      @DisplayName("Should include warnings from the submission messages builder")
      void shouldIncludeWarningsFromSubmissionMessagesBuilder() {
        when(featureFlagsConfig.getIsAlternativeClaimViewEnabled()).thenReturn(true);
        stubCommonDependencies();

        when(submissionMessagesBuilder.buildAllWarnings(OIDC_USER, submissionId, claimId))
            .thenReturn(
                MessagesSummary.builder()
                    .messages(singletonList(MessageRow.builder().message("A warning").build()))
                    .build());

        assertThat(
                mockMvc.perform(
                    get("/submissions/%s/claims/%s".formatted(submissionId, claimId))
                        .with(oidcLogin().oidcUser(OIDC_USER))))
            .hasStatusOk()
            .hasViewName("pages/view-claim-detail");
      }
    }
  }
}
