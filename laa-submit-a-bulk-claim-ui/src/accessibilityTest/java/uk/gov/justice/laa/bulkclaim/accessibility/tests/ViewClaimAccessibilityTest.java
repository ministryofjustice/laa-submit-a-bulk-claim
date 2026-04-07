package uk.gov.justice.laa.bulkclaim.accessibility.tests;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.justice.laa.bulkclaim.accessibility.helpers.AbstractAccessibilityTest;
import uk.gov.justice.laa.bulkclaim.accessibility.helpers.AccessibilityAxeHelper;
import uk.gov.justice.laa.bulkclaim.accessibility.pages.ClaimDetailPage;

@DisplayName("Feature: View claim details page (VC)")
class ViewClaimAccessibilityTest extends AbstractAccessibilityTest {

  private ClaimDetailPage claimDetailPage;

  @BeforeEach
  void setUpPageObject() {
    claimDetailPage = new ClaimDetailPage(page);
  }

  @ParameterizedTest(name = "Scenario: VC1-{0} accessibility checks")
  @MethodSource("allAreaArguments")
  void viewClaimAccessibilityChecks(String areaOfLawAbbr, AreaScenario scenario)
      throws IOException {
    claimDetailPage.open(appUrl(""), scenario.validSubmissionId(), scenario.validClaimId());
    AccessibilityAxeHelper.assertAccessible(
        page, areaScenarioName("claim-detail", areaOfLawAbbr, null));
  }

  @ParameterizedTest(name = "Scenario: VC1-{0}-CW accessibility checks")
  @MethodSource("costWarningAreaArguments")
  void viewClaimCostWarningAccessibilityChecks(String areaOfLawAbbr, AreaScenario scenario)
      throws IOException {
    claimDetailPage.open(appUrl(""), scenario.validSubmissionId(), scenario.validClaimId());
    AccessibilityAxeHelper.assertAccessible(
        page, areaScenarioName("claim-detail", areaOfLawAbbr, "cw"));
  }
}
