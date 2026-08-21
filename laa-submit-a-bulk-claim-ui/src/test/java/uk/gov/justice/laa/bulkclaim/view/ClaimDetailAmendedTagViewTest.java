package uk.gov.justice.laa.bulkclaim.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.CLAIM_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;
import static uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper.OIDC_USER;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.builder.ClaimStatusBannerBuilder;
import uk.gov.justice.laa.bulkclaim.builder.LatestAssessmentResolver;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMessagesBuilder;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClientV2;
import uk.gov.justice.laa.bulkclaim.controller.ClaimDetailController;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.LegalHelpClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.MediationClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimFeeCalculationBreakdownMapper;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimSummaryMapper;
import uk.gov.justice.laa.bulkclaim.service.ClaimService;
import uk.gov.justice.laa.bulkclaim.service.SubmissionService;
import uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails.ClaimDetailPageData;
import uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails.ClaimDetailView;
import uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails.ClaimDetailViewFactory;
import uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails.LegalHelpClaimDetailsView;
import uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails.MediationClaimDetailsView;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryResultSet;

@WebMvcTest(ClaimDetailController.class)
@DisplayName("Claim details amended tags")
class ClaimDetailAmendedTagViewTest extends ViewTestBase {

  @MockitoBean private DataClaimsRestClientV2 dataClaimsRestClientV2;
  @MockitoBean private ClaimSummaryMapper claimSummaryMapper;
  @MockitoBean private ClaimFeeCalculationBreakdownMapper claimFeeCalculationBreakdownMapper;
  @MockitoBean private SubmissionMessagesBuilder submissionMessagesBuilder;
  @MockitoBean private ClaimService claimService;
  @MockitoBean private ClaimDetailViewFactory claimDetailViewFactory;
  @MockitoBean private ClaimStatusBannerBuilder claimStatusBannerBuilder;
  @MockitoBean private LatestAssessmentResolver latestAssessmentResolver;
  @MockitoBean private SubmissionService submissionService;

  ClaimDetailAmendedTagViewTest() {
    this.mapping = "/submissions/%s/claims/%s".formatted(submissionId, claimId);
  }

  @BeforeEach
  void setUpSession() {
    session.setAttribute(SUBMISSION_ID, submissionId);
    session.setAttribute(CLAIM_ID, claimId);

    when(dataClaimsRestClient.getClaimHistory(eq(claimId)))
        .thenReturn(Mono.just(ClaimHistoryResultSet.builder().events(List.of()).build()));
    when(submissionMessagesBuilder.buildAllWarnings(submissionId, claimId))
        .thenReturn(MessagesSummary.builder().messages(List.of()).build());
    when(featureFlagsConfig.getIsAlternativeClaimViewEnabled()).thenReturn(true);
  }

  @Test
  @DisplayName("Should tag only the summary rows changed by the amendment")
  void shouldTagOnlyAmendedSummaryRows() {
    stubLegalHelpClaim(Set.of("client.clientSurname", "claim.feeCode"));

    Document doc = renderDocument();

    assertThat(taggedSummaryRowLabels(doc)).containsExactlyInAnyOrder("Client name", "Fee code");
  }

  @Test
  @DisplayName("Should not tag any row when the claim has not been amended")
  void shouldNotTagAnyRowWhenNotAmended() {
    stubLegalHelpClaim(Set.of());

    Document doc = renderDocument();

    assertThat(doc.select("strong.govuk-tag--yellow")).isEmpty();
  }

  @Test
  @DisplayName("Should not tag rows in the values card when a value has been amended")
  void shouldNotTagValueRows() {
    stubLegalHelpClaim(Set.of("claimSummaryFee.netProfitCostsAmount"));

    Document doc = renderDocument();

    assertThat(doc.select("strong.govuk-tag--yellow")).isEmpty();
  }

