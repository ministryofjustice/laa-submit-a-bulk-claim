package uk.gov.justice.laa.payments.submit.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.payments.submit.e2e.base.BaseTest;
import uk.gov.justice.laa.payments.submit.e2e.pages.LandingPagePage;
import uk.gov.justice.laa.payments.submit.e2e.pages.SubmissionDetailPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.UploadPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission.NilSubmissionAreaOfLawPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission.NilSubmissionCrimeScheduleNumberPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission.NilSubmissionOfficePage;
import uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission.NilSubmissionPeriodPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission.NilSubmissionSummaryPage;

public class NilSubmissionE2ETest extends BaseTest {

  @Test
  public void nilSubmission() {
    var landingPage = new LandingPagePage(page);
    landingPage.getStartNowButton().click();

    var upload = new UploadPage(page);
    upload.getCreateNilSubmissionButton().click();

    var officePage = new NilSubmissionOfficePage(page);
    officePage.selectOffice("0P322F");
    officePage.getContinueButton().click();

    var areaOfLawPage = new NilSubmissionAreaOfLawPage(page);
    areaOfLawPage.getCrimeLowerRadio().click();
    areaOfLawPage.getContinueButton().click();

    var periodPage = new NilSubmissionPeriodPage(page);
    periodPage.selectFirstAvailablePeriod();
    periodPage.getContinueButton().click();

    var referencePage = new NilSubmissionCrimeScheduleNumberPage(page);
    referencePage.getReferenceInput().fill("CRM/0P322F/2025");
    referencePage.getContinueButton().click();

    var summaryPage = new NilSubmissionSummaryPage(page);
    summaryPage.getSubmitButton().click();

    var submissionDetailPage = new SubmissionDetailPage(page);
    submissionDetailPage.assertSubmissionAccepted();
  }

  @Test
  public void nilSubmissionOfficeNotSelectedShowsValidationError() {
    var landingPage = new LandingPagePage(page);
    landingPage.getStartNowButton().click();

    var upload = new UploadPage(page);
    upload.getCreateNilSubmissionButton().click();

    var officePage = new NilSubmissionOfficePage(page);
    officePage.getContinueButton().click();

    var officePageWithError = new NilSubmissionOfficePage(page);
    assertThat(officePageWithError.getErrorSummary()).isVisible();
    assertThat(officePageWithError.getErrorSummary())
        .containsText("Select the office account number");
    assertThat(officePageWithError.getErrorMessage()).containsText("Select the office account number");
  }

  @Test
  public void nilSubmissionAreaOfLawNotSelectedShowsValidationError() {
    startNilSubmissionAndSelectOffice();

    new NilSubmissionAreaOfLawPage(page).getContinueButton().click();

    var areaOfLawPageWithError = new NilSubmissionAreaOfLawPage(page);
    assertThat(areaOfLawPageWithError.getErrorSummary()).containsText("Select the area of law");
    assertThat(areaOfLawPageWithError.getErrorMessage()).containsText("Select the area of law");
  }

  @Test
  public void nilSubmissionPeriodNotSelectedShowsValidationError() {
    startNilSubmissionAndSelectOffice();

    var areaOfLawPage = new NilSubmissionAreaOfLawPage(page);
    areaOfLawPage.getCrimeLowerRadio().click();
    areaOfLawPage.getContinueButton().click();

    new NilSubmissionPeriodPage(page).getContinueButton().click();

    var periodPageWithError = new NilSubmissionPeriodPage(page);
    assertThat(periodPageWithError.getErrorSummary()).containsText("Select a submission period");
    assertThat(periodPageWithError.getErrorMessage()).containsText("Select a submission period");
  }

  private void startNilSubmissionAndSelectOffice() {
    new LandingPagePage(page).getStartNowButton().click();
    new UploadPage(page).getCreateNilSubmissionButton().click();

    var officePage = new NilSubmissionOfficePage(page);
    officePage.selectOffice("0P322F");
    officePage.getContinueButton().click();
  }
}
