package uk.gov.justice.laa.bulkclaim.ui.pages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Page object for handling claim table interactions and sorting functionality on submission
 * detail pages.
 */
public class ClaimsTablePage extends BasePage {

  // Scope to the submission claim details table (there are other govuk tables on the page).
  private static final String TABLE_SELECTOR = "#claims-table + table.govuk-table";
  private static final String THEAD_SELECTOR = "thead th";
  private static final String TBODY_ROWS_SELECTOR = "tbody tr";
  private static final String TABLE_CELLS_SELECTOR = "td";
  private static final String SORTABLE_HEADER_SELECTOR = "th[aria-sort]";
  private static final String SORT_LINK_SELECTOR = "a.govuk-link--sort";
  private static final String NEXT_PAGE_SELECTOR = "a:has-text('Next')";
  private static final String PREV_PAGE_SELECTOR = "a:has-text('Previous')";
  private static final String SORT_VALUE_ATTRIBUTE = "data-sort-value";
  private static final int RETRY_SORT_ATTEMPTS = 3;
  private static final int TIMEOUT_10_SECONDS = 10000;

  public ClaimsTablePage(Page page) {
    super(page);
  }

  /**
   * Waits for the claims table to be visible on the page.
   */
  public void waitForClaimsTable() {
    try {
      page.locator(TABLE_SELECTOR)
          .waitFor(new Locator.WaitForOptions().setTimeout(TIMEOUT_10_SECONDS)
              .setState(WaitForSelectorState.VISIBLE));
      return;
    } catch (TimeoutError ignored) {
      // Continue with fallback to ensure we're on the claims tab.
    }

    Locator claimDetailsTab = page.locator("a[href*='navTab=CLAIM_DETAILS']").first();
    if (claimDetailsTab.count() > 0) {
      claimDetailsTab.click();
      page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    try {
      page.locator(TABLE_SELECTOR)
          .waitFor(new Locator.WaitForOptions().setTimeout(TIMEOUT_10_SECONDS)
              .setState(WaitForSelectorState.VISIBLE));
    } catch (TimeoutError e) {
      throw new AssertionError(
          "Claims table not visible. URL=" + page.url() + ", page heading="
              + page.locator("h1").first().textContent(),
          e);
    }
  }

  /**
   * Checks if a sortable header exists for the given header text.
   *
   * @param headerText the text of the header column
   * @return true if header is sortable, false otherwise
   */
  public boolean hasSortableHeader(String headerText) {
    Locator header =
        page.locator(SORTABLE_HEADER_SELECTOR,
            new Page.LocatorOptions().setHas(
                page.locator(SORT_LINK_SELECTOR + ":has-text(\"" + headerText + "\")")));
    return header.count() > 0;
  }

  /**
   * Sorts a column by header text in the specified direction.
   *
   * @param headerText the text of the header to sort by
   * @param direction "ascending" or "descending"
   * @throws AssertionError if header not found or sort fails
   */
  public void sortByHeader(String headerText, String direction) {
    Locator header =
        page.locator(SORTABLE_HEADER_SELECTOR,
            new Page.LocatorOptions().setHas(
                page.locator(SORT_LINK_SELECTOR + ":has-text(\"" + headerText + "\")")));

    assertTrue(header.count() > 0, "Sortable header '" + headerText + "' was not found");

    Locator link = header.locator(SORT_LINK_SELECTOR).first();
    String expectedDirectionSuffix = "ascending".equals(direction) ? "asc" : "desc";

    for (int i = 0; i < RETRY_SORT_ATTEMPTS; i++) {
      String hrefBeforeClick = link.getAttribute("href");
      if (hrefBeforeClick == null || !hrefHasDirection(hrefBeforeClick, expectedDirectionSuffix)) {
        // Toggle sort state until the next click will request the target direction.
        link.click();
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        continue;
      }

      link.click();
      page.waitForLoadState(LoadState.DOMCONTENTLOADED);

      String currentUrl = page.url();
      if (hrefHasDirection(currentUrl, expectedDirectionSuffix)) {
        return;
      }
    }

    throw new AssertionError("Could not sort " + headerText + " to " + direction);
  }

  /**
   * Gets first row text value for the given header.
   *
   * @param headerText column header text
   * @return first row value as trimmed text
   */
  public String getFirstRowTextValue(String headerText) {
    List<String> values = getColumnTextValues(headerText);
    assertTrue(!values.isEmpty(), "No rows available for column " + headerText);
    return values.get(0);
  }

  /**
   * Gets first row numeric value for the given header.
   *
   * @param headerText column header text
   * @return first row value as number
   */
  public double getFirstRowNumericValue(String headerText) {
    List<Double> values = getColumnNumericValues(headerText);
    assertTrue(!values.isEmpty(), "No numeric rows available for column " + headerText);
    return values.get(0);
  }

  /**
   * Gets the column index for a header by its text.
   *
   * @param headerText the text of the header
   * @return the zero-based column index
   * @throws AssertionError if header not found
   */
  public int getColumnIndex(String headerText) {
    Locator headers = page.locator(TABLE_SELECTOR + " " + THEAD_SELECTOR);
    int count = headers.count();
    String normalizedTarget = headerText.toLowerCase();

    for (int i = 0; i < count; i++) {
      String text = headers.nth(i).textContent().replaceAll("\\s+", " ").trim();
      if (text.toLowerCase().contains(normalizedTarget)) {
        return i;
      }
    }

    throw new AssertionError("Column '" + headerText + "' not found");
  }

  /**
   * Gets all text values in a column by header name on the current page.
   *
   * @param headerText the header text to locate the column
   * @return list of text values in the column
   */
  public List<String> getColumnTextValues(String headerText) {
    int columnIndex = getColumnIndex(headerText);
    Locator rows = page.locator(TABLE_SELECTOR + " " + TBODY_ROWS_SELECTOR);
    int rowCount = rows.count();
    List<String> values = new ArrayList<>();

    for (int i = 0; i < rowCount; i++) {
      Locator cell = rows.nth(i).locator(TABLE_CELLS_SELECTOR).nth(columnIndex);
      String value = cell.textContent().replaceAll("\\s+", " ").trim();
      values.add(value);
    }

    return values;
  }

  /**
   * Gets all numeric values in a column by header name on the current page using data-sort-value.
   *
   * @param headerText the header text to locate the column
   * @return list of numeric values in the column
   */
  public List<Double> getColumnNumericValues(String headerText) {
    int columnIndex = getColumnIndex(headerText);
    Locator rows = page.locator(TABLE_SELECTOR + " " + TBODY_ROWS_SELECTOR);
    int rowCount = rows.count();
    List<Double> values = new ArrayList<>();

    for (int i = 0; i < rowCount; i++) {
      Locator cell = rows.nth(i).locator(TABLE_CELLS_SELECTOR).nth(columnIndex);
      String sortValue = cell.getAttribute(SORT_VALUE_ATTRIBUTE);
      if (sortValue != null && !sortValue.isEmpty()) {
        values.add(Double.parseDouble(sortValue));
      }
    }

    return values;
  }

  /**
   * Validates that text values in a column are sorted in the specified direction.
   *
   * @param headerText the header to validate
   * @param direction "ascending" or "descending"
   */
  public void validateTextSorting(String headerText, String direction) {
    List<String> values = getColumnTextValues(headerText);
    List<String> expected = new ArrayList<>(values);

    if ("ascending".equals(direction)) {
      expected.sort(String::compareTo);
    } else {
      expected.sort(Comparator.reverseOrder());
    }

    assertEquals(expected, values, "Text sorting not correct for " + headerText + " in " + direction);
  }

  /**
   * Validates that numeric values in a column are sorted in the specified direction.
   *
   * @param headerText the header to validate
   * @param direction "ascending" or "descending"
   */
  public void validateNumericSorting(String headerText, String direction) {
    List<Double> values = getColumnNumericValues(headerText);
    List<Double> expected = new ArrayList<>(values);

    if ("ascending".equals(direction)) {
      expected.sort(Double::compareTo);
    } else {
      expected.sort(Comparator.reverseOrder());
    }

    assertEquals(expected, values, "Numeric sorting not correct for " + headerText + " in " + direction);
  }

  /**
   * Validates special sorting for "Escape case" field (No < Escaped).
   *
   * @param direction "ascending" or "descending"
   */
  public void validateEscapeCaseSorting(String direction) {
    List<String> values = getColumnTextValues("Escape case");
    List<String> expected = new ArrayList<>(values);

    expected.sort((a, b) -> {
      int rankA = rankEscapeCase(a);
      int rankB = rankEscapeCase(b);
      return "ascending".equals(direction) ? rankA - rankB : rankB - rankA;
    });

    assertEquals(expected, values, "Escape case sorting not correct in " + direction);
  }

  /**
   * Validates special sorting for "Messages" field (with content < empty).
   *
   * @param direction "ascending" or "descending"
   */
  public void validateMessagesSorting(String direction) {
    List<String> values = getColumnTextValues("Messages");
    List<String> expected = new ArrayList<>(values);

    expected.sort((a, b) -> {
      int rankA = rankMessages(a);
      int rankB = rankMessages(b);
      if (rankA != rankB) {
        return "ascending".equals(direction) ? rankA - rankB : rankB - rankA;
      }
      return "ascending".equals(direction) ? a.compareTo(b) : b.compareTo(a);
    });

    assertEquals(expected, values, "Messages sorting not correct in " + direction);
  }

  /**
   * Checks if pagination is available (next or page 2 link exists).
   *
   * @return true if pagination is available
   */
  public boolean hasPagination() {
    Locator nextLink = page.locator(NEXT_PAGE_SELECTOR);
    Locator pageLink = page.locator("a:has-text('2')");
    return nextLink.count() > 0 || pageLink.count() > 0;
  }

  /**
   * Navigates to the first page of claims by clicking Previous until unavailable.
   */
  public void goToFirstPage() {
    Locator prevLink = page.locator(PREV_PAGE_SELECTOR);
    while (prevLink.count() > 0) {
      prevLink.click();
      page.waitForLoadState(LoadState.DOMCONTENTLOADED);
      waitForClaimsTable();
      prevLink = page.locator(PREV_PAGE_SELECTOR);
    }
  }

  /**
   * Navigates to the next page if available.
   *
   * @return true if next page was navigated to, false if no next page
   */
  public boolean goToNextPage() {
    Locator nextLink = page.locator(NEXT_PAGE_SELECTOR);
    if (nextLink.count() == 0) {
      return false;
    }
    nextLink.click();
    page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    waitForClaimsTable();
    return true;
  }

  /**
   * Collects all text values from a column across all pages.
   *
   * @param headerText the header text to locate the column
   * @return list of all text values across all pages
   */
  public List<String> getAllColumnTextValues(String headerText) {
    List<String> allValues = new ArrayList<>();
    goToFirstPage();
    do {
      allValues.addAll(getColumnTextValues(headerText));
    } while (goToNextPage());
    return allValues;
  }

  /**
   * Collects all numeric values from a column across all pages.
   *
   * @param headerText the header text to locate the column
   * @return list of all numeric values across all pages
   */
  public List<Double> getAllColumnNumericValues(String headerText) {
    List<Double> allValues = new ArrayList<>();
    goToFirstPage();
    do {
      allValues.addAll(getColumnNumericValues(headerText));
    } while (goToNextPage());
    return allValues;
  }

  /**
   * Validates text sorting across all pages.
   *
   * @param headerText the header to validate
   * @param direction "ascending" or "descending"
   */
  public void validateTextSortingAcrossPages(String headerText, String direction) {
    List<String> values = getAllColumnTextValues(headerText);
    List<String> expected = new ArrayList<>(values);

    if ("ascending".equals(direction)) {
      expected.sort(String::compareTo);
    } else {
      expected.sort(Comparator.reverseOrder());
    }

    assertEquals(expected, values, "Text sorting not correct across pages for " + headerText);
  }

  /**
   * Validates numeric sorting across all pages.
   *
   * @param headerText the header to validate
   * @param direction "ascending" or "descending"
   */
  public void validateNumericSortingAcrossPages(String headerText, String direction) {
    List<Double> values = getAllColumnNumericValues(headerText);
    List<Double> expected = new ArrayList<>(values);

    if ("ascending".equals(direction)) {
      expected.sort(Double::compareTo);
    } else {
      expected.sort(Comparator.reverseOrder());
    }

    assertEquals(expected, values, "Numeric sorting not correct across pages for " + headerText);
  }

  /**
   * Validates Escape case sorting across all pages.
   *
   * @param direction "ascending" or "descending"
   */
  public void validateEscapeCaseSortingAcrossPages(String direction) {
    goToFirstPage();
    List<String> values = new ArrayList<>();
    do {
      values.addAll(getColumnTextValues("Escape case"));
    } while (goToNextPage());

    List<String> expected = new ArrayList<>(values);
    expected.sort((a, b) -> {
      int rankA = rankEscapeCase(a);
      int rankB = rankEscapeCase(b);
      return "ascending".equals(direction) ? rankA - rankB : rankB - rankA;
    });

    assertEquals(expected, values, "Escape case sorting not correct across pages");
  }

  /**
   * Validates Messages sorting across all pages.
   *
   * @param direction "ascending" or "descending"
   */
  public void validateMessagesSortingAcrossPages(String direction) {
    goToFirstPage();
    List<String> values = new ArrayList<>();
    do {
      values.addAll(getColumnTextValues("Messages"));
    } while (goToNextPage());

    List<String> expected = new ArrayList<>(values);
    expected.sort((a, b) -> {
      int rankA = rankMessages(a);
      int rankB = rankMessages(b);
      if (rankA != rankB) {
        return "ascending".equals(direction) ? rankA - rankB : rankB - rankA;
      }
      return "ascending".equals(direction) ? a.compareTo(b) : b.compareTo(a);
    });

    assertEquals(expected, values, "Messages sorting not correct across pages");
  }

  /**
   * Gets expected sortable headers for a given area of law.
   *
   * @param areaOfLaw the area of law
   * @return map with "text" and "numeric" header lists
   */
  public Map<String, List<String>> getExpectedSortableHeaders(String areaOfLaw) {
    return switch (areaOfLaw.toLowerCase()) {
      case "legal help" ->
          Map.of(
              "text", List.of(
                  "Client Surname",
                  "Client Forename",
                  "UFN",
                  "UCN",
                  "Fee code",
                  "Escape case",
                  "Messages"
              ),
              "numeric", List.of("Calculated value")
          );

      case "crime lower" ->
          Map.of(
              "text", List.of(
                  "Client Surname",
                  "Client Initial",
                  "UFN",
                  "Fee code",
                  "Date work concluded",
                  "Escape case",
                  "Messages"
              ),
              "numeric", List.of("Calculated value")
          );

      case "mediation" ->
          Map.of(
              "text", List.of(
                  "Client 1 Surname",
                  "Client 1 Forename",
                  "Client 1 UCN",
                  "Client 2 Surname",
                  "Client 2 Forename",
                  "Client 2 UCN",
                  "Fee code",
                  "Messages"
              ),
              "numeric", List.of("Calculated value")
          );

      default ->
          Map.of(
              "text", List.of(),
              "numeric", List.of("Calculated value")
          );
    };
  }

  /**
   * Gets the submission area of law from the summary section.
   *
   * @return the area of law text
   */
  public String getAreaOfLaw() {
    Locator areaOfLawElement = page.locator("dt:has-text('Area of law') + dd");
    return areaOfLawElement.textContent().trim();
  }

  /**
   * Main validation method - validates sorting for current area of law across all pages.
   */
  public void validateSortingForCurrentAreaOfLaw() {
    waitForClaimsTable();
    goToFirstPage();

    String areaOfLaw = getAreaOfLaw();
    Map<String, List<String>> headers = getExpectedSortableHeaders(areaOfLaw);

    // Validate text headers
    List<String> textHeaders = headers.get("text");
    for (String header : textHeaders) {
      if (!hasSortableHeader(header)) {
        System.out.println("Skipping missing sortable header: " + header);
        continue;
      }

      // Test ascending
      goToFirstPage();
      sortByHeader(header, "ascending");

      if ("Escape case".equals(header)) {
        validateEscapeCaseSortingAcrossPages("ascending");
      } else if ("Messages".equals(header)) {
        validateMessagesSortingAcrossPages("ascending");
      } else {
        validateTextSortingAcrossPages(header, "ascending");
      }

      // Test descending
      goToFirstPage();
      sortByHeader(header, "descending");

      if ("Escape case".equals(header)) {
        validateEscapeCaseSortingAcrossPages("descending");
      } else if ("Messages".equals(header)) {
        validateMessagesSortingAcrossPages("descending");
      } else {
        validateTextSortingAcrossPages(header, "descending");
      }
    }

    // Validate numeric headers
    List<String> numericHeaders = headers.get("numeric");
    for (String header : numericHeaders) {
      if (!hasSortableHeader(header)) {
        System.out.println("Skipping missing sortable header: " + header);
        continue;
      }

      // Test ascending
      goToFirstPage();
      sortByHeader(header, "ascending");
      validateNumericSortingAcrossPages(header, "ascending");

      // Test descending
      goToFirstPage();
      sortByHeader(header, "descending");
      validateNumericSortingAcrossPages(header, "descending");
    }
  }

  // Private helper methods

  private int rankEscapeCase(String value) {
    String normalized = value.trim().toLowerCase();
    if ("no".equals(normalized)) {
      return 0;
    }
    if ("escaped".equals(normalized)) {
      return 1;
    }
    return 999;
  }

  private int rankMessages(String value) {
    String normalized = value.trim();
    return normalized.isEmpty() ? 1 : 0;
  }

  private boolean hrefHasDirection(String href, String directionSuffix) {
    return href.contains("," + directionSuffix) || href.contains("%2C" + directionSuffix);
  }
}

