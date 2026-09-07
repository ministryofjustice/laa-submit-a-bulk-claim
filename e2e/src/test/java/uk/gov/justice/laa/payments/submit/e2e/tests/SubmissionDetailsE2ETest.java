package uk.gov.justice.laa.payments.submit.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.payments.submit.e2e.base.SqlInsertBaseTest;
import uk.gov.justice.laa.payments.submit.e2e.models.BulkSubmissionInsert;
import uk.gov.justice.laa.payments.submit.e2e.models.Insert;
import uk.gov.justice.laa.payments.submit.e2e.models.SubmissionInsert;
import uk.gov.justice.laa.payments.submit.e2e.pages.LandingPagePage;
import uk.gov.justice.laa.payments.submit.e2e.pages.SearchPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.SubmissionDetailPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.UploadPage;
import uk.gov.justice.laa.payments.submit.e2e.utils.ClaimFixtureFactory;

class SubmissionDetailsE2ETest extends SqlInsertBaseTest {

  private static final String USER_ID = "e2e-test-user";
  private static final String OFFICE_ACCOUNT_NUMBER = "0P322F";
  private static final String[] LEGAL_HELP_CATEGORY_CODES = {"AAP", "COM"};

  private final String legalHelpSubmissionId = UUID.randomUUID().toString();
  private final String crimeLowerSubmissionId = UUID.randomUUID().toString();
  private final String mediationSubmissionId = UUID.randomUUID().toString();

  @Override
  protected List<Insert> inserts() {
    List<Insert> inserts = new ArrayList<>();

    addSubmission(
        inserts,
        legalHelpSubmissionId,
        "LEGAL_HELP",
        "APR-2026",
        9,
        new BigDecimal("33115.60"),
        9,
        2);
    addSubmission(
        inserts,
        crimeLowerSubmissionId,
        "CRIME_LOWER",
        "JUN-2026",
        10,
        new BigDecimal("5465.50"),
        5,
        0);
    addSubmission(
        inserts,
        mediationSubmissionId,
        "MEDIATION",
        "JAN-2026",
        10,
        new BigDecimal("13930.00"),
        5,
        1);

    return inserts;
  }

  private void addSubmission(
      List<Insert> inserts,
      String submissionId,
      String areaOfLaw,
      String submissionPeriod,
      int claimCount,
      BigDecimal totalValue,
      int warningCount,
      int matterStartCount) {
    String bulkSubmissionId = UUID.randomUUID().toString();
    inserts.add(BulkSubmissionInsert.builder().id(bulkSubmissionId).userId(USER_ID).build());

    inserts.add(
        SubmissionInsert.builder()
            .id(submissionId)
            .bulkSubmissionId(bulkSubmissionId)
            .officeAccountNumber(OFFICE_ACCOUNT_NUMBER)
            .submissionPeriod(submissionPeriod)
            .areaOfLaw(areaOfLaw)
            .numberOfClaims(claimCount)
            .legalHelpSubmissionReference(null)
            .mediationSubmissionReference(null)
            .crimeLowerScheduleNumber(null)
            .userId(USER_ID)
            .build());

    List<BigDecimal> amounts = ClaimFixtureFactory.splitEvenly(totalValue, claimCount);
    List<String> claimIds = new ArrayList<>();
    for (int i = 0; i < claimCount; i++) {
      claimIds.add(
          ClaimFixtureFactory.addClaim(
              inserts, submissionId, i + 1, amounts.get(i), USER_ID));
    }

    for (int i = 0; i < warningCount; i++) {
      ClaimFixtureFactory.addWarning(inserts, submissionId, claimIds.get(i), i + 1);
    }

    for (int i = 0; i < matterStartCount; i++) {
      String categoryCode = "LEGAL_HELP".equals(areaOfLaw) ? LEGAL_HELP_CATEGORY_CODES[i] : null;
      String mediationType = "MEDIATION".equals(areaOfLaw) ? "MDAS" : null;
      ClaimFixtureFactory.addMatterStart(inserts, submissionId, categoryCode, mediationType, USER_ID);
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
