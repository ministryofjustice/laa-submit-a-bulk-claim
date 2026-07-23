package uk.gov.justice.laa.bulkclaim.e2e.tests;

import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.e2e.base.BaseTest;
import uk.gov.justice.laa.bulkclaim.e2e.pages.LandingPagePage;
import uk.gov.justice.laa.bulkclaim.e2e.pages.UploadPage;

public class BulkSubmissionE2ETest extends BaseTest {

  @Test
  public void bulkSubmission() {
    var landingPage = new LandingPagePage(page);
    landingPage.getStartNowButton().click();

    var upload = new UploadPage(page);
  }
}
