package uk.gov.justice.laa.bulkclaim.accessibility.tests;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.accessibility.helpers.AbstractAccessibilityTest;
import uk.gov.justice.laa.bulkclaim.accessibility.helpers.AccessibilityAxeHelper;
import uk.gov.justice.laa.bulkclaim.accessibility.pages.LandingPage;

@DisplayName("Feature: Landing page accessibility checks")
class MainNavigationAccessibilityTest extends AbstractAccessibilityTest {

  @Test
  @DisplayName("Scenario: Landing page accessibility checks")
  void landingPageAccessibilityChecks() throws IOException {
    new LandingPage(page).open(appUrl(""));
    AccessibilityAxeHelper.assertAccessible(page, "landing-page", List.of());
  }
}
