package uk.gov.justice.laa.bulkclaim.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class SearchPage extends BasePage {

  public SearchPage(Page page) {
    super(page);
  }

  public void open(String baseUrl) {
    page.navigate(baseUrl + "/search");
  }

  public void clickSearch() {
    clickButtonByName("Search");
  }

  public void fillSubmissionPeriod(String value) {
    if (value == null || value.isBlank()) {
      return;
    }
    if (page.locator("#submission-period").count() > 0) {
      page.locator("#submission-period").fill(value);
      page.locator("#submission-period").press("Enter");
    }
  }

  public void selectAreaOfLaw(String value) {
    if (value == null || value.isBlank()) {
      return;
    }
    if (page.locator("#area-of-law").count() > 0) {
      page.selectOption("#area-of-law", value);
    }
  }

  public void selectStatus(String status) {
    String normalized = status == null ? "" : status.toLowerCase();
    if (normalized.contains("succeed") && page.locator("#succeeded-radio-option").count() > 0) {
      page.check("#succeeded-radio-option");
    } else if (normalized.contains("fail") && page.locator("#failed-radio-option").count() > 0) {
      page.check("#failed-radio-option");
    } else if (page.locator("#all-radio-option").count() > 0) {
      page.check("#all-radio-option");
    }
  }

  public void deselectAllOffices() {
    openOfficeChooserIfNeeded();
    Locator checkboxes = page.locator("#offices-input-container input[type='checkbox']");
    for (int i = 0; i < checkboxes.count(); i++) {
      Locator cb = checkboxes.nth(i);
      if (cb.isChecked()) {
        cb.uncheck();
      }
    }
  }

  public void selectOffice(String office) {
    if (office == null || office.isBlank()) {
      return;
    }
    openOfficeChooserIfNeeded();
    String selector = "#offices-input-container input[type='checkbox'][value='" + office + "']";
    if (page.locator(selector).count() > 0) {
      page.check(selector);
    }
  }

  public boolean hasResults() {
    return page.locator("table.govuk-table tbody tr").count() > 0;
  }

  public void expectResultsVisible() {
    page.locator("#results-heading").waitFor();
    if (page.locator("table.govuk-table").count() == 0) {
      throw new AssertionError("Expected results table to be visible");
    }
  }

  public void expectValidationSummaryVisible() {
    if (page.locator(".govuk-error-summary").count() == 0) {
      throw new AssertionError("Expected validation summary to be visible");
    }
  }

  public void expectValidationErrorMessage(String text) {
    String body = page.locator("body").innerText();
    if (!body.contains(text)) {
      throw new AssertionError("Expected validation message: " + text);
    }
  }

  public void expectTableHasCorrectHeaders() {
    List<String> expected = List.of(
        "Date submitted",
        "Office account",
        "Area of law",
        "Submission period",
        "Status");
    List<String> actual = page.locator("table.govuk-table thead th").allInnerTexts().stream()
        .map(String::trim)
        .toList();
    if (!actual.containsAll(expected)) {
      throw new AssertionError("Expected search table headers. Actual: " + actual);
    }
  }

  public void expectSingleSearchResult() {
    int count = page.locator("table.govuk-table tbody tr").count();
    if (count != 1) {
      throw new AssertionError("Expected exactly one search result, got " + count);
    }
  }

  public void expectSubmissionLinkMatches(String submissionId) {
    String href = page.locator("table.govuk-table tbody tr:first-child td:first-child a").first().getAttribute("href");
    if (href == null || !href.contains(submissionId)) {
      throw new AssertionError("Expected first result href to contain submissionId " + submissionId + ", got " + href);
    }
  }

  public void openFirstSubmission() {
    Locator viewLink = page.locator("a:has-text('View')").first();
    if (viewLink.count() > 0) {
      viewLink.click();
      return;
    }

    Locator firstRowLink = page.locator("table.govuk-table tbody tr td a").first();
    if (firstRowLink.count() > 0) {
      firstRowLink.click();
    }
  }

  public List<String> collectAllVisibleSubmissionIds() {
    Set<String> ids = new LinkedHashSet<>();
    int pageGuard = 0;

    while (pageGuard++ < 50) {
      Locator links = page.locator("table.govuk-table tbody tr td a[href*='/submission/']");
      for (int i = 0; i < links.count(); i++) {
        String href = links.nth(i).getAttribute("href");
        if (href == null) {
          continue;
        }
        String[] parts = href.split("/");
        if (parts.length > 0) {
          ids.add(parts[parts.length - 1]);
        }
      }

      Locator next = page.locator("a:has-text('Next')").first();
      if (next.count() == 0 || !next.isVisible()) {
        break;
      }
      next.click();
      page.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
    }

    return new ArrayList<>(ids);
  }

  public void verifyNoSubmissionsMessage() {
    Locator noResults = page.locator("p.govuk-body", new Page.LocatorOptions().setHasText("No submissions were found."));
    if (noResults.count() == 0 || !noResults.first().isVisible()) {
      throw new AssertionError("Expected no submissions found message");
    }
  }

  private void openOfficeChooserIfNeeded() {
    if (page.locator("#offices-input-container").count() == 0 || !page.locator("#offices-input-container").isVisible()) {
      Locator details = page.locator("#choose-office-account-details-summary");
      if (details.count() > 0) {
        details.click();
      }
    }
  }
}
