package uk.gov.justice.laa.payments.submit.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.justice.laa.payments.submit.e2e.base.BaseTest;
import uk.gov.justice.laa.payments.submit.e2e.pages.LandingPagePage;
import uk.gov.justice.laa.payments.submit.e2e.pages.SubmissionErrorsPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.UploadBeingCheckedPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.UploadPage;

public class ValidationMessagesE2ETest extends BaseTest {

  private static final String FIXTURES = "src/test/resources/validation/";

  @ParameterizedTest(name = "{0}")
  @MethodSource("claimErrorFiles")
  public void claimErrorsAreDisplayed(String displayName, String fixture) {
    var errorsPage = uploadAndWaitForErrors(fixture);

    assertThat(errorsPage.getFailureBannerHeading())
        .hasText("1 claim has errors for missing or incorrect information");
    assertThat(errorsPage.getFailureBanner())
        .containsText("Resolve the errors and upload the file again.");
    assertThat(errorsPage.getStatusTag()).hasText("Rejected");
    assertThat(errorsPage.getClaimErrorsHeading()).containsText("claim errors");
    assertThat(
            errorsPage.getClaimErrorRows().filter(hasText("Net Disbursement Amount is required")))
        .hasCount(1);
    assertThat(errorsPage.getSubmissionErrorsHeading()).hasCount(0);
  }

  @Test
  public void submissionErrorIsDisplayed() {
    var errorsPage = uploadAndWaitForErrors("submission-period-future-month.csv");

    assertThat(errorsPage.getFailureBannerHeading())
        .hasText("1 error was found with your submission");
    assertThat(errorsPage.getFailureBanner())
        .containsText("Resolve the errors and upload the file again.");
    assertThat(errorsPage.getStatusTag()).hasText("Rejected");
    assertThat(errorsPage.getSubmissionErrorsHeading()).hasText("1 submission error");
    assertThat(errorsPage.getSubmissionErrorRows())
        .containsText("are not accepted. Please submit for a previous month.");
    assertThat(errorsPage.getClaimErrorsHeading()).hasCount(0);
  }

  @Test
  public void fileErrorReturnedOnUploadIsDisplayed() {
    var upload = openUploadPage();
    upload.uploadFile(Paths.get(FIXTURES + "invalid-area-of-law.csv").toAbsolutePath());
    upload.getContinueButton().click();

    assertThat(upload.getErrorSummary()).isVisible();
    assertThat(upload.getErrorSummary())
        .containsText("Area of law must be Mediation, Crime Lower or Legal Help");
    assertThat(upload.getErrorMessage())
        .containsText("Error: Area of law must be Mediation, Crime Lower or Legal Help");
  }

  private SubmissionErrorsPage uploadAndWaitForErrors(String fixture) {
    var upload = openUploadPage();
    upload.uploadFile(Paths.get(FIXTURES + fixture).toAbsolutePath());
    upload.getContinueButton().click();

    new UploadBeingCheckedPage(page);

    return new SubmissionErrorsPage(page);
  }

  private UploadPage openUploadPage() {
    new LandingPagePage(page).getStartNowButton().click();
    return new UploadPage(page);
  }

  private static Locator.FilterOptions hasText(String text) {
    return new Locator.FilterOptions().setHasText(text);
  }

  private static Stream<Arguments> claimErrorFiles() {
    return Stream.of(
        Arguments.of("Crime Lower", "crime-lower-missing-fields.csv"),
        Arguments.of("Legal Help", "legal-help-missing-fields.csv"),
        Arguments.of("Mediation", "mediation-missing-fields.csv"));
  }
}
