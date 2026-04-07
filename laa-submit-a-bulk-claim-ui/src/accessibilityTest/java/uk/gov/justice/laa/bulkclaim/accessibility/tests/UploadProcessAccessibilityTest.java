package uk.gov.justice.laa.bulkclaim.accessibility.tests;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.accessibility.helpers.AbstractAccessibilityTest;
import uk.gov.justice.laa.bulkclaim.accessibility.helpers.AccessibilityAxeHelper;
import uk.gov.justice.laa.bulkclaim.accessibility.pages.UploadPage;

@DisplayName("Feature: Upload a bulk claim file page (UP)")
class UploadProcessAccessibilityTest extends AbstractAccessibilityTest {

  @Test
  @DisplayName("Scenario: UP1 accessibility checks")
  void uploadPageAccessibilityChecks() throws IOException {
    UploadPage uploadPage = new UploadPage(page);
    uploadPage.open(appUrl(""));
    AccessibilityAxeHelper.assertAccessible(page, "upload-page", List.of());
  }

  @Test
  @DisplayName("Scenario: UP1-ERR accessibility checks")
  void uploadValidationErrorAccessibilityChecks() throws IOException {
    UploadPage uploadPage = new UploadPage(page);
    uploadPage.open(appUrl(""));

    Path forbiddenOfficeFile =
        writeFile("forbidden-office.csv", "header-1,header-2\nvalue-1,value-2\n");
    uploadPage.uploadFileAndSubmit(forbiddenOfficeFile);
    uploadPage.waitForUploadPage();

    AccessibilityAxeHelper.assertAccessible(
        page,
        "upload-page-validation-error",
        List.of("landmark-one-main", "page-has-heading-one", "bypass"));
  }

  @Test
  @DisplayName("Scenario: UP2 accessibility checks")
  void uploadInProgressAccessibilityChecks() throws IOException {
    UploadPage uploadPage = new UploadPage(page);
    uploadPage.open(appUrl(""));
    Path validFile = writeFile("validFile.csv", "header-1,header-2\nvalue-1,value-2\n");
    uploadPage.uploadFileAndSubmit(validFile);
    uploadPage.waitForUploadBeingCheckedPage();
    AccessibilityAxeHelper.assertAccessible(page, "upload-being-checked", List.of());
  }
}
