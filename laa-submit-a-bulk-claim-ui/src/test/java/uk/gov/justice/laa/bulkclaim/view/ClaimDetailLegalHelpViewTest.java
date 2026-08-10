package uk.gov.justice.laa.bulkclaim.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.CLAIM_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClientV2;
import uk.gov.justice.laa.bulkclaim.controller.ClaimDetailController;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimFieldRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimStatusBanner;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimValueRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.LegalHelpClaimDetails;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessageRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.bulkclaim.helper.TestObjectCreator;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimFeeCalculationBreakdownMapper;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimSummaryMapper;
import uk.gov.justice.laa.bulkclaim.service.claimdetail.ClaimDetailView;
import uk.gov.justice.laa.bulkclaim.service.claimdetail.ClaimDetailViewFactory;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.DerivedClaimStatus;

@WebMvcTest(ClaimDetailController.class)
@DisplayName("Legal Help claim details page")
class ClaimDetailLegalHelpViewTest extends ViewTestBase {

  @MockitoBean private DataClaimsRestClient dataClaimsRestClient;
  @MockitoBean private DataClaimsRestClientV2 dataClaimsRestClientV2;
  @MockitoBean private ClaimSummaryMapper claimSummaryMapper;
  @MockitoBean private ClaimFeeCalculationBreakdownMapper claimFeeCalculationBreakdownMapper;
  @MockitoBean private SubmissionMessagesBuilder submissionMessagesBuilder;
  @MockitoBean private ClaimDetailViewFactory claimDetailViewFactory;
  @MockitoBean private ClaimStatusBannerBuilder claimStatusBannerBuilder;
  @MockitoBean private LatestAssessmentResolver latestAssessmentResolver;

  private LegalHelpClaimDetails details;

  ClaimDetailLegalHelpViewTest() {
    this.mapping = "/view-claim-detail";
  }

  @BeforeEach
  void setUpSession() {
    session.setAttribute(SUBMISSION_ID, submissionId);
    session.setAttribute(CLAIM_ID, claimId);

    details =
        LegalHelpClaimDetails.builder()
            .clientForename("K")
            .clientSurname("Will")
            .uniqueFileNumber("271219/000")
            .officeCode("ABC123")
            .categoryOfLaw("IMMIGRATION")
            .feeCode("IMCA")
            .feeCodeDescription("Immigration: application")
            .matterTypeCode("IACE")
            .caseStartDate("2025-01-15")
            .caseConcludedDate("2025-02-01")
            .escapeCase(true)
            .areaOfLaw(AreaOfLaw.LEGAL_HELP)
            .build();

    when(dataClaimsRestClient.getClaimHistory(eq(claimId), any()))
        .thenReturn(Mono.just(ClaimHistoryResultSet.builder().events(List.of()).build()));
    when(submissionMessagesBuilder.buildAllWarnings(submissionId, claimId))
        .thenReturn(MessagesSummary.builder().messages(List.of()).build());
  }

  private void stubClaim(
      DerivedClaimStatus derivedClaimStatus, Optional<ClaimStatusBanner> banner) {
    ClaimResponseV2 claimResponse = TestObjectCreator.buildClaimResponseV2(AreaOfLaw.LEGAL_HELP);
    claimResponse.setDerivedClaimStatus(derivedClaimStatus);

    when(dataClaimsRestClientV2.getSubmissionClaim(submissionId, claimId))
        .thenReturn(Mono.just(claimResponse));

    List<ClaimValueRow> valueRows =
        List.of(new ClaimValueRow("Fixed fee", new ClaimFieldRow(null, new BigDecimal("239.00"))));
    List<ClaimValueRow> totalRows =
        List.of(
            new ClaimValueRow(
                "Total including VAT", new ClaimFieldRow(null, new BigDecimal("12.00"))));
    when(claimDetailViewFactory.build(eq(claimResponse), any()))
        .thenReturn(new ClaimDetailView.LegalHelp(details, valueRows, totalRows));

    when(claimStatusBannerBuilder.build(derivedClaimStatus, List.of())).thenReturn(banner);
  }

  @Test
  @DisplayName("Renders header, summary and values for an unassessed claim, with no status banner")
  void shouldRenderUnassessedClaim() {
    stubClaim(DerivedClaimStatus.READY_TO_PROCESS, Optional.empty());

    Document doc = renderDocument();

    assertPageHasHeading(doc, "K Will");
    assertThat(doc.getElementById("claim-status-banner")).isNull();

    var summary = getSummaryListInCard(doc, "Summary");
    assertRowContainsValues(summary.getFirst(), "Client name", "K Will");

    var values = getSummaryListInCard(doc, "Values");
    // header row + 1 data row; current calculated column absent (2 values, not 3)
    assertThat(values.getFirst()).hasSize(3);
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
        .contains("This claim has been Amended. Last edited on 01/02/2026 at 10:00");

    var values = getSummaryListInCard(doc, "Values");
    assertThat(values.getFirst()).hasSize(4);
  }

  @Test
  @DisplayName("Shows the Current calculated column and Assessed banner for an assessed claim")
  void shouldShowCurrentCalculatedWhenAssessed() {
    ClaimStatusBanner banner =
        new ClaimStatusBanner(DerivedClaimStatus.ASSESSED, "02/02/2026", "11:00");
    stubClaim(DerivedClaimStatus.ASSESSED, Optional.of(banner));

    Document doc = renderDocument();

    assertThat(doc.getElementById("claim-status-banner").text())
        .contains("This claim has been Assessed. Last edited on 02/02/2026 at 11:00");

    var totals = getSummaryListInCard(doc, "Total allowed value");
    assertThat(totals.getFirst()).hasSize(4);
  }

  @Test
  @DisplayName("Shows the Voided banner but keeps the Current calculated column hidden")
  void shouldRenderVoidedBannerWithoutCurrentCalculatedColumn() {
    ClaimStatusBanner banner =
        new ClaimStatusBanner(DerivedClaimStatus.VOIDED, "03/02/2026", "12:00");
    stubClaim(DerivedClaimStatus.VOIDED, Optional.of(banner));

    Document doc = renderDocument();

    assertThat(doc.getElementById("claim-status-banner").text())
        .contains("This claim has been Voided. Last edited on 03/02/2026 at 12:00");

    var values = getSummaryListInCard(doc, "Values");
    assertThat(values.getFirst()).hasSize(3);
  }

  @Test
  @DisplayName("Renders warning banners alongside the status banner")
  void shouldRenderWarningBannersAlongsideStatusBanner() {
    ClaimStatusBanner banner =
        new ClaimStatusBanner(DerivedClaimStatus.ASSESSED, "02/02/2026", "11:00");
    stubClaim(DerivedClaimStatus.ASSESSED, Optional.of(banner));
    when(submissionMessagesBuilder.buildAllWarnings(submissionId, claimId))
        .thenReturn(
            MessagesSummary.builder()
                .messages(List.of(MessageRow.builder().message("A warning").build()))
                .build());

    Document doc = renderDocument();

    assertThat(doc.getElementById("claim-status-banner")).isNotNull();
    assertThat(doc.getElementsByClass("moj-alert--information")).hasSize(1);
  }
}
