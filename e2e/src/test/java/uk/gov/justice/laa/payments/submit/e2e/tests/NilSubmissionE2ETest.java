package uk.gov.justice.laa.payments.submit.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.payments.submit.e2e.base.BaseTest;
import uk.gov.justice.laa.payments.submit.e2e.pages.LandingPagePage;
import uk.gov.justice.laa.payments.submit.e2e.pages.SubmissionDetailPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.UploadPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission.NilSubmissionAreaOfLawPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission.NilSubmissionCivilSubmissionReferencePage;
import uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission.NilSubmissionCrimeScheduleNumberPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission.NilSubmissionMediationSubmissionReferencePage;
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

    populateFullNilSubmissionJourney();

    var referencePage = new NilSubmissionCrimeScheduleNumberPage(page);
    referencePage.getReferenceInput().fill("CRM/0P322F/2025");
    referencePage.getContinueButton().click();

    var summaryPage = new NilSubmissionSummaryPage(page);
    summaryPage.getSubmitButton().click();

    var submissionDetailPage = new SubmissionDetailPage(page);
    submissionDetailPage.assertSubmissionAccepted();
  }

  @Nested
  class ChangeLinks {

    @Test
    public void nilSubmissionSummaryChangeOffice() {
      var landingPage = new LandingPagePage(page);
      landingPage.getStartNowButton().click();

      var upload = new UploadPage(page);
      upload.getCreateNilSubmissionButton().click();

      populateFullNilSubmissionJourney();

      var referencePage = new NilSubmissionCrimeScheduleNumberPage(page);
      referencePage.getReferenceInput().fill("CRM/0P322F/2025");
      referencePage.getContinueButton().click();

      var summaryPage = new NilSubmissionSummaryPage(page);
      summaryPage.getChangeOfficeLink().click();

      // Asserts that page is loaded
      new NilSubmissionOfficePage(page);
    }

    @Test
    public void nilSubmissionSummaryChangeAreaOfLaw() {
      var landingPage = new LandingPagePage(page);
      landingPage.getStartNowButton().click();

      var upload = new UploadPage(page);
      upload.getCreateNilSubmissionButton().click();

      populateFullNilSubmissionJourney();

      var referencePage = new NilSubmissionCrimeScheduleNumberPage(page);
      referencePage.getReferenceInput().fill("CRM/0P322F/2025");
      referencePage.getContinueButton().click();

      var summaryPage = new NilSubmissionSummaryPage(page);
      summaryPage.getChangeAreaOfLawLink().click();

      NilSubmissionAreaOfLawPage nilSubmissionAreaOfLawPage = new NilSubmissionAreaOfLawPage(page);
      assertThat(nilSubmissionAreaOfLawPage.getOfficeAccountNumberValue()).hasText("0P322F");
    }

    @Test
    public void nilSubmissionSummaryChangeSubmissionPeriod() {
      var landingPage = new LandingPagePage(page);
      landingPage.getStartNowButton().click();

      var upload = new UploadPage(page);
      upload.getCreateNilSubmissionButton().click();

      populateFullNilSubmissionJourney();

      var referencePage = new NilSubmissionCrimeScheduleNumberPage(page);
      referencePage.getReferenceInput().fill("CRM/0P322F/2025");
      referencePage.getContinueButton().click();

      var summaryPage = new NilSubmissionSummaryPage(page);
      summaryPage.getChangeSubmissionPeriodLink().click();

      NilSubmissionPeriodPage nilSubmissionPeriodPage = new NilSubmissionPeriodPage(page);
      assertThat(nilSubmissionPeriodPage.getOfficeAccountNumberValue()).hasText("0P322F");
      assertThat(nilSubmissionPeriodPage.getAreaOfLawValue()).hasText("Crime lower");
    }

    @Test
    public void nilSubmissionSummaryChangeCivilRefNumber() {
      var landingPage = new LandingPagePage(page);
      landingPage.getStartNowButton().click();

      var upload = new UploadPage(page);
      upload.getCreateNilSubmissionButton().click();

      var officePage = new NilSubmissionOfficePage(page);
      officePage.selectOffice("0P322F");
      officePage.getContinueButton().click();

      var areaOfLawPage = new NilSubmissionAreaOfLawPage(page);
      areaOfLawPage.getLegalHelpRadio().click();
      areaOfLawPage.getContinueButton().click();

      var periodPage = new NilSubmissionPeriodPage(page);
      periodPage.selectFirstAvailablePeriod();
      var value = periodPage.getPeriodSelect().inputValue();
      periodPage.getContinueButton().click();

      var referencePage = new NilSubmissionCivilSubmissionReferencePage(page);
      referencePage.getReferenceInput().fill("0P322F/Civil/01");
      referencePage.getContinueButton().click();

      var summaryPage = new NilSubmissionSummaryPage(page);
      summaryPage.getChangeSubmissionReferenceLink().click();

      NilSubmissionCivilSubmissionReferencePage civilSubmissionReferencePage =
          new NilSubmissionCivilSubmissionReferencePage(page);
      assertThat(civilSubmissionReferencePage.getOfficeAccountNumberValue()).hasText("0P322F");
      assertThat(civilSubmissionReferencePage.getAreaOfLawValue()).hasText("Legal help");
      assertThat(civilSubmissionReferencePage.getSubmissionPeriodValue()).hasText(value);
    }

    @Test
    public void nilSubmissionSummaryChangeCrimeRefNumber() {
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
      var value = periodPage.getPeriodSelect().inputValue();
      periodPage.getContinueButton().click();

      var referencePage = new NilSubmissionCrimeScheduleNumberPage(page);
      referencePage.getReferenceInput().fill("CRM/0P322F/2025");
      referencePage.getContinueButton().click();

      var summaryPage = new NilSubmissionSummaryPage(page);
      summaryPage.getChangeSubmissionReferenceLink().click();

      NilSubmissionCrimeScheduleNumberPage crimeScheduleNumberPage =
          new NilSubmissionCrimeScheduleNumberPage(page);
      assertThat(crimeScheduleNumberPage.getOfficeAccountNumberValue()).hasText("0P322F");
      assertThat(crimeScheduleNumberPage.getAreaOfLawValue()).hasText("Crime lower");
      assertThat(crimeScheduleNumberPage.getSubmissionPeriodValue()).hasText(value);
    }

    @Test
    public void nilSubmissionSummaryChangeMediationRefNumber() {
      var landingPage = new LandingPagePage(page);
      landingPage.getStartNowButton().click();

      var upload = new UploadPage(page);
      upload.getCreateNilSubmissionButton().click();

      var officePage = new NilSubmissionOfficePage(page);
      officePage.selectOffice("0P322F");
      officePage.getContinueButton().click();

      var areaOfLawPage = new NilSubmissionAreaOfLawPage(page);
      areaOfLawPage.getMediationRadio().click();
      areaOfLawPage.getContinueButton().click();

      var periodPage = new NilSubmissionPeriodPage(page);
      periodPage.selectFirstAvailablePeriod();
      var value = periodPage.getPeriodSelect().inputValue();
      periodPage.getContinueButton().click();

      var referencePage = new NilSubmissionMediationSubmissionReferencePage(page);
      referencePage.getReferenceInput().fill("MED/0P322F/2025");
      referencePage.getContinueButton().click();

      var summaryPage = new NilSubmissionSummaryPage(page);
      summaryPage.getChangeSubmissionReferenceLink().click();

      NilSubmissionMediationSubmissionReferencePage mediationReferencePage =
          new NilSubmissionMediationSubmissionReferencePage(page);
      assertThat(mediationReferencePage.getOfficeAccountNumberValue()).hasText("0P322F");
      assertThat(mediationReferencePage.getAreaOfLawValue()).hasText("Mediation");
      assertThat(mediationReferencePage.getSubmissionPeriodValue()).hasText(value);
    }
  }


  @Nested
  class ValidationRedirects {

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
      assertThat(officePageWithError.getErrorMessage()).containsText(
          "Select the office account number");
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

    @Test
    public void nilSubmissionCrimeRefNotEnteredShowsValidationError() {
      startNilSubmissionAndSelectOffice();

      var areaOfLawPage = new NilSubmissionAreaOfLawPage(page);
      areaOfLawPage.getCrimeLowerRadio().click();
      areaOfLawPage.getContinueButton().click();

      NilSubmissionPeriodPage nilSubmissionPeriodPage = new NilSubmissionPeriodPage(page);
      nilSubmissionPeriodPage.selectFirstAvailablePeriod();
      nilSubmissionPeriodPage.getContinueButton().click();

      new NilSubmissionCrimeScheduleNumberPage(page).getContinueButton().click();

      var scheduleNumberPag = new NilSubmissionCrimeScheduleNumberPage(page);
      scheduleNumberPag.getContinueButton().click();

      assertThat(scheduleNumberPag.getErrorSummary()).containsText("Enter a crime schedule number");
      assertThat(scheduleNumberPag.getErrorMessage()).containsText("Enter a crime schedule number");
    }

    @Test
    public void nilSubmissionCivilRefNotEnteredShowsValidationError() {
      startNilSubmissionAndSelectOffice();

      var areaOfLawPage = new NilSubmissionAreaOfLawPage(page);
      areaOfLawPage.getLegalHelpRadio().click();
      areaOfLawPage.getContinueButton().click();

      NilSubmissionPeriodPage nilSubmissionPeriodPage = new NilSubmissionPeriodPage(page);
      nilSubmissionPeriodPage.selectFirstAvailablePeriod();
      nilSubmissionPeriodPage.getContinueButton().click();

      new NilSubmissionCivilSubmissionReferencePage(page).getContinueButton().click();

      var scheduleNumberPag = new NilSubmissionCivilSubmissionReferencePage(page);
      scheduleNumberPag.getContinueButton().click();

      assertThat(scheduleNumberPag.getErrorSummary()).containsText(
          "Enter a civil submission reference");
      assertThat(scheduleNumberPag.getErrorMessage()).containsText(
          "Enter a civil submission reference");
    }

    @Test
    public void nilSubmissionMediationRefNotEnteredShowsValidationError() {
      startNilSubmissionAndSelectOffice();

      var areaOfLawPage = new NilSubmissionAreaOfLawPage(page);
      areaOfLawPage.getMediationRadio().click();
      areaOfLawPage.getContinueButton().click();

      NilSubmissionPeriodPage nilSubmissionPeriodPage = new NilSubmissionPeriodPage(page);
      nilSubmissionPeriodPage.selectFirstAvailablePeriod();
      nilSubmissionPeriodPage.getContinueButton().click();

      new NilSubmissionMediationSubmissionReferencePage(page).getContinueButton().click();

      var scheduleNumberPag = new NilSubmissionMediationSubmissionReferencePage(page);
      scheduleNumberPag.getContinueButton().click();

      assertThat(scheduleNumberPag.getErrorSummary()).containsText(
          "Enter a mediation submission reference");
      assertThat(scheduleNumberPag.getErrorMessage()).containsText(
          "Enter a mediation submission reference");
    }

  }

  private void populateFullNilSubmissionJourney() {
    var officePage = new NilSubmissionOfficePage(page);
    officePage.selectOffice("0P322F");
    officePage.getContinueButton().click();

    var areaOfLawPage = new NilSubmissionAreaOfLawPage(page);
    areaOfLawPage.getCrimeLowerRadio().click();
    areaOfLawPage.getContinueButton().click();

    var periodPage = new NilSubmissionPeriodPage(page);
    periodPage.selectFirstAvailablePeriod();
    periodPage.getContinueButton().click();
  }


  private void startNilSubmissionAndSelectOffice() {
    new LandingPagePage(page).getStartNowButton().click();
    new UploadPage(page).getCreateNilSubmissionButton().click();

    var officePage = new NilSubmissionOfficePage(page);
    officePage.selectOffice("0P322F");
    officePage.getContinueButton().click();
  }
}
