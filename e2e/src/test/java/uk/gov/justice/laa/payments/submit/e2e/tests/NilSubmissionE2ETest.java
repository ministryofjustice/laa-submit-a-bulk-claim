package uk.gov.justice.laa.payments.submit.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import java.util.function.Function;
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
import uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission.NilSubmissionReferencePage;
import uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission.NilSubmissionSummaryPage;

public class NilSubmissionE2ETest extends BaseTest {

  private static final String OFFICE = "0P322F";

  @Test
  public void nilSubmission() {
    var summaryPage = completeCrimeNilSubmission();
    summaryPage.getSubmitButton().click();

    var submissionDetailPage = new SubmissionDetailPage(page);
    submissionDetailPage.assertSubmissionAccepted();
  }

  @Nested
  class ChangeLinks {

    @Test
    public void nilSubmissionSummaryChangeOffice() {
      var summaryPage = completeCrimeNilSubmission();
      summaryPage.getChangeOfficeLink().click();

      // Asserts that page is loaded
      new NilSubmissionOfficePage(page);
    }

    @Test
    public void nilSubmissionSummaryChangeAreaOfLaw() {
      var summaryPage = completeCrimeNilSubmission();
      summaryPage.getChangeAreaOfLawLink().click();

      var areaOfLawPage = new NilSubmissionAreaOfLawPage(page);
      assertThat(areaOfLawPage.getOfficeAccountNumberValue()).hasText(OFFICE);
    }

    @Test
    public void nilSubmissionSummaryChangeSubmissionPeriod() {
      var summaryPage = completeCrimeNilSubmission();
      summaryPage.getChangeSubmissionPeriodLink().click();

      var periodPage = new NilSubmissionPeriodPage(page);
      assertThat(periodPage.getOfficeAccountNumberValue()).hasText(OFFICE);
      assertThat(periodPage.getAreaOfLawValue()).hasText("Crime lower");
    }

    @Test
    public void nilSubmissionSummaryChangeCivilRefNumber() {
      startNilSubmissionAndSelectOffice();
      var periodPage = selectAreaOfLaw(NilSubmissionAreaOfLawPage::getLegalHelpRadio);
      var period = selectFirstPeriod(periodPage);
      var summaryPage =
          submitReference(
              new NilSubmissionCivilSubmissionReferencePage(page), "0P322F/Civil/01");

      summaryPage.getChangeSubmissionReferenceLink().click();

      var referencePage = new NilSubmissionCivilSubmissionReferencePage(page);
      assertReferencePageShowsSelections(referencePage, "Legal help", period);
    }

    @Test
    public void nilSubmissionSummaryChangeCrimeRefNumber() {
      startNilSubmissionAndSelectOffice();
      var periodPage = selectAreaOfLaw(NilSubmissionAreaOfLawPage::getCrimeLowerRadio);
      var period = selectFirstPeriod(periodPage);
      var summaryPage =
          submitReference(new NilSubmissionCrimeScheduleNumberPage(page), "CRM/0P322F/2025");

      summaryPage.getChangeSubmissionReferenceLink().click();

      var referencePage = new NilSubmissionCrimeScheduleNumberPage(page);
      assertReferencePageShowsSelections(referencePage, "Crime lower", period);
    }

    @Test
    public void nilSubmissionSummaryChangeMediationRefNumber() {
      startNilSubmissionAndSelectOffice();
      var periodPage = selectAreaOfLaw(NilSubmissionAreaOfLawPage::getMediationRadio);
      var period = selectFirstPeriod(periodPage);
      var summaryPage =
          submitReference(
              new NilSubmissionMediationSubmissionReferencePage(page), "MED/0P322F/2025");

      summaryPage.getChangeSubmissionReferenceLink().click();

      var referencePage = new NilSubmissionMediationSubmissionReferencePage(page);
      assertReferencePageShowsSelections(referencePage, "Mediation", period);
    }
  }

  @Nested
  class ValidationRedirects {

    @Test
    public void nilSubmissionOfficeNotSelectedShowsValidationError() {
      var officePage = startNilSubmission();
      officePage.getContinueButton().click();

      var officePageWithError = new NilSubmissionOfficePage(page);
      assertThat(officePageWithError.getErrorSummary()).isVisible();
      assertThat(officePageWithError.getErrorSummary())
          .containsText("Select the office account number");
      assertThat(officePageWithError.getErrorMessage())
          .containsText("Select the office account number");
    }

    @Test
    public void nilSubmissionAreaOfLawNotSelectedShowsValidationError() {
      var areaOfLawPage = startNilSubmissionAndSelectOffice();
      areaOfLawPage.getContinueButton().click();

      var areaOfLawPageWithError = new NilSubmissionAreaOfLawPage(page);
      assertThat(areaOfLawPageWithError.getErrorSummary()).containsText("Select the area of law");
      assertThat(areaOfLawPageWithError.getErrorMessage()).containsText("Select the area of law");
    }

