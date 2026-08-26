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
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.LegalHelpClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessageRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.bulkclaim.helper.TestObjectCreator;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimFeeCalculationBreakdownMapper;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimSummaryMapper;
import uk.gov.justice.laa.bulkclaim.service.ClaimService;
import uk.gov.justice.laa.bulkclaim.service.SubmissionService;
import uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails.ClaimDetailPageData;
import uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails.ClaimDetailViewFactory;
import uk.gov.justice.laa.bulkclaim.viewmodels.claimdetails.LegalHelpClaimDetailsView;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.DerivedClaimStatus;

@WebMvcTest(ClaimDetailController.class)
@DisplayName("Legal Help claim details page")
class ClaimDetailLegalHelpViewTest extends ViewTestBase {

  @MockitoBean private DataClaimsRestClientV2 dataClaimsRestClientV2;
  @MockitoBean private ClaimSummaryMapper claimSummaryMapper;
  @MockitoBean private ClaimFeeCalculationBreakdownMapper claimFeeCalculationBreakdownMapper;
  @MockitoBean private SubmissionMessagesBuilder submissionMessagesBuilder;
  @MockitoBean private ClaimService claimService;
  @MockitoBean private ClaimDetailViewFactory claimDetailViewFactory;
  @MockitoBean private ClaimStatusBannerBuilder claimStatusBannerBuilder;
  @MockitoBean private LatestAssessmentResolver latestAssessmentResolver;
  @MockitoBean private SubmissionService submissionService;

  private LegalHelpClaimDetails details;

  ClaimDetailLegalHelpViewTest() {
    this.mapping = "/submissions/%s/claims/%s".formatted(submissionId, claimId);
  }