  @Test
  @DisplayName("Should tag a mediation client row when either of its client fields changed")
  void shouldTagMediationClientRow() {
    stubMediationClaim(Set.of("client.client2Surname"));

    Document doc = renderDocument();

    assertThat(taggedSummaryRowLabels(doc)).containsExactly("Client 2 name");
  }

  private List<String> taggedSummaryRowLabels(Document doc) {
    return getSummaryListInCard(doc, "Summary").stream()
        .filter(
            row ->
                row.stream().anyMatch(cell -> !cell.select("strong.govuk-tag--yellow").isEmpty()))
        .map(row -> row.getFirst().text())
        .toList();
  }

  private void stubLegalHelpClaim(Set<String> amendedFields) {
    LegalHelpClaimDetails details = new LegalHelpClaimDetails();
    details.setClientForename("K");
    details.setClientSurname("Will");
    details.setUniqueFileNumber("271219/000");
    details.setOfficeCode("ABC123");
    details.setCategoryOfLaw("IMMIGRATION");
    details.setFeeCode("IMCA");
    details.setFeeCodeDescription("Immigration: application");
    details.setMatterTypeCodeOne("IACE");
    details.setCaseStartDate("2025-01-15");
    details.setCaseConcludedDate("2025-02-01");
    details.setEscapeCase(true);
    details.setAreaOfLaw(AreaOfLaw.LEGAL_HELP);
    details.setReportedLondonRateIndicator(true);
    details.setFixedFee(valueRow(100));
    details.setProfitCosts(valueRow(110));
    details.setDisbursements(valueRow(120));
    details.setDisbursementsVat(valueRow(130));
    details.setCounselsCosts(valueRow(140));
    details.setTravelAndWaitingCosts(valueRow(150));
    details.setDetentionTravelWaitingCosts(valueRow(160));
    details.setJrFormFilling(valueRow(170));
    details.setAdjournedHearingFee(valueRow(180));
    details.setCmrhOral(valueRow(190));
    details.setCmrhTelephone(valueRow(200));
    details.setHomeOfficeInterview(valueRow(210));
    details.setSubstantiveHearing(valueRow(220));
    details.setVat(valueRow(230));
    details.setTotalVat(valueRow(300));
    details.setTotalIncludingVat(valueRow(310));

    stubPageData(AreaOfLaw.LEGAL_HELP, new LegalHelpClaimDetailsView(details), amendedFields);
  }

  private void stubMediationClaim(Set<String> amendedFields) {
    MediationClaimDetails details = new MediationClaimDetails();
    details.setClientForename("Sally");
    details.setClientSurname("Jenkins");
    details.setUniqueClientNumber("02122002/S/JENK");
    details.setClient2Forename("Rob");
    details.setClient2Surname("Jenkins");
    details.setClient2UniqueClientNumber("02122002/R/JENK");
    details.setFeeCode("ASST");
    details.setOfficeCode("ABC123");
    details.setAreaOfLaw(AreaOfLaw.MEDIATION);
    details.setFixedFee(valueRow(100));
    details.setDisbursements(valueRow(110));
    details.setDisbursementsVat(valueRow(120));
    details.setVat(valueRow(130));
    details.setTotalVat(valueRow(300));
    details.setTotalIncludingVat(valueRow(310));

    stubPageData(AreaOfLaw.MEDIATION, new MediationClaimDetailsView(details), amendedFields);
  }

  private void stubPageData(
      AreaOfLaw areaOfLaw, ClaimDetailView claimDetailView, Set<String> amendedFields) {
    when(claimService.getClaimDetailPageData(submissionId, claimId, OIDC_USER))
        .thenReturn(
            new ClaimDetailPageData(areaOfLaw, false, claimDetailView, null, amendedFields));
  }

  private static ClaimFieldRow valueRow(int base) {
    return new ClaimFieldRow(
        BigDecimal.valueOf(base), BigDecimal.valueOf(base + 1), BigDecimal.valueOf(base + 2));
  }
}
