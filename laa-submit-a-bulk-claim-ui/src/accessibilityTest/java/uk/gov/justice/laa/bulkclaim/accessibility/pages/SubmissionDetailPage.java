package uk.gov.justice.laa.bulkclaim.accessibility.pages;

import com.microsoft.playwright.Page;

/** Page object for the submission detail page tabs. */
public final class SubmissionDetailPage {

  private final Page page;

  public SubmissionDetailPage(Page page) {
    this.page = page;
  }

  public void openMessagesTab() {
    page.locator("a[href*='navTab=CLAIM_MESSAGES']").first().click();
  }

  public void openMatterStartsTab() {
    page.locator("a[href*='navTab=MATTER_STARTS']").first().click();
  }
}
