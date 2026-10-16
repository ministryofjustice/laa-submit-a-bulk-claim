package uk.gov.justice.laa.payments.submit.accessibility.tests;

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
    AccessibilityAxeHelper.assertAccessible(page, "logged-out");
  }
}
