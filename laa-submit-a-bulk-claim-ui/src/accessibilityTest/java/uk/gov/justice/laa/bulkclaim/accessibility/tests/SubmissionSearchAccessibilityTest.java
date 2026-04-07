package uk.gov.justice.laa.bulkclaim.accessibility.tests;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.accessibility.helpers.AbstractAccessibilityTest;
import uk.gov.justice.laa.bulkclaim.accessibility.helpers.AccessibilityAxeHelper;
import uk.gov.justice.laa.bulkclaim.accessibility.pages.SubmissionSearchPage;

@DisplayName("Feature: Submission search page (SS)")
class SubmissionSearchAccessibilityTest extends AbstractAccessibilityTest {

  @Test
  @DisplayName("Scenario: SS1 accessibility checks")
  void searchPageAccessibilityChecks() throws IOException {
    new SubmissionSearchPage(page).openSearch(appUrl(""));
    AccessibilityAxeHelper.assertAccessible(
        page, "submission-search-page", List.of("aria-valid-attr-value"));
  }

  @Test
  @DisplayName("Scenario: SS2 accessibility checks")
  void searchResultsAccessibilityChecks() throws IOException {
    new SubmissionSearchPage(page).openSearchResults(appUrl(""));
    AccessibilityAxeHelper.assertAccessible(
        page, "submission-search-results", List.of("aria-valid-attr-value"));
  }
}
