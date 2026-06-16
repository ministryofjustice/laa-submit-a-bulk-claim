package uk.gov.justice.laa.bulkclaim.ui.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.justice.laa.bulkclaim.ui.helpers.AbstractUiTest;
import uk.gov.justice.laa.bulkclaim.ui.helpers.UiWiremockSupport;
import uk.gov.justice.laa.bulkclaim.ui.pages.SubmissionSummaryPage;

@DisplayName("Feature: Export submission via UI")
@TestInstance(Lifecycle.PER_CLASS)
class ExportUiTest extends AbstractUiTest {

  private SubmissionSummaryPage submissionSummaryPage;

  @BeforeAll
  void registerExportStubs() {
    UiWiremockSupport.registerExportStubs(WIREMOCK, LEGAL_HELP);
    UiWiremockSupport.registerExportStubs(WIREMOCK, MEDIATION);
    UiWiremockSupport.registerExportStubs(WIREMOCK, CRIME_LOWER);
  }

  @BeforeEach
  void setUp() {
    submissionSummaryPage = new SubmissionSummaryPage(page);
  }

  @ParameterizedTest(name = "Scenario: Successful export of submission for {0}")
  @MethodSource("allAreaArguments")
  void shouldExportAcceptedSubmission(String areaOfLawAbbr, AreaScenario scenario) throws IOException {
    assertTrue(
        areaOfLawAbbr != null && !areaOfLawAbbr.isBlank(),
        "Expected area abbreviation to be present for scenario " + scenario.areaOfLaw());

    openSubmissionDetail(scenario);
    submissionSummaryPage.waitForSummaryPage();
    submissionSummaryPage.assertAreaOfLaw(scenario.areaOfLaw());
    submissionSummaryPage.assertExportVisible();

    var download = submissionSummaryPage.downloadExport();
    String expectedFilename = expectedExportFilename(scenario);
    assertEquals(expectedFilename, download.suggestedFilename());

    Path downloadPath = Path.of("build", "tmp", "ui", "exports", expectedFilename);
    Files.createDirectories(downloadPath.getParent());
    download.saveAs(downloadPath);

    String csv = Files.readString(downloadPath);
    assertTrue(csv.contains(expectedExportMarker(scenario)), "Expected CSV to contain area-specific export header");
    assertTrue(csv.contains(scenario.apiAreaOfLaw()), "Expected CSV to contain the area of law value");
  }

  @ParameterizedTest(name = "Scenario: Should not be able to export when failed submission for {0}")
  @MethodSource("allAreaArguments")
  void shouldNotAllowExportForFailedSubmission(String areaOfLawAbbr, AreaScenario scenario) {
    assertTrue(
        areaOfLawAbbr != null && !areaOfLawAbbr.isBlank(),
        "Expected area abbreviation to be present for scenario " + scenario.areaOfLaw());

    openInvalidSubmissionDetail(scenario);
    submissionSummaryPage.waitForSummaryPage();
    submissionSummaryPage.assertAreaOfLaw(scenario.areaOfLaw());
    submissionSummaryPage.assertExportHidden();
  }

  private String expectedExportFilename(AreaScenario scenario) {
    return "submission-claims-%s-%s-0P322F-2025-05-01.csv"
        .formatted(scenario.areaOfLaw().toLowerCase().replace(" ", "-"), scenario.validSubmissionId());
  }

  private String expectedExportMarker(AreaScenario scenario) {
    return switch (scenario.areaOfLaw()) {
      case "Legal help" -> "Legal Help Submission Reference";
      case "Mediation" -> "Mediation Submission Reference";
      case "Crime lower" -> "Crime Lower Schedule Number";
      default -> throw new IllegalArgumentException("Unsupported area of law: " + scenario.areaOfLaw());
    };
  }
}