  @BeforeEach
  void setUpSession() {
    session.setAttribute(SUBMISSION_ID, submissionId);
    session.setAttribute(CLAIM_ID, claimId);

    details = new LegalHelpClaimDetails();
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
    details.setVat(valueRow(230));
    details.setTotalVat(valueRow(300));
    details.setTotalIncludingVat(valueRow(310));
    details.setCounselsCosts(valueRow(140));
    details.setTravelAndWaitingCosts(valueRow(150));
    details.setDetentionTravelWaitingCosts(valueRow(160));
    details.setJrFormFilling(valueRow(170));
    details.setAdjournedHearingFee(valueRow(180));
    details.setCmrhOral(valueRow(190));
    details.setCmrhTelephone(valueRow(200));
    details.setHomeOfficeInterview(valueRow(210));
    details.setSubstantiveHearing(valueRow(220));

    when(dataClaimsRestClient.getClaimHistory(eq(claimId)))
        .thenReturn(Mono.just(ClaimHistoryResultSet.builder().events(List.of()).build()));
    when(submissionMessagesBuilder.buildAllWarnings(OIDC_USER, submissionId, claimId))
        .thenReturn(MessagesSummary.builder().messages(List.of()).build());
    when(featureFlagsConfig.getIsAlternativeClaimViewEnabled()).thenReturn(true);
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
        values.get(2), "Profit costs (excluding VAT)", currency(110), currency(111), currency(112));
    assertRowContainsValues(
        values.get(3),
        "Disbursements (excluding VAT)",
        currency(120),
        currency(121),
        currency(122));
    assertRowContainsValues(
        values.get(4), "Disbursement VAT", currency(130), currency(131), currency(132));
    assertRowContainsValues(
        values.get(5),
        "Counsel's costs (excluding VAT)",
        currency(140),
        currency(141),
        currency(142));
    assertRowContainsValues(
        values.get(6), "Travel and waiting costs", currency(150), currency(151), currency(152));
    assertRowContainsValues(
        values.get(7),
        "Detention, travel and waiting costs",
        currency(160),
        currency(161),
        currency(162));
    assertRowContainsValues(
        values.get(8),
        "Judicial review or form filling",
        currency(170),
        currency(171),
        currency(172));
    assertRowContainsValues(
        values.get(9), "Adjourned hearing fee", currency(180), currency(181), currency(182));
    assertRowContainsValues(
        values.get(10),
        "Case management review hearing (CMRH) - oral",
        currency(190),
        currency(191),
        currency(192));
    assertRowContainsValues(
        values.get(11),
        "Case management review hearing (CMRH) - telephone",
        currency(200),
        currency(201),
        currency(202));
    assertRowContainsValues(
        values.get(12), "London rate", "Yes", "Not applicable", "Not applicable");
    assertRowContainsValues(
        values.get(13), "Home Office interview", currency(210), currency(211), currency(212));
    assertRowContainsValues(
        values.get(14), "Substantive hearing", currency(220), currency(221), currency(222));
    assertRowContainsValues(values.get(15), "VAT", currency(230), currency(231), currency(232));
  }

  private void stubClaim(
      DerivedClaimStatus derivedClaimStatus, Optional<ClaimStatusBanner> banner) {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.LEGAL_HELP);
    claimResponse.setDerivedClaimStatus(derivedClaimStatus);

    LegalHelpClaimDetailsView claimDetailView = new LegalHelpClaimDetailsView(details);
    boolean showCurrentCalculated =
        derivedClaimStatus == DerivedClaimStatus.AMENDED
            || derivedClaimStatus == DerivedClaimStatus.ASSESSED;
    when(claimService.getClaimDetailPageData(submissionId, claimId, OIDC_USER))
        .thenReturn(
            new ClaimDetailPageData(
                AreaOfLaw.LEGAL_HELP, showCurrentCalculated, claimDetailView, banner.orElse(null)));
  }

  @Test
  @DisplayName("Renders header, summary and values for an unassessed claim, with no status banner")
  void shouldRenderUnassessedClaim() {
    stubClaim(DerivedClaimStatus.READY_TO_PROCESS, Optional.empty());

    Document doc = renderDocument();

    assertPageHasHeading(doc, "K Will");
    assertThat(doc.getElementById("claim-status-banner")).isNull();

    var summary = getSummaryListInCard(doc, "Summary");
    assertRowContainsValues(summary.get(0), "Client name", "K Will");
    assertRowContainsValues(summary.get(1), "Unique file number (UFN)", "271219/000");
    assertRowContainsValues(summary.get(2), "Office account number", "ABC123");
    assertRowContainsValues(summary.get(3), "Date submitted", "Not applicable");
    assertRowContainsValues(summary.get(4), "Area of law", "LEGAL HELP");
    assertRowContainsValues(summary.get(5), "Category of law", "IMMIGRATION");
    assertRowContainsValues(summary.get(6), "Fee code", "IMCA");
    assertRowContainsValues(summary.get(7), "Fee code description", "Immigration: application");
    assertRowContainsValues(summary.get(8), "Matter type 1", "IACE");
    assertRowContainsValues(summary.get(9), "Matter type 2", "Not applicable");
    assertRowContainsValues(summary.get(10), "Case start date", "2025-01-15");
    assertRowContainsValues(summary.get(11), "Date of work concluded", "2025-02-01");
    assertRowContainsValues(summary.get(12), "Escape case", "Yes");

    var values = getSummaryListInCard(doc, "Values");
    // Just two columns of values + label, current calculated should be hidden
    assertThat(values.getFirst()).hasSize(3);
    assertRowContainsValues(values.get(1), "Fixed fee", currency(100), currency(101));
    assertRowContainsValues(
        values.get(2), "Profit costs (excluding VAT)", currency(110), currency(111));
    assertRowContainsValues(
        values.get(3), "Disbursements (excluding VAT)", currency(120), currency(121));
    assertRowContainsValues(values.get(4), "Disbursement VAT", currency(130), currency(131));
    assertRowContainsValues(
        values.get(5), "Counsel's costs (excluding VAT)", currency(140), currency(141));
    assertRowContainsValues(
        values.get(6), "Travel and waiting costs", currency(150), currency(151));
    assertRowContainsValues(
        values.get(7), "Detention, travel and waiting costs", currency(160), currency(161));
    assertRowContainsValues(
        values.get(8), "Judicial review or form filling", currency(170), currency(171));
    assertRowContainsValues(values.get(9), "Adjourned hearing fee", currency(180), currency(181));
    assertRowContainsValues(
        values.get(10),
        "Case management review hearing (CMRH) - oral",
        currency(190),
        currency(191));
    assertRowContainsValues(
        values.get(11),
        "Case management review hearing (CMRH) - telephone",
        currency(200),
        currency(201));
    assertRowContainsValues(values.get(12), "London rate", "Yes", "Not applicable");
    assertRowContainsValues(values.get(13), "Home Office interview", currency(210), currency(211));
    assertRowContainsValues(values.get(14), "Substantive hearing", currency(220), currency(221));
    assertRowContainsValues(values.get(15), "VAT", currency(230), currency(231));
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
