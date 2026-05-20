package uk.gov.justice.laa.bulkclaim.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

public final class BulkInProgressPage extends BasePage {

  public BulkInProgressPage(Page page) {
    super(page);
  }

  public void waitForReady() {
    page.locator("h1.moj-interruption-card__heading").waitFor();
  }

  public String heading() {
    return page.locator("h1.moj-interruption-card__heading").innerText().trim();
  }

  public String dateOfUpload() {
    return page.locator("#bulk-submission-date").innerText().trim();
  }

  public String submissionReference() {
    return page.locator("#bulk-submission-id").innerText().trim();
  }

  public String fileName() {
    return page.locator("#bulk-submission-file-name").innerText().trim();
  }

  public boolean goToSearchVisible() {
    Locator button = page.locator("#go-to-search-button");
    return button.count() > 0 && button.isVisible();
  }

  public boolean copyReferenceVisible() {
    Locator link = page.locator("a:has-text('Copy submission reference')");
    return link.count() > 0 && link.isVisible();
  }

  public void clickGoToSearch() {
    page.locator("#go-to-search-button").click();
  }

  public void clickCopyReference() {
    page.locator("a:has-text('Copy submission reference')").click();
  }
}

