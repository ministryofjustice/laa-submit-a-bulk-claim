package uk.gov.justice.laa.payments.submit.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;

@Getter
public class SearchPage extends BasePage {

  private final Locator searchButton;
  private final Locator resultsTable;

  public SearchPage(Page page) {
    super(page, "Search for a submission");
    searchButton = page.locator("#searchButton");
    resultsTable = page.locator(".govuk-table");
  }

  public void clickOnLink(int index) {

    resultsTable.locator(".govuk-table__cell .govuk-link--no-visited-state").nth(index).click();
  }
}
