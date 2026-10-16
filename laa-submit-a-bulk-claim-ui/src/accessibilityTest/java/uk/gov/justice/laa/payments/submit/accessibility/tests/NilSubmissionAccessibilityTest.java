package uk.gov.justice.laa.payments.submit.accessibility.tests;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

  @Test
  @DisplayName("Scenario: Submission period page accessibility checks")
  void periodPageAccessibilityChecks() throws IOException {
    nilSubmissionPage.openOffice();
    nilSubmissionPage.selectOffice("0P322F");
    nilSubmissionPage.selectAreaOfLaw("LEGAL HELP");
    AccessibilityAxeHelper.assertAccessible(page, "nil-submission-period");
  }

  @Test
  @DisplayName("Scenario: Submission reference page accessibility checks")
  void referencePageAccessibilityChecks() throws IOException {
    nilSubmissionPage.openOffice();
    nilSubmissionPage.selectOffice("0P322F");
    nilSubmissionPage.selectAreaOfLaw("LEGAL HELP");
    nilSubmissionPage.selectSubmissionPeriod();
    AccessibilityAxeHelper.assertAccessible(page, "nil-submission-reference");
  }

  @Test
  @DisplayName("Scenario: Summary details page accessibility checks")
  void summaryPageAccessibilityChecks() throws IOException {
    nilSubmissionPage.openOffice();
    nilSubmissionPage.selectOffice("0P322F");
    nilSubmissionPage.selectAreaOfLaw("LEGAL HELP");
    nilSubmissionPage.selectSubmissionPeriod();
    nilSubmissionPage.enterReference("REF/123");
    AccessibilityAxeHelper.assertAccessible(page, "nil-submission-summary");
  }

  @Test
  @DisplayName("Scenario: No submission periods page accessibility checks")
  void noSubmissionPeriodsPageAccessibilityChecks() throws IOException {
    nilSubmissionPage.openOffice();
    nilSubmissionPage.selectOffice(FULLY_SUBMITTED_OFFICE);
    nilSubmissionPage.selectAreaOfLaw("LEGAL HELP");
    AccessibilityAxeHelper.assertAccessible(page, "nil-submission-no-submission-periods");
  }
}
