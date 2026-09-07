package uk.gov.justice.laa.payments.submit.e2e.tests;


import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.payments.submit.e2e.base.DbUnitBaseTest;
import uk.gov.justice.laa.payments.submit.e2e.pages.LandingPagePage;
import uk.gov.justice.laa.payments.submit.e2e.pages.SearchPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.SubmissionDetailPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.UploadPage;

class SubmissionDetailsE2ETest extends DbUnitBaseTest {

  @Override
  protected String getDataSetPath() {
    return "datasets/submission_details.xml";
  }

  @Test
  void legalHelpSubmissionSuccessful() {
    var landingPage = new LandingPagePage(page);
    landingPage.getStartNowButton().click();

    // Start user journey to make search button visible
    var uploadPage = new UploadPage(page);

    // Next search for the same submission
    uploadPage.getSearchLink().click();
    var searchPage = new SearchPage(page);
    searchPage.getAreaOfLawSelect().selectOption("Legal help");
    searchPage.getSearchButton().click();

    // Click first option
    searchPage.clickOnLink(0);

    var submissionDetails = new SubmissionDetailPage(page);
    // Assert basic summary details
    submissionDetails.assertSubmissionAccepted();
    submissionDetails.assertTotalWarnings(9);
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

    // Start user journey to make search button visible
    var uploadPage = new UploadPage(page);

    // Next search for the same submission
    uploadPage.getSearchLink().click();
    var searchPage = new SearchPage(page);
    searchPage.getAreaOfLawSelect().selectOption("Crime lower");
    searchPage.getSearchButton().click();

    // Click first option
    searchPage.clickOnLink(0);

    var submissionDetails = new SubmissionDetailPage(page);
    // Assert basic summary details
    submissionDetails.assertSubmissionAccepted();
    submissionDetails.assertTotalWarnings(5);
    submissionDetails.assertSubmissionSummary(
        "0P322F", "Crime lower", "JUN-2026", "£5,465.50");

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

  @Test
  void mediationSubmissionSuccessful() {
    var landingPage = new LandingPagePage(page);
    landingPage.getStartNowButton().click();

    // Start user journey to make search button visible
    var uploadPage = new UploadPage(page);

    // Next search for the same submission
    uploadPage.getSearchLink().click();
    var searchPage = new SearchPage(page);
    searchPage.getAreaOfLawSelect().selectOption("Mediation");
    searchPage.getSearchButton().click();

    // Click first option
    searchPage.clickOnLink(0);

    var submissionDetails = new SubmissionDetailPage(page);
    // Assert basic summary details
    submissionDetails.assertSubmissionAccepted();
    submissionDetails.assertTotalWarnings(5);
    submissionDetails.assertSubmissionSummary(
        "0P322F", "Mediation", "JAN-2026", "£13,930.00");

    // Assert tabs are visible
    assertThat(submissionDetails.getClaimsTab()).isVisible();
    assertThat(submissionDetails.getMessagesTab()).isVisible();
    assertThat(submissionDetails.getMatterStartsTab()).isVisible();

    // Assert claims tab
    submissionDetails.assertTotalClaims(10);

    // Assert messages tab
    submissionDetails.getMessagesTab().click();
    submissionDetails.assertTotalMessages(5);

    // Assert matter starts tab
    submissionDetails.getMatterStartsTab().click();
    submissionDetails.assertTotalMatterStarts(1);
  }

}
