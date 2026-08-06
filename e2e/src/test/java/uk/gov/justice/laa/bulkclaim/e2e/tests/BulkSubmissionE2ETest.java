package uk.gov.justice.laa.bulkclaim.e2e.tests;

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.e2e.base.BaseTest;
import uk.gov.justice.laa.bulkclaim.e2e.pages.LandingPagePage;
import uk.gov.justice.laa.bulkclaim.e2e.pages.SubmissionDetailPage;
import uk.gov.justice.laa.bulkclaim.e2e.pages.UploadBeingCheckedPage;
import uk.gov.justice.laa.bulkclaim.e2e.pages.UploadPage;

public class BulkSubmissionE2ETest extends BaseTest {

  @Test
  public void bulkSubmissionForCrimeLowerAccepted() {
    var landingPage = new LandingPagePage(page);
    landingPage.getStartNowButton().click();

    var upload = new UploadPage(page);
    var csvPath = Paths.get("../docs/sample-data/crime-lower-may-2026.csv").toAbsolutePath();
    upload.uploadFile(csvPath);
    
    upload.getContinueButton().click();
    
    var uploadBeingChecked = new UploadBeingCheckedPage(page);

    var submissionDetailPage = new SubmissionDetailPage(page);
    submissionDetailPage.assertSubmissionAccepted();
  }

  @Test
  public void bulkSubmissionForLegalHelpAccepted() {
    var landingPage = new LandingPagePage(page);
    landingPage.getStartNowButton().click();

    var upload = new UploadPage(page);
    var csvPath = Paths.get("../docs/sample-data/legal-help-june-2026.csv").toAbsolutePath();
    upload.uploadFile(csvPath);

    upload.getContinueButton().click();

    var uploadBeingChecked = new UploadBeingCheckedPage(page);

    var submissionDetailPage = new SubmissionDetailPage(page);
    submissionDetailPage.assertSubmissionAccepted();
  }

  @Test
  public void bulkSubmissionForMediationAccepted() {
    var landingPage = new LandingPagePage(page);
    landingPage.getStartNowButton().click();

    var upload = new UploadPage(page);
    var csvPath = Paths.get("../docs/sample-data/mediation-june-2026.csv").toAbsolutePath();
    upload.uploadFile(csvPath);

    upload.getContinueButton().click();

    var uploadBeingChecked = new UploadBeingCheckedPage(page);

    var submissionDetailPage = new SubmissionDetailPage(page);
    submissionDetailPage.assertSubmissionAccepted();
  }
}
