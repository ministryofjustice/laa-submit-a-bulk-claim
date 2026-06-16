package uk.gov.justice.laa.bulkclaim.ui.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.justice.laa.bulkclaim.ui.helpers.AbstractUiTest;
import uk.gov.justice.laa.bulkclaim.ui.helpers.UiWiremockSupport;
import uk.gov.justice.laa.bulkclaim.ui.pages.SubmissionSearchPage;

@DisplayName("Feature: Search submissions via UI")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SearchUiTest extends AbstractUiTest {

  private SubmissionSearchPage searchPage;

  @BeforeAll
  void registerSearchStubs() {
    UiWiremockSupport.registerSearchStubs(WIREMOCK);
  }

  @BeforeEach
  void setUp() {
    searchPage = new SubmissionSearchPage(page);
  }

  @Test
  @DisplayName("Scenario: Search with no filter specified")
  void shouldShowResultsWhenSearchingWithDefaults() {
    searchPage.open(appUrl(""));

    searchPage.clickSearch();

    searchPage.assertResultsVisible();
    assertTrue(searchPage.getResultRowCount() > 0, "Expected default search to return rows");
  }

  @Test
  @DisplayName("Scenario: Search for submissions via all fields")
  void shouldFilterByAllFields() {
    searchPage.open(appUrl(""));
    searchPage.selectSubmissionPeriod("MAY-2025");
    searchPage.selectAreaOfLaw("LEGAL_HELP");
    searchPage.selectOutcomeFailed();
    searchPage.deselectAllOffices();
    searchPage.selectOffice("0P322F");

    searchPage.clickSearch();

    searchPage.assertResultsVisible();
    assertEquals(1, searchPage.getResultRowCount());
  }

  @Test
  @DisplayName("Scenario: Search for submissions via Submission Period")
  void shouldFilterBySubmissionPeriodOnly() {
    searchPage.open(appUrl(""));
    searchPage.selectSubmissionPeriod("MAY-2025");
    searchPage.deselectAllOffices();
    searchPage.selectOffice("0P322F");

    searchPage.clickSearch();

    searchPage.assertResultsVisible();
    assertEquals(2, searchPage.getResultRowCount());
  }

  @ParameterizedTest(name = "Scenario: Search for submissions via Area of Law for {0}")
  @MethodSource("allAreaArguments")
  @DisplayName("Scenario: Search for submissions via Area of Law")
  void shouldFilterByAreaOfLawOnly(
      String areaOfLawAbbr,
      uk.gov.justice.laa.bulkclaim.accessibility.helpers.AbstractAccessibilityTest.AreaScenario
          scenario) {
    searchPage.open(appUrl(""));
    searchPage.selectAreaOfLaw(scenario.apiAreaOfLaw().replace(" ", "_"));

    searchPage.clickSearch();

    searchPage.assertResultsVisible();
    assertEquals(
        1,
        searchPage.getResultRowCount(),
        "Expected one filtered result for area " + areaOfLawAbbr);
  }

  @Test
  @DisplayName("Scenario: Search for submissions via Submission Status")
  void shouldFilterBySubmissionStatusOnly() {
    searchPage.open(appUrl(""));
    searchPage.selectOutcomeFailed();

    searchPage.clickSearch();

    searchPage.assertResultsVisible();
    assertEquals(2, searchPage.getResultRowCount());
  }

  @Test
  @DisplayName("Scenario: Search for submissions via Office Account")
  void shouldFilterByOfficeOnly() {
    searchPage.open(appUrl(""));
    searchPage.deselectAllOffices();
    searchPage.selectOffice("2Q779P");

    searchPage.clickSearch();

    searchPage.assertResultsVisible();
    assertEquals(1, searchPage.getResultRowCount());
  }

  @Test
  @DisplayName("Scenario: Validate office is selected")
  void shouldShowValidationWhenNoOfficeSelected() {
    searchPage.open(appUrl(""));
    searchPage.deselectAllOffices();

    searchPage.clickSearch();

    searchPage.assertValidationMessage("Select an office code");
  }

  @Test
  @DisplayName("Scenario: Search with a past date range that returns no submissions")
  void shouldShowNoResultsMessageForPastDateRange() {
    searchPage.open(appUrl(""));
    searchPage.selectSubmissionPeriod("APR-2025");
    searchPage.deselectAllOffices();
    searchPage.selectOffice("0P322F");

    searchPage.clickSearch();

    searchPage.assertNoResultsMessage();
  }
}

