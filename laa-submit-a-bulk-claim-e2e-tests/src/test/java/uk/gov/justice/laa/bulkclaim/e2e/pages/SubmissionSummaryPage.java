package uk.gov.justice.laa.bulkclaim.e2e.pages;

import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SubmissionSummaryPage extends BasePage {

  public SubmissionSummaryPage(Page page) {
    super(page);
  }

  public void waitForReady() {
    long deadline = System.currentTimeMillis() + 120_000;
    while (System.currentTimeMillis() < deadline) {
      String url = page.url();
      if (url.contains("upload-is-being-checked")) {
        page.waitForTimeout(1500);
        page.reload();
        continue;
      }

      boolean hasSummaryHeading = page.locator("h1:has-text('Submission summary')").count() > 0;
      boolean hasSummaryRows = page.locator(".govuk-summary-list__row").count() > 0;
      if (hasSummaryHeading && hasSummaryRows) {
        page.locator("h1:has-text('Submission summary')").first().waitFor();
        return;
      }

      page.waitForTimeout(500);
    }

    String body = page.locator("body").innerText();
    String snippet = body.length() > 600 ? body.substring(0, 600) + "..." : body;
    throw new AssertionError(
        "Timed out waiting for submission summary page. URL: " + page.url() + " | body snippet: " + snippet);
  }

  public void verifySuccessBanner() {
    Locator banner = page.locator(".govuk-notification-banner--success").first();
    if (banner.count() == 0 || !banner.isVisible()) {
      throw new AssertionError("Expected success banner on submission summary page");
    }
  }

  public void verifyErrorBanner(int expectedErrors) {
    Locator banner = page.locator(".moj-alert--error, .govuk-error-summary, [role='alert']").first();
    if (banner.count() == 0 || !banner.isVisible()) {
      throw new AssertionError("Expected error banner on submission summary page");
    }
    String text = banner.innerText();
    if (!text.contains(String.valueOf(expectedErrors))) {
      throw new AssertionError("Expected error banner to mention " + expectedErrors + " error(s). Actual: " + text);
    }
  }

  public void verifyWarningBanner(int expectedWarnings) {
    Locator banner = page.locator(".moj-alert--warning, .govuk-warning-text").first();
    if (banner.count() == 0 || !banner.isVisible()) {
      throw new AssertionError("Expected warning banner on submission summary page");
    }
    String text = page.locator("body").innerText();
    if (!text.contains(String.valueOf(expectedWarnings))) {
      throw new AssertionError("Expected warning text to mention " + expectedWarnings + " warning(s)");
    }
  }

  public boolean containsAreaOfLaw(String areaOfLaw) {
    String normalizedExpected = areaOfLaw == null ? "" : areaOfLaw.trim().toLowerCase();
    String summaryArea = getSummaryData().getOrDefault("Area of law", "").trim().toLowerCase();
    if (!summaryArea.isBlank()) {
      return summaryArea.contains(normalizedExpected);
    }
    String body = page.locator("body").innerText().toLowerCase();
    return body.contains(normalizedExpected);
  }

  public Map<String, String> getSummaryData() {
    Map<String, String> summary = new LinkedHashMap<>();
    Locator rows = page.locator(".govuk-summary-list__row");
    for (int i = 0; i < rows.count(); i++) {
      Locator row = rows.nth(i);
      String key = row.locator(".govuk-summary-list__key").innerText().trim();
      String value = row.locator(".govuk-summary-list__value").innerText().trim();
      if (!key.isBlank()) {
        summary.put(key, value);
      }
    }
    return summary;
  }

  public String submissionReference() {
    return getSummaryData().getOrDefault("Submission reference", "");
  }

  public boolean exportVisible() {
    Locator exportButton = page.locator("#export-button, .govuk-button:has-text('Download Claims')").first();
    return exportButton.count() > 0 && exportButton.isVisible();
  }

  public Download exportDownload() {
    Locator exportButton = page.locator("#export-button, .govuk-button:has-text('Download Claims')").first();
    return page.waitForDownload(exportButton::click);
  }

  public void openFirstClaim() {
    openClaimByIndex(0);
  }

  public void openClaimByIndex(int index) {
    Locator rows = page.locator("table.govuk-table tbody tr");
    rows.first().waitFor();
    int rowIndex = Math.max(0, Math.min(index, rows.count() - 1));
    Locator row = rows.nth(rowIndex);
    Locator link = row.locator("a:has-text('View')").first();
    if (link.count() == 0) {
      throw new AssertionError("Expected View link in claim row " + rowIndex);
    }
    link.click();
  }

  public int warningCount() {
    return page.locator(".govuk-warning-text, .moj-alert--warning").count();
  }

  public List<Map<String, String>> getClaimsData() {
    List<Map<String, String>> claims = new ArrayList<>();
    Locator rows = page.locator("table.govuk-table tbody tr");
    for (int i = 0; i < rows.count(); i++) {
      Locator cells = rows.nth(i).locator("td");
      Map<String, String> claim = new LinkedHashMap<>();
      for (int j = 0; j < cells.count(); j++) {
        claim.put("col" + j, cells.nth(j).innerText().replaceAll("\\s+", " ").trim());
      }
      claims.add(claim);
    }
    return claims;
  }

  public int claimsCount() {
    return page.locator("table.govuk-table tbody tr").count();
  }

  public List<String> getSubmissionErrors() {
    List<String> errors = new ArrayList<>();
    Locator errorRows = page.locator("table.govuk-table tbody tr td.govuk-table__cell, .govuk-error-summary, .moj-alert__heading");
    for (String text : errorRows.allInnerTexts()) {
      String normalized = text.trim();
      if (!normalized.isBlank()) {
        errors.add(normalized);
      }
    }
    return errors;
  }

  public Set<String> getPaginatedSubmissionErrors(int pageSize) {
    Set<String> allErrors = new LinkedHashSet<>();
    int pageGuard = 0;
    while (pageGuard++ < 50) {
      allErrors.addAll(getSubmissionErrors());
      Locator rows = page.locator("table.govuk-table tbody tr");
      Locator next = page.locator("a:has-text('Next')").first();
      if (rows.count() < pageSize || next.count() == 0 || !next.isVisible()) {
        break;
      }
      next.click();
      page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
    }
    return allErrors;
  }

  public void expectVoidedTagForClaim(int index) {
    Locator rows = page.locator("table.govuk-table tbody tr");
    rows.first().waitFor();
    int rowIndex = Math.max(0, Math.min(index, rows.count() - 1));
    Locator badge = rows.nth(rowIndex).locator(".moj-badge, .govuk-tag", new Locator.LocatorOptions().setHasText("VOIDED")).first();
    if (badge.count() == 0 || !badge.isVisible()) {
      throw new AssertionError("Expected VOIDED tag for claim row " + (rowIndex + 1));
    }
  }

  public List<Map<String, String>> getMatterStartsData() {
    openMatterStartsTab();
    List<Map<String, String>> data = new ArrayList<>();
    Locator rows = page.locator("#matter-starts + dl .govuk-summary-list__row");
    for (int i = 0; i < rows.count(); i++) {
      Locator row = rows.nth(i);
      Map<String, String> entry = new LinkedHashMap<>();
      entry.put("code", row.locator(".govuk-summary-list__key").innerText().trim());
      entry.put("count", row.locator(".govuk-summary-list__value").innerText().trim());
      data.add(entry);
    }
    return data;
  }

  public void ensureMatterStartsTabHidden() {
    Locator tab = page.locator(".moj-sub-navigation__link", new Page.LocatorOptions().setHasText("Matter starts"));
    if (tab.count() > 0 && tab.first().isVisible()) {
      throw new AssertionError("Expected Matter starts tab to be hidden");
    }
  }

  public String validateNoMatterStartsMessage() {
    openMatterStartsTab();
    Locator message = page.locator("#matter-starts ~ p.govuk-body").first();
    if (message.count() == 0) {
      throw new AssertionError("Expected no matter starts message");
    }
    return message.innerText().trim();
  }

  private void openMatterStartsTab() {
    Locator tab = page.locator(".moj-sub-navigation__link", new Page.LocatorOptions().setHasText("Matter starts")).first();
    if (tab.count() > 0 && tab.isVisible()) {
      tab.click();
      page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
    }
  }
}
