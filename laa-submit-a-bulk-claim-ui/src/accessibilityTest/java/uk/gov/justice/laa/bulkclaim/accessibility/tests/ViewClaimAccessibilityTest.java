package uk.gov.justice.laa.bulkclaim.accessibility.tests;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.justice.laa.bulkclaim.accessibility.helpers.AbstractAccessibilityTest;
import uk.gov.justice.laa.bulkclaim.accessibility.helpers.AccessibilityAxeHelper;
import uk.gov.justice.laa.bulkclaim.accessibility.pages.ClaimDetailPage;

@DisplayName("Feature: View claim details page (VC)")
class ViewClaimAccessibilityTest extends AbstractAccessibilityTest {

  @ParameterizedTest(name = "Scenario: VC1-{0} accessibility checks")
  @MethodSource("allAreaArguments")
  void viewClaimAccessibilityChecks(String areaOfLawAbbr, AreaScenario scenario)
      throws IOException {
    new ClaimDetailPage(page)
        .open(appUrl(""), scenario.validSubmissionId(), scenario.validClaimId());
    AccessibilityAxeHelper.assertAccessible(page, "claim-detail-" + areaOfLawAbbr, List.of());
  }

  @ParameterizedTest(name = "Scenario: VC1-{0}-CW accessibility checks")
  @MethodSource("costWarningAreaArguments")
  void viewClaimCostWarningAccessibilityChecks(String areaOfLawAbbr, AreaScenario scenario)
      throws IOException {
    new ClaimDetailPage(page)
        .open(appUrl(""), scenario.validSubmissionId(), scenario.validClaimId());
    AccessibilityAxeHelper.assertAccessible(
        page, "claim-detail-" + areaOfLawAbbr + "-cw", List.of());
  }

  private static Stream<Arguments> allAreaArguments() {
    return allAreas().map(scenario -> Arguments.of(scenario.areaOfLawAbbr(), scenario));
  }

  private static Stream<Arguments> costWarningAreaArguments() {
    return costWarningAreas().map(scenario -> Arguments.of(scenario.areaOfLawAbbr(), scenario));
  }
}