    @Test
    public void nilSubmissionPeriodNotSelectedShowsValidationError() {
      startNilSubmissionAndSelectOffice();
      var periodPage = selectAreaOfLaw(NilSubmissionAreaOfLawPage::getCrimeLowerRadio);
      periodPage.getContinueButton().click();

      var periodPageWithError = new NilSubmissionPeriodPage(page);
      assertThat(periodPageWithError.getErrorSummary()).containsText("Select a submission period");
      assertThat(periodPageWithError.getErrorMessage()).containsText("Select a submission period");
    }

    @Test
    public void nilSubmissionCrimeRefNotEnteredShowsValidationError() {
      startNilSubmissionAndSelectOffice();
      var periodPage = selectAreaOfLaw(NilSubmissionAreaOfLawPage::getCrimeLowerRadio);
      selectFirstPeriod(periodPage);

      new NilSubmissionCrimeScheduleNumberPage(page).getContinueButton().click();

      var referencePageWithError = new NilSubmissionCrimeScheduleNumberPage(page);
      assertReferenceRequiredError(referencePageWithError, "Enter a crime schedule number");
    }

    @Test
    public void nilSubmissionCivilRefNotEnteredShowsValidationError() {
      startNilSubmissionAndSelectOffice();
      var periodPage = selectAreaOfLaw(NilSubmissionAreaOfLawPage::getLegalHelpRadio);
      selectFirstPeriod(periodPage);

      new NilSubmissionCivilSubmissionReferencePage(page).getContinueButton().click();

      var referencePageWithError = new NilSubmissionCivilSubmissionReferencePage(page);
      assertReferenceRequiredError(referencePageWithError, "Enter a civil submission reference");
    }

    @Test
    public void nilSubmissionMediationRefNotEnteredShowsValidationError() {
      startNilSubmissionAndSelectOffice();
      var periodPage = selectAreaOfLaw(NilSubmissionAreaOfLawPage::getMediationRadio);
      selectFirstPeriod(periodPage);

      new NilSubmissionMediationSubmissionReferencePage(page).getContinueButton().click();

      var referencePageWithError = new NilSubmissionMediationSubmissionReferencePage(page);
      assertReferenceRequiredError(
          referencePageWithError, "Enter a mediation submission reference");
    }
  }

  /** Landing page through to the office page. */
  private NilSubmissionOfficePage startNilSubmission() {
    new LandingPagePage(page).getStartNowButton().click();
    new UploadPage(page).getCreateNilSubmissionButton().click();
    return new NilSubmissionOfficePage(page);
  }

  /** Landing page, then selects the office and continues to the area of law page. */
  private NilSubmissionAreaOfLawPage startNilSubmissionAndSelectOffice() {
    var officePage = startNilSubmission();
    officePage.selectOffice(OFFICE);
    officePage.getContinueButton().click();
    return new NilSubmissionAreaOfLawPage(page);
  }

  /** On the area of law page, picks the radio and continues to the period page. */
  private NilSubmissionPeriodPage selectAreaOfLaw(
      Function<NilSubmissionAreaOfLawPage, Locator> radio) {
    var areaOfLawPage = new NilSubmissionAreaOfLawPage(page);
    radio.apply(areaOfLawPage).click();
    areaOfLawPage.getContinueButton().click();
    return new NilSubmissionPeriodPage(page);
  }

  /** Selects the first period, continues, and returns the period that was selected. */
  private String selectFirstPeriod(NilSubmissionPeriodPage periodPage) {
    periodPage.selectFirstAvailablePeriod();
    var period = periodPage.getPeriodSelect().inputValue();
    periodPage.getContinueButton().click();
    return period;
  }

  /** Enters the reference, continues, and returns the summary page. */
  private NilSubmissionSummaryPage submitReference(
      NilSubmissionReferencePage referencePage, String reference) {
    referencePage.getReferenceInput().fill(reference);
    referencePage.getContinueButton().click();
    return new NilSubmissionSummaryPage(page);
  }

  /** Crime lower journey: office, area of law, first period and reference, ending on summary. */
  private NilSubmissionSummaryPage completeCrimeNilSubmission() {
    startNilSubmissionAndSelectOffice();
    var periodPage = selectAreaOfLaw(NilSubmissionAreaOfLawPage::getCrimeLowerRadio);
    selectFirstPeriod(periodPage);
    return submitReference(new NilSubmissionCrimeScheduleNumberPage(page), "CRM/0P322F/2025");
  }

  private void assertReferencePageShowsSelections(
      NilSubmissionReferencePage referencePage, String areaOfLaw, String period) {
    assertThat(referencePage.getOfficeAccountNumberValue()).hasText(OFFICE);
    assertThat(referencePage.getAreaOfLawValue()).hasText(areaOfLaw);
    assertThat(referencePage.getSubmissionPeriodValue()).hasText(period);
  }

  private void assertReferenceRequiredError(
      NilSubmissionReferencePage referencePage, String message) {
    assertThat(referencePage.getErrorSummary()).containsText(message);
    assertThat(referencePage.getErrorMessage()).containsText(message);
  }
}
