package uk.gov.justice.laa.bulkclaim.e2e.tests;

import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.e2e.base.BaseTest;
import uk.gov.justice.laa.bulkclaim.e2e.pages.LandingPagePage;
import uk.gov.justice.laa.bulkclaim.e2e.pages.SubmissionDetailPage;
import uk.gov.justice.laa.bulkclaim.e2e.pages.UploadPage;
import uk.gov.justice.laa.bulkclaim.e2e.pages.nilsubmission.NilSubmissionAreaOfLawPage;
import uk.gov.justice.laa.bulkclaim.e2e.pages.nilsubmission.NilSubmissionOfficePage;
import uk.gov.justice.laa.bulkclaim.e2e.pages.nilsubmission.NilSubmissionPeriodPage;
import uk.gov.justice.laa.bulkclaim.e2e.pages.nilsubmission.NilSubmissionReferencePage;
import uk.gov.justice.laa.bulkclaim.e2e.pages.nilsubmission.NilSubmissionSummaryPage;

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

    var referencePage = new NilSubmissionReferencePage(page);
    referencePage.getReferenceInput().fill("CRM/0P322F/2025");
    referencePage.getContinueButton().click();

    var summaryPage = new NilSubmissionSummaryPage(page);
    summaryPage.getSubmitButton().click();

    var submissionDetailPage = new SubmissionDetailPage(page);
    submissionDetailPage.assertSubmissionAccepted();
  }
}
