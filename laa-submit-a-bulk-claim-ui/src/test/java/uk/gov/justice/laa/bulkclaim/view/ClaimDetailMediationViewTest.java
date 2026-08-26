package uk.gov.justice.laa.bulkclaim.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.CLAIM_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;
import static uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper.OIDC_USER;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimStatusBanner;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.MediationClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessageRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.bulkclaim.helper.TestObjectCreator;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimFeeCalculationBreakdownMapper;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimSummaryMapper;
import uk.gov.justice.laa.bulkclaim.service.ClaimService;
import uk.gov.justice.laa.bulkclaim.service.SubmissionService;
import uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails.ClaimDetailPageData;
import uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails.ClaimDetailViewFactory;
import uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails.MediationClaimDetailsView;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.DerivedClaimStatus;

@WebMvcTest(ClaimDetailController.class)
@DisplayName("Mediation claim details page")
class ClaimDetailMediationViewTest extends ViewTestBase {

  @MockitoBean private DataClaimsRestClientV2 dataClaimsRestClientV2;
  @MockitoBean private ClaimSummaryMapper claimSummaryMapper;
  @MockitoBean private ClaimFeeCalculationBreakdownMapper claimFeeCalculationBreakdownMapper;
  @MockitoBean private SubmissionMessagesBuilder submissionMessagesBuilder;
  @MockitoBean private ClaimService claimService;
  @MockitoBean private ClaimDetailViewFactory claimDetailViewFactory;
  @MockitoBean private ClaimStatusBannerBuilder claimStatusBannerBuilder;
  @MockitoBean private LatestAssessmentResolver latestAssessmentResolver;
  @MockitoBean private SubmissionService submissionService;

  private MediationClaimDetails details;

  ClaimDetailMediationViewTest() {
    this.mapping = "/submissions/%s/claims/%s".formatted(submissionId, claimId);
  }

  @BeforeEach
  void setUpSession() {
    session.setAttribute(SUBMISSION_ID, submissionId);
    session.setAttribute(CLAIM_ID, claimId);

    details = new MediationClaimDetails();
    details.setClientForename("Sally");
    details.setClientSurname("Jenkins");
    details.setUniqueClientNumber("02122002/S/JENK");
    details.setFeeCode("ASST");
    details.setFeeCodeDescription("Assessment Together");
    details.setOfficeCode("ABC123");
    details.setAreaOfLaw(AreaOfLaw.MEDIATION);
    details.setMatterTypeCode("ASST-ASST");
    details.setCaseStartDate("2025-01-15");
    details.setCaseConcludedDate("2025-02-01");
    setValueFields(details);

    when(dataClaimsRestClient.getClaimHistory(eq(claimId)))
        .thenReturn(Mono.just(ClaimHistoryResultSet.builder().events(List.of()).build()));
    when(submissionMessagesBuilder.buildAllWarnings(OIDC_USER, submissionId, claimId))
        .thenReturn(MessagesSummary.builder().messages(List.of()).build());
    when(featureFlagsConfig.getIsAlternativeClaimViewEnabled()).thenReturn(true);
  }

  private void setValueFields(MediationClaimDetails details) {
    details.setFixedFee(valueRow(100));
    details.setDisbursements(valueRow(110));
    details.setDisbursementsVat(valueRow(120));
    details.setVat(valueRow(130));
    details.setTotalVat(valueRow(300));
    details.setTotalIncludingVat(valueRow(310));
  }

  /** Builds a ClaimFieldRow with distinct reported/initialCalculated/assessed values. */
  private static ClaimFieldRow valueRow(int base) {
    return new ClaimFieldRow(
        BigDecimal.valueOf(base), BigDecimal.valueOf(base + 1), BigDecimal.valueOf(base + 2));
  }

  private static String currency(int amount) {
    return "£%d.00".formatted(amount);
  }

