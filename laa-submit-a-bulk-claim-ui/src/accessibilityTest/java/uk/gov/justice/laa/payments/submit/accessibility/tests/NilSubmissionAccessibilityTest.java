package uk.gov.justice.laa.payments.submit.accessibility.tests;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.justice.laa.payments.submit.accessibility.helpers.AbstractAccessibilityTest;
import uk.gov.justice.laa.payments.submit.accessibility.helpers.AccessibilityAxeHelper;
import uk.gov.justice.laa.payments.submit.accessibility.pages.NilSubmissionPage;

@DisplayName("Feature: Nil submission journey accessibility checks")
class NilSubmissionAccessibilityTest extends AbstractAccessibilityTest {

  private NilSubmissionPage nilSubmissionPage;

  @BeforeEach
  void setUpPageObject() {
    nilSubmissionPage = new NilSubmissionPage(page, appUrl(""));
  }

  @Test
  @DisplayName("Scenario: Office page accessibility checks")
  void officePageAccessibilityChecks() throws IOException {
    nilSubmissionPage.openOffice();
    AccessibilityAxeHelper.assertAccessible(page, "nil-submission-office");
  }

  @Test
  @DisplayName("Scenario: Area of law page accessibility checks")
  void areaOfLawPageAccessibilityChecks() throws IOException {
    nilSubmissionPage.openOffice();
    nilSubmissionPage.selectOffice("0P322F");
    AccessibilityAxeHelper.assertAccessible(page, "nil-submission-area-of-law");
  }

  @ParameterizedTest(name = "Scenario: NS3-{0} accessibility checks")
  @MethodSource("allAreaArguments")
  void periodPageAccessibilityChecks(String areaOfLawAbbr, AreaScenario scenario)
      throws IOException {
    nilSubmissionPage.openOffice();
    nilSubmissionPage.selectOffice("0P322F");
    nilSubmissionPage.selectAreaOfLaw(scenario.apiAreaOfLaw());
    AccessibilityAxeHelper.assertAccessible(
        page, areaScenarioName("nil-submission-period", areaOfLawAbbr, null));
  }

  @ParameterizedTest(name = "Scenario: NS4-{0} accessibility checks")
  @MethodSource("allAreaArguments")
  void referencePageAccessibilityChecks(String areaOfLawAbbr, AreaScenario scenario)
      throws IOException {
    nilSubmissionPage.openOffice();
    nilSubmissionPage.selectOffice("0P322F");
    nilSubmissionPage.selectAreaOfLaw(scenario.apiAreaOfLaw());
    nilSubmissionPage.selectSubmissionPeriod();
    AccessibilityAxeHelper.assertAccessible(
        page, areaScenarioName("nil-submission-reference", areaOfLawAbbr, null));
  }

  @ParameterizedTest(name = "Scenario: NS5-{0} accessibility checks")
  @MethodSource("allAreaArguments")
  void summaryPageAccessibilityChecks(String areaOfLawAbbr, AreaScenario scenario)
      throws IOException {
    nilSubmissionPage.openOffice();
    nilSubmissionPage.selectOffice("0P322F");
    nilSubmissionPage.selectAreaOfLaw(scenario.apiAreaOfLaw());
    nilSubmissionPage.selectSubmissionPeriod();
    nilSubmissionPage.enterReference("REF/123");
    AccessibilityAxeHelper.assertAccessible(
        page, areaScenarioName("nil-submission-summary", areaOfLawAbbr, null));
  }

  @ParameterizedTest(name = "Scenario: NS6-{0} accessibility checks")
  @MethodSource("allAreaArguments")
  void noSubmissionPeriodsPageAccessibilityChecks(String areaOfLawAbbr, AreaScenario scenario)
      throws IOException {
    nilSubmissionPage.openOffice();
    nilSubmissionPage.selectOffice(FULLY_SUBMITTED_OFFICE);
    nilSubmissionPage.selectAreaOfLaw(scenario.apiAreaOfLaw());
    AccessibilityAxeHelper.assertAccessible(
        page, areaScenarioName("nil-submission-no-submission-periods", areaOfLawAbbr, null));
  }
}
