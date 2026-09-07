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
import uk.gov.justice.laa.payments.submit.e2e.utils.ClaimFixtureFactory;

class SubmissionDetailsE2ETest extends JdbcTemplateBaseTest {

  private static final String USER_ID = "e2e-test-user";
  private static final String[] LEGAL_HELP_CATEGORY_CODES = {"AAP", "COM"};

  private final UUID legalHelpSubmissionId = UUID.randomUUID();
  private final UUID crimeLowerSubmissionId = UUID.randomUUID();
  private final UUID mediationSubmissionId = UUID.randomUUID();

  @Override
  protected void seedDatabase() {
    addSubmission(
        legalHelpSubmissionId, "LEGAL_HELP", "APR-2026", 9, new BigDecimal("33115.60"), 9, 2);
    addSubmission(
        crimeLowerSubmissionId, "CRIME_LOWER", "JUN-2026", 10, new BigDecimal("5465.50"), 5, 0);
    addSubmission(
        mediationSubmissionId, "MEDIATION", "JAN-2026", 10, new BigDecimal("13930.00"), 5, 1);
  }

  private void addSubmission(
      UUID submissionId,
      String areaOfLaw,
      String submissionPeriod,
      int claimCount,
      BigDecimal totalValue,
      int warningCount,
      int matterStartCount) {
    UUID bulkSubmissionId = BulkSubmissionDao.builder().build().insert(jdbcTemplate);

    SubmissionDao.builder(bulkSubmissionId).id(submissionId)
        .submissionPeriod(submissionPeriod)
        .areaOfLaw(areaOfLaw)
        .numberOfClaims(claimCount)
        .userId(USER_ID)
        .build().insert(jdbcTemplate);

    List<BigDecimal> amounts = ClaimFixtureFactory.splitEvenly(totalValue, claimCount);
    List<UUID> claimIds = new ArrayList<>();
    for (int i = 0; i < claimCount; i++) {
      claimIds.add(
          claimFixtureFactory.addClaim(submissionId, i + 1, amounts.get(i), USER_ID));
    }

    for (int i = 0; i < warningCount; i++) {
      ValidationMessageLogDao.builder(submissionId)
          .claimId(claimIds.get(i))
          .displayMessage("Test warning message " + (i + 1))
          .build()
          .insert(jdbcTemplate);
    }

    for (int i = 0; i < matterStartCount; i++) {
      String categoryCode = "LEGAL_HELP".equals(areaOfLaw) ? LEGAL_HELP_CATEGORY_CODES[i] : null;
      String mediationType = "MEDIATION".equals(areaOfLaw) ? "MDAS All Issues Sole" : null;
      MatterStartDao.builder(submissionId)
          .categoryCode(categoryCode)
          .mediationType(mediationType)
          .userId(USER_ID)
          .build()
          .insert(jdbcTemplate);
    }
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
