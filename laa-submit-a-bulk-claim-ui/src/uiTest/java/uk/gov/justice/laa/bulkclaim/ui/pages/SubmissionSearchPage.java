package uk.gov.justice.laa.bulkclaim.ui.pages;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import java.util.List;

/** Page object for submission search and results views. */
public final class SubmissionSearchPage extends BasePage {

  private static final String SEARCH_FORM_PATH = "/submissions/search";
  private static final String SEARCH_BUTTON = "#searchButton";
  private static final String SUBMISSION_PERIOD = "#submission-period";
  private static final String SUBMISSION_PERIOD_SELECT = "select[name='submissionPeriod']";
  private static final String AREA_OF_LAW = "#area-of-law";
  private static final String SUCCEEDED_RADIO = "#succeeded-radio-option";
  private static final String FAILED_RADIO = "#failed-radio-option";
  private static final String ALL_RADIO = "#all-radio-option";
  private static final String OFFICES = "#offices-input-container input[type='checkbox']";
  private static final String OFFICES_DETAILS_SUMMARY = "#choose-office-account-details-summary";
  private static final String RESULTS_HEADING = "#results-heading";
  private static final String RESULTS_ROWS = "tbody.govuk-table__body tr.govuk-table__row";
  private static final String NO_RESULTS_MESSAGE = "p.govuk-body:has-text('No submissions were found.')";

  public SubmissionSearchPage(Page page) {
    super(page);
  }

  public void open(String appBaseUrl) {
    page.navigate(appBaseUrl + SEARCH_FORM_PATH);
    page.waitForLoadState(LoadState.DOMCONTENTLOADED);
  }

  public void selectSubmissionPeriod(String submissionPeriod) {
    String tagName =
        (String)
            page.locator(SUBMISSION_PERIOD).first().evaluate("element => element.tagName.toLowerCase()");

    if ("select".equals(tagName)) {
      page.selectOption(SUBMISSION_PERIOD, submissionPeriod);
      return;
    }

    String submissionPeriodLabel =
        (String)
            page.evaluate(
                """
                ([selectLocator, periodValue]) => {
                  const select = document.querySelector(selectLocator);
                  if (!select) {
                    return periodValue;
                  }
                  const matchingOption = Array.from(select.options)
                    .find(option => option.value === periodValue);
                  return matchingOption ? matchingOption.textContent.trim() : periodValue;
                }
                """,
                List.of(SUBMISSION_PERIOD_SELECT, submissionPeriod));

    page.fill(SUBMISSION_PERIOD, submissionPeriodLabel);

    Locator matchingAutocompleteOption =
        page.locator("#submission-period__listbox li:has-text('" + submissionPeriodLabel + "')").first();
    if (matchingAutocompleteOption.count() > 0 && matchingAutocompleteOption.isVisible()) {
      matchingAutocompleteOption.click();
      return;
    }

    page.press(SUBMISSION_PERIOD, "Enter");
  }

  public void selectAreaOfLaw(String areaOfLaw) {
    page.selectOption(AREA_OF_LAW, areaOfLaw);
  }

  public void selectOutcomeSucceeded() {
    page.check(SUCCEEDED_RADIO);
  }

  public void selectOutcomeFailed() {
    page.check(FAILED_RADIO);
  }

  public void selectOutcomeAll() {
    page.check(ALL_RADIO);
  }

  public void deselectAllOffices() {
    ensureOfficeDetailsOpen();
    Locator checkboxes = page.locator(OFFICES);
    for (int i = 0; i < checkboxes.count(); i++) {
      Locator checkbox = checkboxes.nth(i);
      if (checkbox.isChecked()) {
        checkbox.uncheck();
      }
    }
  }

  public void selectOffice(String officeCode) {
    ensureOfficeDetailsOpen();
    page.check("#offices-input-container input[type='checkbox'][value='" + officeCode + "']");
  }

  public void clickSearch() {
    page.click(SEARCH_BUTTON);
    page.waitForLoadState(LoadState.DOMCONTENTLOADED);
  }

  public void assertResultsVisible() {
    assertTrue(page.locator(RESULTS_HEADING).isVisible(), "Expected results heading to be visible");
    assertTrue(getResultRowCount() > 0, "Expected at least one search result row");
  }

  public int getResultRowCount() {
    return page.locator(RESULTS_ROWS).count();
  }

  public void assertNoResultsMessage() {
    assertTrue(page.locator(NO_RESULTS_MESSAGE).isVisible(), "Expected no-results message to be visible");
  }

  public void assertValidationMessage(String message) {
    assertTrue(
        page.locator("#error-summary").isVisible(),
        "Expected validation error summary to be visible");
    assertTrue(
        page.locator("#error-summary").innerText().contains(message),
        "Expected validation message to contain: " + message);
  }

  private void ensureOfficeDetailsOpen() {
    if (!page.locator("#offices-input-container").isVisible()) {
      page.click(OFFICES_DETAILS_SUMMARY);
    }
  }
}

