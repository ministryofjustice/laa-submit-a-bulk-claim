package uk.gov.justice.laa.payments.submit.e2e.tests;


import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.payments.submit.e2e.base.BaseTest;
import uk.gov.justice.laa.payments.submit.e2e.pages.LandingPagePage;
import uk.gov.justice.laa.payments.submit.e2e.pages.SearchPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.SubmissionDetailPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.UploadBeingCheckedPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.UploadPage;

class SubmissionDetailsE2ETest extends BaseTest {

  @Test
  void legalHelpSubmissionSuccessful() {
    var landingPage = new LandingPagePage(page);
    landingPage.getStartNowButton().click();

    var uploadPage = new UploadPage(page);
    var csvPath = Paths.get("../docs/sample-data/legal-help-apr-2026-ms.csv").toAbsolutePath();
    uploadPage.uploadFile(csvPath);
    uploadPage.getContinueButton().click();

    new UploadBeingCheckedPage(page);

    var submissionDetailPage = new SubmissionDetailPage(page);

    // Checks that submission details page is reached via upload. Will validate page content
    //  after searching for submission.
    submissionDetailPage.assertSubmissionAccepted();

    // Next search for the same submission
    uploadPage.getSearchLink().click();
    var searchPage = new SearchPage(page);
    searchPage.getSearchButton().click();

    // View the newly submitted submission
    searchPage.clickOnLink(0);

    var submissionDetails = new SubmissionDetailPage(page);
    // Assert basic summary details
    submissionDetails.assertSubmissionAccepted();
    submissionDetailPage.assertTotalWarnings(9);
    submissionDetails.assertSubmissionSummary(
        "0P322F", "Legal help", "APR-2026", "£33,115.60");

    // Assert tabs are visible
    assertThat(submissionDetails.getClaimsTab()).isVisible();
    assertThat(submissionDetails.getMessagesTab()).isVisible();
    assertThat(submissionDetails.getMatterStartsTab()).isVisible();

    // Assert claims tab
    submissionDetails.assertTotalClaims(9);

    // Assert messages tab
    submissionDetails.getMessagesTab().click();
    submissionDetails.assertTotalMessages(9);

    // Assert matter starts tab
    submissionDetails.getMatterStartsTab().click();
    submissionDetails.assertTotalMatterStarts(2);
  }

  @Test
  void crimeLowerSubmissionSuccessful() {
    var landingPage = new LandingPagePage(page);
    landingPage.getStartNowButton().click();

    var uploadPage = new UploadPage(page);
    var csvPath = Paths.get("../docs/sample-data/crime-lower-june-2026.csv").toAbsolutePath();
    uploadPage.uploadFile(csvPath);
    uploadPage.getContinueButton().click();

    new UploadBeingCheckedPage(page);

    var submissionDetailPage = new SubmissionDetailPage(page);

    // Checks that submission details page is reached via upload. Will validate page content
    //  after searching for submission.
    submissionDetailPage.assertSubmissionAccepted();

    // Next search for the same submission
    uploadPage.getSearchLink().click();
    var searchPage = new SearchPage(page);
    searchPage.getSearchButton().click();

    // View the newly submitted submission
    searchPage.clickOnLink(0);

    var submissionDetails = new SubmissionDetailPage(page);
    // Assert basic summary details
    submissionDetails.assertSubmissionAccepted();
    submissionDetailPage.assertTotalWarnings(5);
    submissionDetails.assertSubmissionSummary(
        "0P322F", "Crime lower", "JUN-2026", "£5,465.47");

    // Assert tabs are visible
    assertThat(submissionDetails.getClaimsTab()).isVisible();
    assertThat(submissionDetails.getMessagesTab()).isVisible();
    assertThat(submissionDetails.getMatterStartsTab()).isHidden();

    // Assert claims tab
    submissionDetails.assertTotalClaims(10);

    // Assert messages tab
    submissionDetails.getMessagesTab().click();
    submissionDetails.assertTotalMessages(5);
  }
}
