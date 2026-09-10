package uk.gov.justice.laa.payments.submit.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.payments.submit.e2e.base.JdbcTemplateBaseTest;
import uk.gov.justice.laa.payments.submit.e2e.pages.LandingPagePage;
import uk.gov.justice.laa.payments.submit.e2e.pages.SearchPage;
import uk.gov.justice.laa.payments.submit.e2e.pages.UploadPage;
import uk.gov.justice.laa.payments.submit.e2e.persistence.dao.BulkSubmissionDao;
import uk.gov.justice.laa.payments.submit.e2e.persistence.dao.SubmissionDao;

class SearchE2ETest extends JdbcTemplateBaseTest {

  private static final String USER_ID = "e2e-test-user";

  private final UUID legalHelpSubmissionId = UUID.randomUUID();
  private final UUID crimeLowerSubmissionId = UUID.randomUUID();
  private final UUID mediationSubmissionId = UUID.randomUUID();

  @Override
  protected void seedDatabase() {
    UUID bulkSubmissionId = BulkSubmissionDao.builder().build().insert(jdbcTemplate);

    SubmissionDao.builder(bulkSubmissionId).id(legalHelpSubmissionId)
        .submissionPeriod("MAY-2026")
        .areaOfLaw("LEGAL_HELP")
        .numberOfClaims(5)
        .userId(USER_ID)
        .build().insert(jdbcTemplate);
    SubmissionDao.builder(bulkSubmissionId).id(crimeLowerSubmissionId)
        .submissionPeriod("JUN-2026")
        .areaOfLaw("CRIME_LOWER")
        .numberOfClaims(5)
        .userId(USER_ID)
        .build().insert(jdbcTemplate);
    SubmissionDao.builder(bulkSubmissionId).id(mediationSubmissionId)
        .submissionPeriod("JUL-2026")
        .areaOfLaw("MEDIATION")
        .numberOfClaims(5)
        .userId(USER_ID)
        .build().insert(jdbcTemplate);

    for (int i = 0; i < 5; i++) {
      claimFixtureFactory.addClaim(
          legalHelpSubmissionId, i + 1, new BigDecimal("100.00"), USER_ID);
      claimFixtureFactory.addClaim(
          crimeLowerSubmissionId, i + 1, new BigDecimal("200.00"), USER_ID);
      claimFixtureFactory.addClaim(
          mediationSubmissionId, i + 1, new BigDecimal("300.00"), USER_ID);
    }
  }

  @Test
  void searchSubmissionFlow() {
    var landingPage = new LandingPagePage(page);
    landingPage.getStartNowButton().click();

    var uploadPage = new UploadPage(page);
    uploadPage.getSearchLink().click();

    var searchPage = new SearchPage(page);
    searchPage.getSearchButton().click();

    assertThat(searchPage.getResultsTable()).isVisible();

    assertTableContainsHeaders(
        "Date submitted", "Office account", "Area of law", "Submission period", "Status");
    searchPage.assertTotalSubmissions(3);
  }

  @Test
  void searchForAreaOfLaw(){
    var landingPage = new LandingPagePage(page);
    landingPage.getStartNowButton().click();

    var uploadPage = new UploadPage(page);
    uploadPage.getSearchLink().click();

    var searchPage = new SearchPage(page);
    searchPage.getAreaOfLawSelect().locator("Legal help");
    searchPage.getSearchButton().click();

    assertThat(searchPage.getResultsTable()).isVisible();

    assertTableContainsHeaders(
        "Date submitted", "Office account", "Area of law", "Submission period", "Status");
    searchPage.assertTotalSubmissions(1);
  }
}
