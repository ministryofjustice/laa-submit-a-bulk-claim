package uk.gov.justice.laa.bulkclaim.accessibility.tests;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.accessibility.helpers.AbstractAccessibilityTest;
import uk.gov.justice.laa.bulkclaim.accessibility.helpers.AccessibilityAxeHelper;
import uk.gov.justice.laa.bulkclaim.accessibility.pages.SubmissionSearchPage;

@DisplayName("Feature: Submission search page (SS)")
class SubmissionSearchAccessibilityTest extends AbstractAccessibilityTest {

  private static final List<String> SEARCH_RULE_IGNORES = List.of("aria-valid-attr-value");
  private SubmissionSearchPage submissionSearchPage;

  @BeforeEach
  void setUpPageObject() {
    submissionSearchPage = new SubmissionSearchPage(page);
  }

  @Test
  @DisplayName("Scenario: SS1 accessibility checks")
  void searchPageAccessibilityChecks() throws IOException {
    submissionSearchPage.openSearch(appUrl(""));
    AccessibilityAxeHelper.assertAccessible(page, "submission-search-page", SEARCH_RULE_IGNORES);
  }

  @Test
  @DisplayName("Scenario: SS2 accessibility checks")
  void searchResultsAccessibilityChecks() throws IOException {
    submissionSearchPage.openSearchResults(appUrl(""));
    AccessibilityAxeHelper.assertAccessible(page, "submission-search-results", SEARCH_RULE_IGNORES);
  }
}
