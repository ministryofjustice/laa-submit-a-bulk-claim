package uk.gov.justice.laa.payments.submit.accessibility.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.payments.submit.accessibility.helpers.AbstractAccessibilityTest;
import uk.gov.justice.laa.payments.submit.accessibility.helpers.AccessibilityAxeHelper;

@DisplayName("Feature: Logged out page accessibility checks")
class LoggedOutAccessibilityTest extends AbstractAccessibilityTest {

  @Test
  @DisplayName("Scenario: Logged out page accessibility checks")
  void loggedOutPageAccessibilityChecks() throws IOException {
    page.navigate(appUrl("/logged-out"));
    assertThat(page.locator("text=Sign out")).hasCount(0);
    assertThat(page.locator("nav[aria-label='Organisation switcher']")).hasCount(0);
    AccessibilityAxeHelper.assertAccessible(page, "logged-out");
  }
}
