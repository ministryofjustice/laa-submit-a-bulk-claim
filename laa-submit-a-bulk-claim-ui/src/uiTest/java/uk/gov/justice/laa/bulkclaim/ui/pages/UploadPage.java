package uk.gov.justice.laa.bulkclaim.ui.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.FilePayload;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/** Page object for bulk upload journeys. */
public final class UploadPage extends BasePage {

  public UploadPage(Page page) {
    super(page);
  }

  public void open(String appBaseUrl) {
    page.navigate(appBaseUrl + "/upload");
  }

  public void uploadFileAndSubmit(Path filePath) {
    page.locator("input[type='file']").first().setInputFiles(filePath);
    page.locator("form.form button.govuk-button").click();
  }

  public void uploadFileAndSubmit(String fileName, String mimeType, String content) {
    FilePayload payload =
        new FilePayload(
            fileName,
            mimeType,
            content.getBytes(StandardCharsets.UTF_8));
    page.locator("input[type='file']").first().setInputFiles(payload);
    page.locator("form.form button.govuk-button").click();
  }

  public void submitWithoutSelectingFile() {
    page.locator("form.form button.govuk-button").click();
  }

  public void waitForUploadPage() {
    page.waitForURL("**/upload");
  }

  public void waitForUploadBeingCheckedPage() {
    page.waitForURL("**/upload-is-being-checked");
  }
}