  private void assertAllThreeValueColumnsPopulated(List<List<Element>> values) {
    assertRowContainsValues(
        values.get(1), "Fixed fee", currency(100), currency(101), currency(102));
    assertRowContainsValues(
        values.get(2),
        "Disbursements (excluding VAT)",
        currency(110),
        currency(111),
        currency(112));
    assertRowContainsValues(
        values.get(3), "Disbursement VAT", currency(120), currency(121), currency(122));
    assertRowContainsValues(values.get(4), "VAT", currency(130), currency(131), currency(132));
  }

  private void stubClaim(
      DerivedClaimStatus derivedClaimStatus, Optional<ClaimStatusBanner> banner) {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.MEDIATION);
    claimResponse.setDerivedClaimStatus(derivedClaimStatus);

    MediationClaimDetailsView claimDetailView = new MediationClaimDetailsView(details);
    boolean showCurrentCalculated =
        derivedClaimStatus == DerivedClaimStatus.AMENDED
            || derivedClaimStatus == DerivedClaimStatus.ASSESSED;
    when(claimService.getClaimDetailPageData(submissionId, claimId, OIDC_USER))
        .thenReturn(
            new ClaimDetailPageData(
                AreaOfLaw.MEDIATION, showCurrentCalculated, claimDetailView, banner.orElse(null)));
  }

  @Test
  @DisplayName("Renders header, summary and values for an unassessed claim, with no status banner")
  void shouldRenderUnassessedClaim() {
    stubClaim(DerivedClaimStatus.READY_TO_PROCESS, Optional.empty());

    Document doc = renderDocument();

    assertPageHasHeading(doc, "Sally Jenkins");
    assertThat(doc.getElementById("claim-status-banner")).isNull();

    var summary = getSummaryListInCard(doc, "Summary");
    assertRowContainsValues(summary.getFirst(), "Client 1 name", "Sally Jenkins");
    assertRowContainsValues(summary.get(1), "Client 1 UCN", "02122002/S/JENK");
    assertRowContainsValues(summary.get(2), "Client 2 name", "Not applicable");
    assertRowContainsValues(summary.get(3), "Client 2 UCN", "Not applicable");
    assertRowContainsValues(summary.get(4), "Office account number", "ABC123");
    assertRowContainsValues(summary.get(5), "Date submitted", "Not applicable");
    assertRowContainsValues(summary.get(6), "Area of law", "MEDIATION");
    assertRowContainsValues(summary.get(7), "Fee code", "ASST");
    assertRowContainsValues(summary.get(8), "Fee code description", "Assessment Together");
    assertRowContainsValues(summary.get(9), "Matter type", "ASST-ASST");
    assertRowContainsValues(summary.get(10), "Case start date", "2025-01-15");
    assertRowContainsValues(summary.get(11), "Date of work concluded", "2025-02-01");

    var values = getSummaryListInCard(doc, "Values");
    // Just two columns of values + label, current calculated should be hidden
    assertThat(values.getFirst()).hasSize(3);
    assertRowContainsValues(values.get(1), "Fixed fee", currency(100), currency(101));
    assertRowContainsValues(
        values.get(2), "Disbursements (excluding VAT)", currency(110), currency(111));
    assertRowContainsValues(values.get(3), "Disbursement VAT", currency(120), currency(121));
    assertRowContainsValues(values.get(4), "VAT", currency(130), currency(131));
  }

  @Test
  @DisplayName("Renders Client 2 details in the header and summary table when present")
  void shouldRenderClient2WhenPresent() {
    details = new MediationClaimDetails();
    details.setClientForename("Sally");
    details.setClientSurname("Jenkins");
    details.setUniqueClientNumber("02122002/S/JENK");
    details.setClient2Forename("John");
    details.setClient2Surname("Smith");
    details.setClient2UniqueClientNumber("02122002/J/SMIT");
    details.setFeeCode("ASST");
    details.setFeeCodeDescription("Assessment Together");
    details.setOfficeCode("ABC123");
    details.setAreaOfLaw(AreaOfLaw.MEDIATION);
    details.setMatterTypeCode("ASST-ASST");
    details.setCaseStartDate("2025-01-15");
    details.setCaseConcludedDate("2025-02-01");
    setValueFields(details);
    stubClaim(DerivedClaimStatus.READY_TO_PROCESS, Optional.empty());

    Document doc = renderDocument();

    var summary = getSummaryListInCard(doc, "Summary");
    assertRowContainsValues(summary.get(2), "Client 2 name", "John Smith");
    assertRowContainsValues(summary.get(3), "Client 2 UCN", "02122002/J/SMIT");
  }

  @Test
  @DisplayName("Shows the Current calculated column and Amended banner for an amended claim")
  void shouldShowCurrentCalculatedWhenAmended() {
    ClaimStatusBanner banner =
        new ClaimStatusBanner(DerivedClaimStatus.AMENDED, "01/02/2026", "10:00");
    stubClaim(DerivedClaimStatus.AMENDED, Optional.of(banner));

    Document doc = renderDocument();

    assertThat(doc.getElementById("claim-status-banner")).isNotNull();
    assertThat(doc.getElementById("claim-status-banner").text())
        .contains("This claim has been Amended Last edited on 01/02/2026 at 10:00");

    var values = getSummaryListInCard(doc, "Values");
    assertThat(values.getFirst()).hasSize(4);
    assertAllThreeValueColumnsPopulated(values);
  }

  @Test
  @DisplayName("Shows the Current calculated column and Assessed banner for an assessed claim")
  void shouldShowCurrentCalculatedWhenAssessed() {
    ClaimStatusBanner banner =
        new ClaimStatusBanner(DerivedClaimStatus.ASSESSED, "02/02/2026", "11:00");
    stubClaim(DerivedClaimStatus.ASSESSED, Optional.of(banner));

    when(featureFlagsConfig.getIsAssessedColumnEnabled()).thenReturn(true);

    Document doc = renderDocument();

    assertThat(doc.getElementById("claim-status-banner").text())
        .contains("This claim has been Assessed Last edited on 02/02/2026 at 11:00");

    var values = getSummaryListInCard(doc, "Values");
    assertAllThreeValueColumnsPopulated(values);

    var totals = getSummaryListInCard(doc, "Total allowed value");
    assertThat(totals.getFirst()).hasSize(4);
    assertRowContainsValues(
        totals.get(1), "Total VAT", currency(300), currency(301), currency(302));
    assertRowContainsValues(
        totals.get(2), "Total including VAT", currency(310), currency(311), currency(312));
  }

  @Test
  @DisplayName("Shows the Voided banner but keeps the Current calculated column hidden")
  void shouldRenderVoidedBannerWithoutCurrentCalculatedColumn() {
    ClaimStatusBanner banner =
        new ClaimStatusBanner(DerivedClaimStatus.VOIDED, "03/02/2026", "12:00");
    stubClaim(DerivedClaimStatus.VOIDED, Optional.of(banner));

    Document doc = renderDocument();

    assertThat(doc.getElementById("claim-status-banner").text())
        .contains("This claim has been Voided Last edited on 03/02/2026 at 12:00");

    var values = getSummaryListInCard(doc, "Values");
    assertThat(values.getFirst()).hasSize(3);
  }

  @Test
  @DisplayName("Renders warning banners alongside the status banner")
  void shouldRenderWarningBannersAlongsideStatusBanner() {
    ClaimStatusBanner banner =
        new ClaimStatusBanner(DerivedClaimStatus.ASSESSED, "02/02/2026", "11:00");
    stubClaim(DerivedClaimStatus.ASSESSED, Optional.of(banner));
    when(submissionMessagesBuilder.buildAllWarnings(OIDC_USER, submissionId, claimId))
        .thenReturn(
            MessagesSummary.builder()
                .messages(List.of(MessageRow.builder().message("A warning").build()))
                .build());

    Document doc = renderDocument();

    assertThat(doc.getElementById("claim-status-banner")).isNotNull();
    assertThat(doc.getElementsByClass("moj-alert--information")).hasSize(1);
  }
}
