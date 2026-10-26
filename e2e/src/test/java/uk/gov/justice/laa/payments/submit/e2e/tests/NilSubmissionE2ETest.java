package uk.gov.justice.laa.payments.submit.e2e.tests;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.payments.submit.e2e.base.BaseTest;
import uk.gov.justice.laa.payments.submit.e2e.pages.LandingPagePage;
import uk.gov.justice.laa.payments.submit.e2e.pages.PreSubmissionDetailPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.SubmissionDetailPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.UploadPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission.NilSubmissionAreaOfLawPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission.NilSubmissionCrimeScheduleNumberPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission.NilSubmissionOfficePage;
import uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission.NilSubmissionPeriodPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission.NilSubmissionSummaryPage;

public class NilSubmissionE2ETest extends BaseTest {

  @Test
  @Tag("pre-submission-disabled")
  public void nilSubmission() {
    createNilSubmission();

    var submissionDetailPage = new SubmissionDetailPage(page);
    submissionDetailPage.assertSubmissionAccepted();
  }

  @Test
  @Tag("pre-submission-enabled")
  public void nilSubmissionAcceptedAfterApproval() {
    createNilSubmission();

    var preSubmissionDetailPage = new PreSubmissionDetailPage(page);
    preSubmissionDetailPage.assertSubmissionNotSubmitted();
    preSubmissionDetailPage.getSubmitButton().click();

    var submissionDetailPage = new SubmissionDetailPage(page);
    submissionDetailPage.assertSubmissionAccepted();
  }

  private void createNilSubmission() {
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
  }
}
