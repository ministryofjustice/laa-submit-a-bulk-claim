package uk.gov.justice.laa.bulkclaim.ui.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.justice.laa.bulkclaim.ui.helpers.AbstractUiTest;
import uk.gov.justice.laa.bulkclaim.ui.pages.ClaimDetailPage;
import uk.gov.justice.laa.bulkclaim.ui.pages.UploadPage;

@DisplayName("Feature: Core UI content checks")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CoreContentUiTest extends AbstractUiTest {

  private ClaimDetailPage claimDetailPage;

  @BeforeEach
  void setUp() {
    claimDetailPage = new ClaimDetailPage(page);
  }

  @Test
  @DisplayName("Scenario: Landing page shows core content")
  void shouldShowLandingPageCoreContent() {
    page.navigate(appLandingUrl());

    assertTrue(page.locator("h1:has-text('Submit a bulk claim')").isVisible());
    assertTrue(page.locator("a.govuk-button:has-text('Start now')").isVisible());
  }

  @Test
  @DisplayName("Scenario: Upload page shows core content")
  void shouldShowUploadPageCoreContent() {
    page.navigate(appUrl("/upload"));

    assertTrue(page.locator("h1:has-text('Upload a bulk claim file')").isVisible());
    assertTrue(page.locator("button.govuk-button:has-text('Continue')").isVisible());
  }

  @Test
  @DisplayName("Scenario: Import-in-progress page shows core content")
  void shouldShowImportInProgressCoreContent() {
    UploadPage uploadPage = new UploadPage(page);
    uploadPage.open(appLandingUrl());
    uploadPage.uploadFileAndSubmit("content-smoke.csv", "text/csv", "h1,h2\nv1,v2\n");
    uploadPage.waitForUploadBeingCheckedPage();

    assertTrue(page.locator("h1:has-text('Your file is being checked')").isVisible());
    assertTrue(page.locator("#go-to-search-button:has-text('Go to search')").isVisible());
  }

  @Test
  @DisplayName("Scenario: Search page shows core content")
  void shouldShowSearchPageCoreContent() {
    page.navigate(appUrl("/submissions/search"));

    assertTrue(page.locator("h1:has-text('Search for a submission')").isVisible());
    assertTrue(page.locator("#searchButton:has-text('Search')").isVisible());
    assertTrue(page.locator("label[for='area-of-law']:has-text('Area of law')").isVisible());
  }

  @ParameterizedTest(name = "Scenario: Claim details show fee calculation headings for {0}")
  @MethodSource("allAreaArguments")
  void shouldShowFeeCalculationHeadingsForArea(
      String areaOfLawAbbr,
      uk.gov.justice.laa.bulkclaim.accessibility.helpers.AbstractAccessibilityTest.AreaScenario
          scenario) {
    assertTrue(
        areaOfLawAbbr != null && !areaOfLawAbbr.isBlank(),
        "Expected area abbreviation to be present for scenario " + scenario.areaOfLaw());

    claimDetailPage.open(appUrl(""), scenario.validSubmissionId(), scenario.validClaimId());
    claimDetailPage.waitForClaimDetail();

    claimDetailPage.assertFeeCalculationHeadings(expectedHeadingsFor(scenario));
  }

  private List<String> expectedHeadingsFor(
      uk.gov.justice.laa.bulkclaim.accessibility.helpers.AbstractAccessibilityTest.AreaScenario
          scenario) {
    return switch (scenario.areaOfLaw()) {
      case "Legal help" ->
          List.of(
              "Fixed Fee",
              "Net Profit Cost",
              "Net Disbursements",
              "Disbursement VAT",
              "Net Cost of Counsel",
              "Travel & Waiting Costs",
              "Adjourned Hearing Fee",
              "JR / Form Filling",
              "Detention Travel & Waiting Costs",
              "CMRH Telephone",
              "CMRH Oral",
              "Home Office Interview",
              "Substantive Hearing",
              "VAT");
      case "Crime lower" ->
          List.of(
              "Fixed Fee",
              "Net Profit Cost",
              "Net Disbursements",
              "Disbursement VAT",
              "Net Cost of Counsel",
              "Travel Costs",
              "Waiting Costs",
              "Adjourned Hearing Fee",
              "JR / Form Filling",
              "Detention Travel & Waiting Costs",
              "CMRH Telephone",
              "CMRH Oral",
              "Home Office Interview",
              "Substantive Hearing",
              "VAT");
      case "Mediation" ->
          List.of(
              "Fixed Fee",
              "Net Profit Cost",
              "Net Disbursements",
              "Disbursement VAT",
              "Net Cost of Counsel",
              "Adjourned Hearing Fee",
              "JR / Form Filling",
              "Detention Travel & Waiting Costs",
              "CMRH Telephone",
              "CMRH Oral",
              "Home Office Interview",
              "Substantive Hearing",
              "VAT");
      default ->
          throw new IllegalArgumentException("Unsupported area of law: " + scenario.areaOfLaw());
    };
  }
}


