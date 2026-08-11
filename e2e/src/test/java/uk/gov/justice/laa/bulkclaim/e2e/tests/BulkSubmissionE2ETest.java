package uk.gov.justice.laa.bulkclaim.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.justice.laa.bulkclaim.e2e.base.BaseTest;
import uk.gov.justice.laa.bulkclaim.e2e.pages.LandingPagePage;
import uk.gov.justice.laa.bulkclaim.e2e.pages.SubmissionDetailPage;
import uk.gov.justice.laa.bulkclaim.e2e.pages.SubmissionErrorsPage;
import uk.gov.justice.laa.bulkclaim.e2e.pages.UploadBeingCheckedPage;
import uk.gov.justice.laa.bulkclaim.e2e.pages.UploadPage;

public class BulkSubmissionE2ETest extends BaseTest {

  @ParameterizedTest(name = "{0}")
  @MethodSource("csvFiles")
  public void bulkSubmissionAccepted(String displayName, String csvFile) {
    var landingPage = new LandingPagePage(page);
    landingPage.getStartNowButton().click();

    var upload = new UploadPage(page);
    var csvPath = Paths.get(csvFile).toAbsolutePath();
    upload.uploadFile(csvPath);

    upload.getContinueButton().click();

    var submissionDetailPage = new SubmissionDetailPage(page);
    submissionDetailPage.assertSubmissionAccepted();
  }

  @Test
  public void bulkSubmissionForCrimeRejected() {
    var landingPage = new LandingPagePage(page);
    landingPage.getStartNowButton().click();

    var upload = new UploadPage(page);
    var csvPath =
        Paths.get("../docs/sample-data/invalid-crime-lower-november-2025.csv").toAbsolutePath();
    upload.uploadFile(csvPath);

    upload.getContinueButton().click();

    var submissionErrorsPage = new SubmissionErrorsPage(page);
    assertThat(submissionErrorsPage.getFailureBanner()).isVisible();
    assertThat(submissionErrorsPage.getFailureBanner())
        .containsText("Resolve the errors and upload the file again.");
  }

  private static Stream<Arguments> csvFiles() {
    return Stream.of(
        Arguments.of("Crime Lower", "../docs/sample-data/crime-lower-may-2026.csv"),
        Arguments.of("Legal Help", "../docs/sample-data/legal-help-june-2026.csv"),
        Arguments.of("Mediation", "../docs/sample-data/mediation-june-2026.csv"));
  }

}
