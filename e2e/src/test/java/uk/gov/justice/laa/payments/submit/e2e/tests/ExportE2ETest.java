package uk.gov.justice.laa.payments.submit.e2e.tests;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Download;
import java.io.IOException;
import java.io.InputStream;
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
import uk.gov.justice.laa.payments.submit.e2e.persistence.dao.SubmissionDao;
import uk.gov.justice.laa.payments.submit.e2e.persistence.dao.ValidationMessageLogDao;

class ExportE2ETest extends JdbcTemplateBaseTest {

  private static final String USER_ID = "e2e-test-user";
  private static final String LEGAL_HELP_FIRST_COLUMN = "Providers LAA Office Number";
  private static final int CLAIM_COUNT = 3;
  private static final int MESSAGE_COUNT = 2;

  private final UUID acceptedSubmissionId = UUID.randomUUID();

  @Override
  protected void seedDatabase() {
    UUID bulkSubmissionId = BulkSubmissionDao.builder().build().insert(jdbcTemplate);

    SubmissionDao.builder(bulkSubmissionId)
        .id(acceptedSubmissionId)
        .submissionPeriod("APR-2026")
        .areaOfLaw("LEGAL_HELP")
        .status("VALIDATION_SUCCEEDED")
        .numberOfClaims(CLAIM_COUNT)
        .userId(USER_ID)
        .build()
        .insert(jdbcTemplate);

    List<UUID> claimIds = new ArrayList<>();
    for (int i = 0; i < CLAIM_COUNT; i++) {
      claimIds.add(
          claimFixtureFactory.addClaim(
              acceptedSubmissionId, i + 1, new BigDecimal("100.00"), USER_ID));
    }

    for (int i = 0; i < MESSAGE_COUNT; i++) {
      ValidationMessageLogDao.builder(acceptedSubmissionId)
          .claimId(claimIds.get(i))
          .displayMessage("Test warning message " + (i + 1))
          .build()
          .insert(jdbcTemplate);
    }
  }

  @Test
  void exportsAcceptedSubmissionAndLeavesPageUsable() throws IOException {
    var submissionDetails = openSubmissionSummary("LEGAL HELP");
    submissionDetails.assertSubmissionAccepted();

    var download = submissionDetails.downloadClaims();

    assertTrue(
        download.suggestedFilename().endsWith(".csv"),
        "Expected the exported file to be a CSV, but was: " + download.suggestedFilename());

    String csv = readDownload(download);
    assertFalse(csv.isBlank(), "Expected the exported file to have content");
    assertTrue(
        csv.lines().findFirst().orElse("").contains(LEGAL_HELP_FIRST_COLUMN),
        "Expected the exported file to start with the legal help CSV header row, but was: " + csv);

    assertThat(submissionDetails.getHeading()).isVisible();
    submissionDetails.assertTotalClaims(CLAIM_COUNT);
    submissionDetails.getMessagesTab().click();
    submissionDetails.assertTotalMessages(MESSAGE_COUNT);
  }

  private SubmissionDetailPage openSubmissionSummary(String areaOfLaw) {
    var landingPage = new LandingPagePage(page);
    landingPage.getStartNowButton().click();

    // Start user journey to make search button visible
    var uploadPage = new UploadPage(page);

    uploadPage.getSearchLink().click();
    var searchPage = new SearchPage(page);
    searchPage.getAreaOfLawSelect().selectOption(areaOfLaw);
    searchPage.getSearchButton().click();

    searchPage.clickOnLink(0);

    return new SubmissionDetailPage(page);
  }

  private String readDownload(Download download) throws IOException {
    try (InputStream stream = download.createReadStream()) {
      return new String(stream.readAllBytes(), UTF_8);
    }
  }
}
