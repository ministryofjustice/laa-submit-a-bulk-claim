package uk.gov.justice.laa.bulkclaim.e2e.pages;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.FilePayload;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class BulkImportPage extends BasePage {
  private static final String FILE_INPUT_SELECTOR = "#file-input-input";
  private static final String PRIMARY_BUTTON_SELECTOR = "form.form button.govuk-button";

  public BulkImportPage(Page page) {
    super(page);
  }

  public void open(String baseUrl) {
    String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    System.out.println("[BulkImportPage] open() baseUrl=" + baseUrl + ", normalizedBase=" + normalizedBase + ", currentUrl=" + page.url());
    page.navigate( "/upload");
    page.waitForURL(url -> url.contains("/upload"));
    page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
    System.out.println("[BulkImportPage] open() navigatedUrl=" + page.url());
  }

  public void uploadAndSubmit(Path file) {
    page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
    page.waitForSelector(
        FILE_INPUT_SELECTOR,
        new com.microsoft.playwright.Page.WaitForSelectorOptions()
            .setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED)
            .setTimeout(60_000));
    page.setInputFiles(FILE_INPUT_SELECTOR, file);
    assertFileAttached(page.locator(FILE_INPUT_SELECTOR).first(), file);
    clickPrimaryButton();
  }

  public void uploadAndSubmit(Path file, String mimeType) {
    try {
      page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
      page.waitForSelector(
          FILE_INPUT_SELECTOR,
          new com.microsoft.playwright.Page.WaitForSelectorOptions()
              .setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED)
              .setTimeout(60_000));
      byte[] bytes = java.nio.file.Files.readAllBytes(file);
      page.setInputFiles(FILE_INPUT_SELECTOR, new FilePayload(file.getFileName().toString(), mimeType, bytes));
      assertFileAttached(page.locator(FILE_INPUT_SELECTOR).first(), file);
      clickPrimaryButton();
    } catch (Exception e) {
      throw new IllegalStateException("Unable to upload with MIME", e);
    }
  }

  public void uploadAndSubmit(String fileName, String mimeType, String content) {
    page.waitForSelector(
        FILE_INPUT_SELECTOR,
        new com.microsoft.playwright.Page.WaitForSelectorOptions()
            .setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED)
            .setTimeout(60_000));
    page.setInputFiles(
        FILE_INPUT_SELECTOR,
        new FilePayload(fileName, mimeType, content.getBytes(StandardCharsets.UTF_8)));
    com.microsoft.playwright.Locator fileInput = page.locator(FILE_INPUT_SELECTOR).first();
    if (fileInput.inputValue().isBlank()) {
      throw new IllegalStateException("File was not attached before upload");
    }
    clickPrimaryButton();
  }

  private void assertFileAttached(com.microsoft.playwright.Locator fileInput, Path file) {
    String selected = fileInput.inputValue();
    if (selected == null || selected.isBlank() || !selected.contains(file.getFileName().toString())) {
      throw new IllegalStateException("Expected file to be attached before upload: " + file.getFileName());
    }
  }

  public void submitWithoutFile() {
    clickPrimaryButton();
  }

  private void clickPrimaryButton() {
    page.locator(PRIMARY_BUTTON_SELECTOR).first().click();
  }
}
