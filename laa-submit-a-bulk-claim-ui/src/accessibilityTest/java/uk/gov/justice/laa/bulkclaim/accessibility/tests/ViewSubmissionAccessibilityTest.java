package uk.gov.justice.laa.bulkclaim.accessibility.tests;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.justice.laa.bulkclaim.accessibility.helpers.AbstractAccessibilityTest;
import uk.gov.justice.laa.bulkclaim.accessibility.helpers.AccessibilityAxeHelper;
import uk.gov.justice.laa.bulkclaim.accessibility.pages.SubmissionDetailPage;

@DisplayName("Feature: View submission details page (VS)")
class ViewSubmissionAccessibilityTest extends AbstractAccessibilityTest {

  private SubmissionDetailPage submissionDetailPage;

  @BeforeEach
  void setUpPageObject() {
    submissionDetailPage = new SubmissionDetailPage(page);
  }

  @ParameterizedTest(name = "Scenario: VS1-{0} accessibility checks")
  @MethodSource("allAreaArguments")
  void viewSubmissionAccessibilityChecks(String areaOfLawAbbr, AreaScenario scenario)
      throws IOException {
    openSubmissionDetail(scenario);
    AccessibilityAxeHelper.assertAccessible(
        page, areaScenarioName("submission-detail", areaOfLawAbbr, "claims"));
  }

  @ParameterizedTest(name = "Scenario: VS2-{0} accessibility checks")
  @MethodSource("allAreaArguments")
  void viewSubmissionMessagesAccessibilityChecks(String areaOfLawAbbr, AreaScenario scenario)
      throws IOException {
    openSubmissionDetail(scenario);
    submissionDetailPage.openMessagesTab();
    AccessibilityAxeHelper.assertAccessible(
        page, areaScenarioName("submission-detail", areaOfLawAbbr, "messages"));
  }

  @ParameterizedTest(name = "Scenario: VS3-{0}-EMP accessibility checks")
  @MethodSource("matterStartAreaArguments")
  void viewSubmissionMatterStartsAccessibilityChecks(String areaOfLawAbbr, AreaScenario scenario)
      throws IOException {
    openSubmissionDetail(scenario);
    submissionDetailPage.openMatterStartsTab();
    AccessibilityAxeHelper.assertAccessible(
        page, areaScenarioName("submission-detail", areaOfLawAbbr, "matter-starts-emp"));
  }

  @ParameterizedTest(name = "Scenario: VS3-{0} accessibility checks")
  @MethodSource("matterStartAreaArguments")
  void viewSubmissionMatterStartsAllMatterTypeAccessibilityChecks(
      String areaOfLawAbbr, AreaScenario scenario) throws IOException {
    openSubmissionDetail(scenario);
    submissionDetailPage.openMatterStartsTab();
    AccessibilityAxeHelper.assertAccessible(
        page, areaScenarioName("submission-detail", areaOfLawAbbr, "matter-starts"));
  }

  @ParameterizedTest(name = "Scenario: VS1-{0}-CW accessibility checks")
  @MethodSource("costWarningAreaArguments")
  void viewSubmissionCostWarningAccessibilityChecks(String areaOfLawAbbr, AreaScenario scenario)
      throws IOException {
    openSubmissionDetail(scenario);
    AccessibilityAxeHelper.assertAccessible(
        page, areaScenarioName("submission-detail", areaOfLawAbbr, "claims-cw"));
  }

  @ParameterizedTest(name = "Scenario: VS2-{0}-CW accessibility checks")
  @MethodSource("costWarningAreaArguments")
  void viewSubmissionMessagesCostWarningAccessibilityChecks(
      String areaOfLawAbbr, AreaScenario scenario) throws IOException {
    openSubmissionDetail(scenario);
    submissionDetailPage.openMessagesTab();
    AccessibilityAxeHelper.assertAccessible(
        page, areaScenarioName("submission-detail", areaOfLawAbbr, "messages-cw"));
  }

  @ParameterizedTest(name = "Scenario: VS3-{0}-EMP-CW accessibility checks")
  @MethodSource("legalHelpArguments")
  void viewSubmissionMatterStartsCostWarningAccessibilityChecks(
      String areaOfLawAbbr, AreaScenario scenario) throws IOException {
    openSubmissionDetail(scenario);
    submissionDetailPage.openMatterStartsTab();
    AccessibilityAxeHelper.assertAccessible(
        page, areaScenarioName("submission-detail", areaOfLawAbbr, "matter-starts-emp-cw"));
  }

  @ParameterizedTest(name = "Scenario: VS1-{0}-SE accessibility checks")
  @MethodSource("allAreaArguments")
  void invalidSubmissionAccessibilityChecks(String areaOfLawAbbr, AreaScenario scenario)
      throws IOException {
    openInvalidSubmissionDetail(scenario);
    AccessibilityAxeHelper.assertAccessible(
        page, areaScenarioName("submission-detail", areaOfLawAbbr, "se"));
  }

  @ParameterizedTest(name = "Scenario: VS1-{0}-CE accessibility checks")
  @MethodSource("allAreaArguments")
  void claimErrorSubmissionAccessibilityChecks(String areaOfLawAbbr, AreaScenario scenario)
      throws IOException {
    openInvalidSubmissionDetail(scenario);
    AccessibilityAxeHelper.assertAccessible(
        page, areaScenarioName("submission-detail", areaOfLawAbbr, "ce"));
  }
}
