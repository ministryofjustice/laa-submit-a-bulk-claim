package uk.gov.justice.laa.bulkclaim.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;

@Getter
public class SearchResultsPage extends BasePage {

  private final Page page;
  private final Locator resultsTable;

  public SearchResultsPage(Page page) {
    super(page, "Search for a submission");
    this.page = page;
    this.resultsTable = page.locator(".govuk-table");
  }
}
