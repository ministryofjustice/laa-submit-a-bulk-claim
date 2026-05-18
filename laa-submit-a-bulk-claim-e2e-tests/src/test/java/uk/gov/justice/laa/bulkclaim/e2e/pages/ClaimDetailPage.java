package uk.gov.justice.laa.bulkclaim.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.util.List;

public final class ClaimDetailPage extends BasePage {

  public ClaimDetailPage(Page page) {
    super(page);
  }

  public void waitForReady() {
    page.locator("h1, table").first().waitFor();
  }

  public boolean hasVoidedBanner() {
    String text = page.locator("body").innerText().toUpperCase();
    return text.contains("VOIDED") || text.contains("THIS CLAIM HAS BEEN VOIDED");
  }

  public void expectVoidedBanner() {
    if (!hasVoidedBanner()) {
      throw new AssertionError("Expected voided claim banner");
    }
  }

  public void assertFeeHeadingsPresent(List<String> headings) {
    String all = String.join("\n", page.locator("table th").allInnerTexts()).toLowerCase();
    for (String heading : headings) {
      if (!all.contains(heading.toLowerCase())) {
        throw new AssertionError("Missing fee heading: " + heading);
      }
    }
  }

  public FeeCalculationRow getFeeCalculationRow(String targetLabel) {
    Locator rows = page.locator("table.govuk-table tbody tr");
    int rowCount = rows.count();
    String normalizedTarget = targetLabel.trim().toLowerCase();

    for (int i = 0; i < rowCount; i++) {
      Locator row = rows.nth(i);
      String labelText = row.locator("th").innerText().replaceAll("\\s+", " ").trim();
      if (labelText.isBlank() || !labelText.toLowerCase().contains(normalizedTarget)) {
        continue;
      }

      Locator cells = row.locator("td");
      return new FeeCalculationRow(
          labelText,
          cellText(cells, 0),
          cellText(cells, 1),
          cellText(cells, 2));
    }

    return null;
  }

  public void voidClaimUiIfAvailable() {
    Locator button = page.locator("button:has-text('Void')").first();
    if (button.count() > 0 && button.isVisible()) {
      button.click();
      Locator confirm = page.locator("button:has-text('Confirm'), button:has-text('Yes')").first();
      if (confirm.count() > 0) {
        confirm.click();
      }
    }
  }

  private String cellText(Locator cells, int index) {
    if (cells.count() <= index) {
      return "";
    }
    return cells.nth(index).innerText().replaceAll("\\s+", " ").trim();
  }

  public record FeeCalculationRow(String label, String entered, String calculated, String notes) {}
}
