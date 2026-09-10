package uk.gov.justice.laa.payments.submit.e2e.pages;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;

@Getter
public class SearchPage extends BasePage {

  private final Locator searchButton;
  private final Locator resultsTable;
  private final Locator areaOfLawSelect;
  private final Locator resultsHeading;

  public SearchPage(Page page) {
    super(page, "Search for a submission");
    searchButton = page.locator("#searchButton");
    resultsTable = page.locator(".govuk-table");
    areaOfLawSelect = page.locator("#area-of-law");
    resultsHeading = page.locator("#results-heading");
  }

  public void clickOnLink(int index) {
    resultsTable.locator(".govuk-table__cell .govuk-link--no-visited-state").nth(index).click();
  }

  public void assertTotalSubmissions(int total){
    assertThat(resultsHeading).isVisible();
    var expectedTest =
        total == 1 ? "1 Search result" : total + " Search results";
    assertThat(resultsHeading).hasText(expectedTest);
  }
}
