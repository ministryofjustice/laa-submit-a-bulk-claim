package uk.gov.justice.laa.bulkclaim.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;

@Getter
public class SearchPage extends BasePage {

  private final Locator searchButton;

  public SearchPage(Page page) {
    super(page, "Search for a submission");
    searchButton = page.locator("#searchButton");
  }

  public void clickSearch() {
    searchButton.click();
  }
}
