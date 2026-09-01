package uk.gov.justice.laa.payments.submit.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.payments.submit.constants.SessionConstants.CLAIM_ID;
import static uk.gov.justice.laa.payments.submit.constants.SessionConstants.SUBMISSION_ID;
import static uk.gov.justice.laa.payments.submit.controller.ControllerTestHelper.OIDC_USER;
import static uk.gov.justice.laa.payments.submit.helper.AmendedFieldsTestData.amended;
import static uk.gov.justice.laa.payments.submit.util.MatterTypeUtil.FIRST_PART;
import static uk.gov.justice.laa.payments.submit.util.MatterTypeUtil.SECOND_PART;

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
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryResultSet;
import uk.gov.justice.laa.payments.submit.builder.ClaimStatusBannerBuilder;
import uk.gov.justice.laa.payments.submit.builder.LatestAssessmentResolver;
import uk.gov.justice.laa.payments.submit.builder.SubmissionMessagesBuilder;
import uk.gov.justice.laa.payments.submit.client.DataClaimsRestClientV2;
import uk.gov.justice.laa.payments.submit.controller.ClaimDetailController;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.LegalHelpClaimDetails;
import uk.gov.justice.laa.payments.submit.dto.submission.claim.viewmodels.MediationClaimDetails;
import uk.gov.justice.laa.payments.submit.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.payments.submit.mapper.ClaimFeeCalculationBreakdownMapper;
import uk.gov.justice.laa.payments.submit.mapper.ClaimSummaryMapper;
import uk.gov.justice.laa.payments.submit.service.ClaimService;
import uk.gov.justice.laa.payments.submit.service.SubmissionService;
import uk.gov.justice.laa.payments.submit.util.MatterTypeUtil;
import uk.gov.justice.laa.payments.submit.viewmodels.claimdetails.ClaimDetailPageData;
import uk.gov.justice.laa.payments.submit.viewmodels.claimdetails.ClaimDetailView;
import uk.gov.justice.laa.payments.submit.viewmodels.claimdetails.ClaimDetailViewFactory;
import uk.gov.justice.laa.payments.submit.viewmodels.claimdetails.LegalHelpClaimDetailsView;
import uk.gov.justice.laa.payments.submit.viewmodels.claimdetails.MediationClaimDetailsView;

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
    when(submissionMessagesBuilder.buildAllWarnings(OIDC_USER, submissionId, claimId))
        .thenReturn(MessagesSummary.builder().messages(List.of()).build());
    when(featureFlagsConfig.getIsAlternativeClaimViewEnabled()).thenReturn(true);
  }

  @Test
  @DisplayName("Should tag only the summary rows changed by the amendment")
  void shouldTagOnlyAmendedSummaryRows() {
    stubLegalHelpClaim(amended("client.clientSurname", "claim.feeCode"));

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
  @DisplayName("Should tag a cost row that the provider amended")
  void shouldTagAmendedCostRow() {
    stubLegalHelpClaim(amended("claimSummaryFee.netProfitCostsAmount"));

    Document doc = renderDocument();

    assertThat(taggedRowLabels(doc, "Values")).containsExactly("Profit costs (excluding VAT)");
  }

  @Test
  @DisplayName("Should not tag the totals when the fee scheme did not reprice the claim")
  void shouldNotTagTotalsWhenNotRepriced() {
    when(featureFlagsConfig.getIsAssessedColumnEnabled()).thenReturn(true);
    stubLegalHelpClaim(amended("claimSummaryFee.netProfitCostsAmount"));

    Document doc = renderDocument();

    assertThat(taggedRowLabels(doc, "Total allowed value")).isEmpty();
  }

  @Test
  @DisplayName("Should tag the totals that the fee scheme recalculated")
  void shouldTagRecalculatedTotals() {
    when(featureFlagsConfig.getIsAssessedColumnEnabled()).thenReturn(true);
    stubLegalHelpClaim(
        amended(
            "claimSummaryFee.netProfitCostsAmount",
            "fee.netProfitCostsAmount",
            "fee.calculatedVatAmount",
            "fee.totalAmount"));

    Document doc = renderDocument();

    assertThat(taggedRowLabels(doc, "Total allowed value"))
        .containsExactlyInAnyOrder("Total VAT", "Total including VAT");
    assertThat(taggedRowLabels(doc, "Values")).containsExactly("Profit costs (excluding VAT)");
  }

  @Test
  @DisplayName("Should tag the recalculated totals when the amended field is not a displayed row")
  void shouldTagRecalculatedTotalsWhenAmendedFieldIsNotDisplayed() {
    when(featureFlagsConfig.getIsAssessedColumnEnabled()).thenReturn(true);
    stubLegalHelpClaim(amended("claim.schemeId", "fee.calculatedVatAmount", "fee.totalAmount"));

    Document doc = renderDocument();

    assertThat(taggedRowLabels(doc, "Total allowed value"))
        .containsExactlyInAnyOrder("Total VAT", "Total including VAT");
    assertThat(taggedSummaryRowLabels(doc)).isEmpty();
  }

  @Test
  @DisplayName("Should tag the fee scheme rows that a repricing changed")
  void shouldTagRepricedFeeSchemeRows() {
    stubLegalHelpClaim(
        amended(
            "claim.feeCode", "fee.feeCodeDescription", "fee.escapeCaseFlag", "fee.categoryOfLaw"));

    Document doc = renderDocument();

    assertThat(taggedSummaryRowLabels(doc))
        .containsExactlyInAnyOrder(
            "Fee code", "Fee code description", "Escape case", "Category of law");
  }

  @Test
  @DisplayName("Should not tag any cost row when only a summary field was amended")
  void shouldNotTagCostRowsForSummaryAmendment() {
    stubLegalHelpClaim(amended("claim.feeCode"));

    Document doc = renderDocument();

    assertThat(taggedRowLabels(doc, "Values")).isEmpty();
  }

  @Test
  @DisplayName("Should tag a mediation client row when either of its client fields changed")
  void shouldTagMediationClientRow() {
    stubMediationClaim(amended("client.client2Surname"));

    Document doc = renderDocument();

    assertThat(taggedSummaryRowLabels(doc)).containsExactly("Client 2 name");
  }

  @Test
  @DisplayName("Should tag only the matter type row whose half of the code changed")
  void shouldTagOnlyChangedMatterTypeRow() {
    stubLegalHelpClaim(amended(MatterTypeUtil.partIdentifier(SECOND_PART)));

    Document doc = renderDocument();

    assertThat(taggedSummaryRowLabels(doc)).containsExactly("Matter type 2");
  }

  @Test
  @DisplayName("Should tag both matter type rows when both halves of the code changed")
  void shouldTagBothMatterTypeRowsWhenBothHalvesChanged() {
    stubLegalHelpClaim(
        amended(
            MatterTypeUtil.partIdentifier(FIRST_PART), MatterTypeUtil.partIdentifier(SECOND_PART)));

    Document doc = renderDocument();

    assertThat(taggedSummaryRowLabels(doc))
        .containsExactlyInAnyOrder("Matter type 1", "Matter type 2");
  }

  private List<String> taggedSummaryRowLabels(Document doc) {
    return taggedRowLabels(doc, "Summary");
  }

  private List<String> taggedRowLabels(Document doc, String cardTitle) {
    return getSummaryListInCard(doc, cardTitle).stream()
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
