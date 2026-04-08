package uk.gov.justice.laa.bulkclaim.accessibility.tests;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.accessibility.helpers.AbstractAccessibilityTest;
import uk.gov.justice.laa.bulkclaim.accessibility.helpers.AccessibilityAxeHelper;
import uk.gov.justice.laa.bulkclaim.accessibility.pages.UploadPage;

@DisplayName("Feature: Upload a bulk claim file page (UP)")
class UploadProcessAccessibilityTest extends AbstractAccessibilityTest {

  private static final List<String> UPLOAD_VALIDATION_ERROR_RULE_IGNORES =
      List.of("landmark-one-main", "page-has-heading-one", "bypass");
  private UploadPage uploadPage;

  @BeforeEach
  void setUpPageObject() {
    uploadPage = new UploadPage(page);
  }

  @Test
  @DisplayName("Scenario: UP1 accessibility checks")
  void uploadPageAccessibilityChecks() throws IOException {
    uploadPage.open(appLandingUrl());
    AccessibilityAxeHelper.assertAccessible(page, "upload-page");
  }

  @Test
  @DisplayName("Scenario: UP1-ERR accessibility checks")
  void uploadValidationErrorAccessibilityChecks() throws IOException {
    uploadPage.open(appLandingUrl());

    Path forbiddenOfficeFile =
        writeFile("forbidden-office.csv", "header-1,header-2\nvalue-1,value-2\n");
    uploadPage.uploadFileAndSubmit(forbiddenOfficeFile);
    uploadPage.waitForUploadPage();

    AccessibilityAxeHelper.assertAccessible(
        page, "upload-page-validation-error", UPLOAD_VALIDATION_ERROR_RULE_IGNORES);
  }

  @Test
  @DisplayName("Scenario: UP2 accessibility checks")
  void uploadInProgressAccessibilityChecks() throws IOException {
    uploadPage.open(appLandingUrl());
    Path validFile = writeFile("validFile.csv", "header-1,header-2\nvalue-1,value-2\n");
    uploadPage.uploadFileAndSubmit(validFile);
    uploadPage.waitForUploadBeingCheckedPage();
    AccessibilityAxeHelper.assertAccessible(page, "upload-being-checked");
  }
}
