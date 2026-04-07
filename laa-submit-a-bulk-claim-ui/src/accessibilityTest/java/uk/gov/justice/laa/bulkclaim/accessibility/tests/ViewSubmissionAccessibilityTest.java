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
import uk.gov.justice.laa.bulkclaim.accessibility.pages.SubmissionDetailPage;

@DisplayName("Feature: View submission details page (VS)")
class ViewSubmissionAccessibilityTest extends AbstractAccessibilityTest {

  @ParameterizedTest(name = "Scenario: VS1-{0} accessibility checks")
  @MethodSource("allAreaArguments")
  void viewSubmissionAccessibilityChecks(String areaOfLawAbbr, AreaScenario scenario)
      throws IOException {
    openSubmissionDetail(scenario);
    AccessibilityAxeHelper.assertAccessible(
        page, "submission-detail-" + areaOfLawAbbr + "-claims", List.of());
  }

  @ParameterizedTest(name = "Scenario: VS2-{0} accessibility checks")
  @MethodSource("allAreaArguments")
  void viewSubmissionMessagesAccessibilityChecks(String areaOfLawAbbr, AreaScenario scenario)
      throws IOException {
    openSubmissionDetail(scenario);
    new SubmissionDetailPage(page).openMessagesTab();
    AccessibilityAxeHelper.assertAccessible(
        page, "submission-detail-" + areaOfLawAbbr + "-messages", List.of());
  }

  @ParameterizedTest(name = "Scenario: VS3-{0}-EMP accessibility checks")
  @MethodSource("matterStartAreaArguments")
  void viewSubmissionMatterStartsAccessibilityChecks(String areaOfLawAbbr, AreaScenario scenario)
      throws IOException {
    openSubmissionDetail(scenario);
    new SubmissionDetailPage(page).openMatterStartsTab();
    AccessibilityAxeHelper.assertAccessible(
        page, "submission-detail-" + areaOfLawAbbr + "-matter-starts-emp", List.of());
  }

  @ParameterizedTest(name = "Scenario: VS3-{0} accessibility checks")
  @MethodSource("matterStartAreaArguments")
  void viewSubmissionMatterStartsAllMatterTypeAccessibilityChecks(
      String areaOfLawAbbr, AreaScenario scenario) throws IOException {
    openSubmissionDetail(scenario);
    new SubmissionDetailPage(page).openMatterStartsTab();
    AccessibilityAxeHelper.assertAccessible(
        page, "submission-detail-" + areaOfLawAbbr + "-matter-starts", List.of());
  }

  @ParameterizedTest(name = "Scenario: VS1-{0}-CW accessibility checks")
  @MethodSource("costWarningAreaArguments")
  void viewSubmissionCostWarningAccessibilityChecks(String areaOfLawAbbr, AreaScenario scenario)
      throws IOException {
    openSubmissionDetail(scenario);
    AccessibilityAxeHelper.assertAccessible(
        page, "submission-detail-" + areaOfLawAbbr + "-claims-cw", List.of());
  }

  @ParameterizedTest(name = "Scenario: VS2-{0}-CW accessibility checks")
  @MethodSource("costWarningAreaArguments")
  void viewSubmissionMessagesCostWarningAccessibilityChecks(
      String areaOfLawAbbr, AreaScenario scenario) throws IOException {
    openSubmissionDetail(scenario);
    new SubmissionDetailPage(page).openMessagesTab();
    AccessibilityAxeHelper.assertAccessible(
        page, "submission-detail-" + areaOfLawAbbr + "-messages-cw", List.of());
  }

  @ParameterizedTest(name = "Scenario: VS3-{0}-EMP-CW accessibility checks")
  @MethodSource("legalHelpArguments")
  void viewSubmissionMatterStartsCostWarningAccessibilityChecks(
      String areaOfLawAbbr, AreaScenario scenario) throws IOException {
    openSubmissionDetail(scenario);
    new SubmissionDetailPage(page).openMatterStartsTab();
    AccessibilityAxeHelper.assertAccessible(
        page, "submission-detail-" + areaOfLawAbbr + "-matter-starts-emp-cw", List.of());
  }

  @ParameterizedTest(name = "Scenario: VS1-{0}-SE accessibility checks")
  @MethodSource("allAreaArguments")
  void invalidSubmissionAccessibilityChecks(String areaOfLawAbbr, AreaScenario scenario)
      throws IOException {
    openInvalidSubmissionDetail(scenario);
    AccessibilityAxeHelper.assertAccessible(
        page, "submission-detail-" + areaOfLawAbbr + "-se", List.of());
  }

  @ParameterizedTest(name = "Scenario: VS1-{0}-CE accessibility checks")
  @MethodSource("allAreaArguments")
  void claimErrorSubmissionAccessibilityChecks(String areaOfLawAbbr, AreaScenario scenario)
      throws IOException {
    openInvalidSubmissionDetail(scenario);
    AccessibilityAxeHelper.assertAccessible(
        page, "submission-detail-" + areaOfLawAbbr + "-ce", List.of());
  }

  private static Stream<Arguments> allAreaArguments() {
    return allAreas().map(scenario -> Arguments.of(scenario.areaOfLawAbbr(), scenario));
  }

  private static Stream<Arguments> costWarningAreaArguments() {
    return costWarningAreas().map(scenario -> Arguments.of(scenario.areaOfLawAbbr(), scenario));
  }

  private static Stream<Arguments> matterStartAreaArguments() {
    return matterStartAreas().map(scenario -> Arguments.of(scenario.areaOfLawAbbr(), scenario));
  }

  private static Stream<Arguments> legalHelpArguments() {
    return Stream.of(Arguments.of(LEGAL_HELP.areaOfLawAbbr(), LEGAL_HELP));
  }
}
