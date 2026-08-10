package uk.gov.justice.laa.bulkclaim.view;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.StringWriter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.thymeleaf.TemplateSpec;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import uk.gov.justice.laa.bulkclaim.builder.ClaimStatusBannerBuilder;
import uk.gov.justice.laa.bulkclaim.builder.LatestAssessmentResolver;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMessagesBuilder;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClientV2;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.controller.ClaimDetailController;
import uk.gov.justice.laa.bulkclaim.dto.submission.claim.viewmodels.ClaimStatusBanner;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessageRow;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.MessagesSummary;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimFeeCalculationBreakdownMapper;
import uk.gov.justice.laa.bulkclaim.mapper.ClaimSummaryMapper;
import uk.gov.justice.laa.bulkclaim.service.claimdetail.ClaimDetailViewFactory;
import uk.gov.justice.laa.bulkclaim.util.ThymeleafHrefUtils;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.DerivedClaimStatus;

@WebMvcTest(ClaimDetailController.class)
@Import({WebMvcTestConfig.class, ThymeleafHrefUtils.class, ClaimStatusBannerBuilder.class})
@DisplayName("Shared claim status banner fragment")
class ClaimStatusBannerFragmentTest {

  private static final String TEMPLATE = "fragments/claim-status-banner";
  private static final Set<String> SELECTOR = Set.of("claim-status-banner");

  @Autowired private SpringTemplateEngine templateEngine;
  @Autowired private ClaimStatusBannerBuilder claimStatusBannerBuilder;

  @MockitoBean private FeatureFlagsConfig featureFlagsConfig;
  @MockitoBean private DataClaimsRestClient dataClaimsRestClient;
  @MockitoBean private DataClaimsRestClientV2 dataClaimsRestClientV2;
  @MockitoBean private ClaimSummaryMapper claimSummaryMapper;
  @MockitoBean private ClaimFeeCalculationBreakdownMapper claimFeeCalculationBreakdownMapper;
  @MockitoBean private SubmissionMessagesBuilder submissionMessagesBuilder;
  @MockitoBean private ClaimDetailViewFactory claimDetailViewFactory;
  @MockitoBean private LatestAssessmentResolver latestAssessmentResolver;

  @Test
  @DisplayName("Renders the Voided wording with an error-style alert")
  void shouldRenderVoidedBannerAsErrorAlert() {
    ClaimStatusBanner banner =
        new ClaimStatusBanner(DerivedClaimStatus.VOIDED, "17/03/2026", "14:32");

    Document doc = render(banner, null);

    Element alert = doc.getElementById("claim-status-banner");
    assertThat(alert).isNotNull();
    assertThat(alert.hasClass("moj-alert--error")).isTrue();
    assertThat(alert.hasClass("moj-alert--information")).isFalse();
    assertThat(alert.selectFirst(".moj-alert__heading").text())
        .isEqualTo("This claim has been Voided. Last edited on 17/03/2026 at 14:32");
  }

  @Test
  @DisplayName("Renders the Assessed wording with an information-style alert")
  void shouldRenderAssessedBannerAsInformationAlert() {
    ClaimStatusBanner banner =
        new ClaimStatusBanner(DerivedClaimStatus.ASSESSED, "05/02/2026", "08:00");

    Document doc = render(banner, null);

    Element alert = doc.getElementById("claim-status-banner");
    assertThat(alert).isNotNull();
    assertThat(alert.hasClass("moj-alert--information")).isTrue();
    assertThat(alert.hasClass("moj-alert--error")).isFalse();
    assertThat(alert.selectFirst(".moj-alert__heading").text())
        .isEqualTo("This claim has been Assessed. Last edited on 05/02/2026 at 08:00");
  }

  @Test
  @DisplayName("Renders the Amended wording with an information-style alert")
  void shouldRenderAmendedBannerAsInformationAlert() {
    ClaimStatusBanner banner =
        new ClaimStatusBanner(DerivedClaimStatus.AMENDED, "30/06/2026", "23:59");

    Document doc = render(banner, null);

    Element alert = doc.getElementById("claim-status-banner");
    assertThat(alert).isNotNull();
    assertThat(alert.hasClass("moj-alert--information")).isTrue();
    assertThat(alert.selectFirst(".moj-alert__heading").text())
        .isEqualTo("This claim has been Amended. Last edited on 30/06/2026 at 23:59");
  }

  @Test
  @DisplayName("Renders no status banner for a derived status without one")
  void shouldRenderNoBannerForAcceptedStatus() {
    Optional<ClaimStatusBanner> banner =
        claimStatusBannerBuilder.build(DerivedClaimStatus.ACCEPTED, List.of());

    Document doc = render(banner.orElse(null), null);

    assertThat(doc.getElementById("claim-status-banner")).isNull();
  }

  @Test
  @DisplayName("Renders warning banners after the status banner, both together")
  void shouldRenderStatusBannerAndWarningBannersTogether() {
    ClaimStatusBanner banner =
        new ClaimStatusBanner(DerivedClaimStatus.ASSESSED, "05/02/2026", "08:00");
    MessagesSummary claimMessages =
        MessagesSummary.builder()
            .messages(
                List.of(
                    MessageRow.builder().message("First warning message").build(),
                    MessageRow.builder().message("Second warning message").build()))
            .build();

    Document doc = render(banner, claimMessages);

    Element statusAlert = doc.getElementById("claim-status-banner");
    assertThat(statusAlert).isNotNull();

    var warningAlerts = doc.getElementsByClass("moj-alert--warning");
    assertThat(warningAlerts).hasSize(2);
    assertThat(warningAlerts.eachText())
        .allSatisfy(text -> assertThat(text).contains("warning message"));

    // Status banner is ordered before the warning banners.
    assertThat(statusAlert.elementSiblingIndex())
        .isLessThan(warningAlerts.getFirst().parent().elementSiblingIndex());
  }

  @Test
  @DisplayName("Renders warning banners even when there is no status banner")
  void shouldRenderWarningBannersWithoutStatusBanner() {
    MessagesSummary claimMessages =
        MessagesSummary.builder()
            .messages(List.of(MessageRow.builder().message("Only warning").build()))
            .build();

    Document doc = render(null, claimMessages);

    assertThat(doc.getElementById("claim-status-banner")).isNull();
    assertThat(doc.getElementsByClass("moj-alert--warning")).hasSize(1);
  }

  private Document render(ClaimStatusBanner banner, MessagesSummary claimMessages) {
    Context context = new Context(Locale.UK);
    context.setVariable("banner", banner);
    context.setVariable("claimMessages", claimMessages);

    StringWriter writer = new StringWriter();
    templateEngine.process(
        new TemplateSpec(TEMPLATE, SELECTOR, (TemplateMode) null, Map.of()), context, writer);
    return Jsoup.parse(writer.toString());
  }
}
