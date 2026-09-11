package uk.gov.justice.laa.payments.submit.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.payments.submit.e2e.base.JdbcTemplateBaseTest;
import uk.gov.justice.laa.payments.submit.e2e.pages.LandingPagePage;
import uk.gov.justice.laa.payments.submit.e2e.pages.SearchPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.SubmissionDetailPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.UploadPage;
import uk.gov.justice.laa.payments.submit.e2e.persistence.dao.BulkSubmissionDao;
import uk.gov.justice.laa.payments.submit.e2e.persistence.dao.MatterStartDao;
import uk.gov.justice.laa.payments.submit.e2e.persistence.dao.SubmissionDao;
import uk.gov.justice.laa.payments.submit.e2e.persistence.dao.ValidationMessageLogDao;

class SubmissionDetailsE2ETest extends JdbcTemplateBaseTest {

  private static final String USER_ID = "e2e-test-user";

  private final UUID legalHelpSubmissionId = UUID.randomUUID();
  private final UUID crimeLowerSubmissionId = UUID.randomUUID();
  private final UUID mediationSubmissionId = UUID.randomUUID();

  @Override
  protected void seedDatabase() {
    addSubmission(
        legalHelpSubmissionId, "LEGAL_HELP", "APR-2026", 9, new BigDecimal("3311.60"), 9);
    addMatterStart(legalHelpSubmissionId, "AAP", "MDAS All Issues Sole");
    addMatterStart(legalHelpSubmissionId, "COM", null);

    addSubmission(
        crimeLowerSubmissionId, "CRIME_LOWER", "JUN-2026", 10, new BigDecimal("546.53"), 5);

    addSubmission(
        mediationSubmissionId, "MEDIATION", "JAN-2026", 10, new BigDecimal("1393.10"), 5);
    addMatterStart(mediationSubmissionId, null, "MDAS All Issues Sole");
  }

  private void addSubmission(
      UUID submissionId,
      String areaOfLaw,
      String submissionPeriod,
      int claimCount,
      BigDecimal claimValue,
      int warningCount) {
    UUID bulkSubmissionId = BulkSubmissionDao.builder().build().insert(jdbcTemplate);

    SubmissionDao.builder(bulkSubmissionId).id(submissionId)
        .submissionPeriod(submissionPeriod)
        .areaOfLaw(areaOfLaw)
        .numberOfClaims(claimCount)
        .userId(USER_ID)
        .build().insert(jdbcTemplate);

    List<UUID> claimIds = new ArrayList<>();
    for (int i = 0; i < claimCount; i++) {
      claimIds.add(
          claimFixtureFactory.addClaim(submissionId, i + 1, claimValue, USER_ID));
    }

    for (int i = 0; i < warningCount; i++) {
      ValidationMessageLogDao.builder(submissionId)
          .claimId(claimIds.get(i))
          .displayMessage("Test warning message " + (i + 1))
          .build()
          .insert(jdbcTemplate);
    }

  }

  void addMatterStart(UUID submissionId, String categoryCode, String mediationType) {
      MatterStartDao.builder(submissionId)
          .categoryCode(categoryCode)
          .mediationType(mediationType)
          .userId(USER_ID)
          .build()
          .insert(jdbcTemplate);
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
    searchPage.getAreaOfLawSelect().selectOption("LEGAL HELP");
    searchPage.getSearchButton().click();

    // Click first option
    searchPage.clickOnLink(0);

    var submissionDetails = new SubmissionDetailPage(page);
    // Assert basic summary details
    submissionDetails.assertSubmissionAccepted();
    submissionDetails.assertTotalWarnings(9);
    submissionDetails.assertSubmissionSummary(
        "0P322F", "Legal help", "APR-2026", "£29,804.40");

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
    searchPage.getAreaOfLawSelect().selectOption("CRIME LOWER");
    searchPage.getSearchButton().click();

    // Click first option
    searchPage.clickOnLink(0);

    var submissionDetails = new SubmissionDetailPage(page);
    // Assert basic summary details
    submissionDetails.assertSubmissionAccepted();
    submissionDetails.assertTotalWarnings(5);
    submissionDetails.assertSubmissionSummary(
        "0P322F", "Crime lower", "JUN-2026", "£5,465.30");

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
    searchPage.getAreaOfLawSelect().selectOption("MEDIATION");
    searchPage.getSearchButton().click();

    // Click first option
    searchPage.clickOnLink(0);

    var submissionDetails = new SubmissionDetailPage(page);
    // Assert basic summary details
    submissionDetails.assertSubmissionAccepted();
    submissionDetails.assertTotalWarnings(5);
    submissionDetails.assertSubmissionSummary(
        "0P322F", "Mediation", "JAN-2026", "£13,931.00");

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
