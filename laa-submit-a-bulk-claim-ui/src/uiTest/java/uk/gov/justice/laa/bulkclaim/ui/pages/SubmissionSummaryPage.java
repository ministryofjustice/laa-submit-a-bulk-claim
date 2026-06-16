package uk.gov.justice.laa.bulkclaim.ui.pages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

/** Page object for accepted and invalid submission summary pages. */
public final class SubmissionSummaryPage extends BasePage {

  private static final int TIMEOUT_10_SECONDS = 10000;

  public SubmissionSummaryPage(Page page) {
    super(page);
  }

  public void waitForSummaryPage() {
    page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    page.locator(".govuk-summary-list")
        .waitFor(
            new Locator.WaitForOptions()
                .setTimeout(TIMEOUT_10_SECONDS)
                .setState(WaitForSelectorState.VISIBLE));
  }

  public void assertAreaOfLaw(String expectedAreaOfLaw) {
    assertEquals(expectedAreaOfLaw, summaryValue("Area of law"));
  }

  public void assertExportVisible() {
    Locator exportButton = exportButton();
    exportButton.waitFor(
        new Locator.WaitForOptions()
            .setTimeout(TIMEOUT_10_SECONDS)
            .setState(WaitForSelectorState.VISIBLE));
    assertTrue(exportButton.count() > 0, "Expected export button to be visible");
  }

  public void assertExportHidden() {
    assertTrue(exportButton().count() == 0 || !exportButton().first().isVisible(),
        "Expected export button to be hidden on invalid submission");
  }

  public Download downloadExport() {
    assertExportVisible();
    return page.waitForDownload(() -> exportButton().first().click());
  }

  public String submissionReference() {
    return summaryValue("Submission reference");
  }

  public String officeAccount() {
    return summaryValue("Account");
  }

  private Locator exportButton() {
    return page.locator("#export-button");
  }

  private String summaryValue(String label) {
    return page.locator("dt:has-text('" + label + "') + dd").first().textContent().trim();
  }
}

