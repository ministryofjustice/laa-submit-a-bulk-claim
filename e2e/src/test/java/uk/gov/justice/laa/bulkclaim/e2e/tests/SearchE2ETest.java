package uk.gov.justice.laa.bulkclaim.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.e2e.base.BaseTest;
import uk.gov.justice.laa.bulkclaim.e2e.pages.*;

public class SearchE2ETest extends BaseTest {

  @Test
  public void searchSubmissionFlow() {
    var landingPage = new LandingPagePage(page);
    landingPage.getStartNowButton().click();

    var uploadPage = new UploadPage(page);
    var csvPath = Paths.get("../docs/sample-data/crime-lower-may-2026.csv").toAbsolutePath();
    uploadPage.uploadFile(csvPath);
    uploadPage.getContinueButton().click();

    new UploadBeingCheckedPage(page);

    var submissionDetailPage = new SubmissionDetailPage(page);
    submissionDetailPage.assertSubmissionAccepted();

    uploadPage.getSearchLink().click();

    var searchPage = new SearchPage(page);
    searchPage.clickSearch();

    assertThat(searchPage.getResultsTable()).isVisible();

    assertTableContainsHeaders("Date submitted", "Office account", "Area of law", "Submission period", "Status"
    );

  }
}
