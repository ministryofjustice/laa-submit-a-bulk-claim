package uk.gov.justice.laa.bulkclaim.accessibility.pages;

import com.microsoft.playwright.Page;
import java.nio.file.Path;

public final class UploadPage {

  private final Page page;

  public UploadPage(Page page) {
    this.page = page;
  }

  public void open(String appBaseUrl) {
    page.navigate(appBaseUrl + "/upload");
  }

  public void uploadFileAndSubmit(Path filePath) {
    page.locator("input[type='file']").first().setInputFiles(filePath);
    page.locator("form.form button.govuk-button").click();
  }

  public void waitForUploadPage() {
    page.waitForURL("**/upload");
  }

  public void waitForUploadBeingCheckedPage() {
    page.waitForURL("**/upload-is-being-checked");
  }
}
