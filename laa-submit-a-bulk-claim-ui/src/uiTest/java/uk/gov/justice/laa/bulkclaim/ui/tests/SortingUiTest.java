package uk.gov.justice.laa.bulkclaim.ui.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import uk.gov.justice.laa.bulkclaim.ui.helpers.AbstractUiTest;
import uk.gov.justice.laa.bulkclaim.ui.helpers.UiWiremockSupport;
import uk.gov.justice.laa.bulkclaim.ui.pages.ClaimsTablePage;

/**
 * Feature: Sort submission via UI
 *
 * <p>Tests sorting functionality on claim summary tables using mocked API responses. Three area
 * scenarios are covered (Legal help, Mediation, Crime lower) using WireMock stubs registered in
 * AbstractAccessibilityTest + UiWiremockSupport.
 */
@DisplayName("Feature: Sort submission via UI")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SortingUiTest extends AbstractUiTest {

  private ClaimsTablePage claimsTablePage;
  private static final int EXPECTED_ROWS_ACROSS_THREE_PAGES = 30;

  private static final String CLIENT_SURNAME_HEADER = "Client surname";
  private static final String CLIENT_1_SURNAME_HEADER = "Client 1 Surname";
  private static final String CALCULATED_VALUE_HEADER = "Calculated value";

  @BeforeAll
  void registerSortingStubs() {
    UiWiremockSupport.registerSortingStubs(WIREMOCK, LEGAL_HELP);
    UiWiremockSupport.registerSortingStubs(WIREMOCK, MEDIATION);
    UiWiremockSupport.registerSortingStubs(WIREMOCK, CRIME_LOWER);
  }

  @BeforeEach
  void setUp() {
    claimsTablePage = new ClaimsTablePage(page);
    navigateToSubmission(LEGAL_HELP.validSubmissionId());
  }

  @Test
  @DisplayName("Sort links are available on claims table")
  void shouldExposeCoreSortableHeaders() {
    assertTrue(claimsTablePage.hasSortableHeader(CLIENT_SURNAME_HEADER));
    assertTrue(claimsTablePage.hasSortableHeader("Calculated value"));
  }

  @Test
  @DisplayName("Global sort moves records from later pages to page 1")
  void shouldMoveLaterPageRecordToFirstPageWhenSortingClientSurnameAscending() {
    String firstBeforeSort = claimsTablePage.getFirstRowTextValue(CLIENT_SURNAME_HEADER);
    assertNotEquals("Adams", firstBeforeSort, "Precondition failed: Adams already first");

    claimsTablePage.sortByHeader(CLIENT_SURNAME_HEADER, "ascending");

    assertTrue(page.url().contains("page=0"));
    assertTrue(page.url().contains("sort=client_surname") || page.url().contains("sort=client_surname%2C"));
    assertEquals("Adams", claimsTablePage.getFirstRowTextValue(CLIENT_SURNAME_HEADER));

    assertTextColumnAscendingAcrossPages(CLIENT_SURNAME_HEADER);
  }

  @Test
  @DisplayName("Global numeric sort shows max calculated value on first row")
  void shouldShowMaxCalculatedValueOnFirstRowWhenSortingDescending() {
    claimsTablePage.sortByHeader(CALCULATED_VALUE_HEADER, "descending");
    assertTrue(page.url().contains("page=0"));
    assertEquals(2000.00, claimsTablePage.getFirstRowNumericValue(CALCULATED_VALUE_HEADER), 0.001);

    assertCalculatedValueDescendingAcrossPages();
  }

  @Test
  @DisplayName("Global sort works across pages for Mediation")
  void shouldApplyGlobalSortingForMediation() {
    navigateToSubmission(MEDIATION.validSubmissionId());

    claimsTablePage.sortByHeader(CLIENT_1_SURNAME_HEADER, "ascending");
    assertTrue(page.url().contains("page=0"));

    assertTextColumnAscendingAcrossPages(CLIENT_1_SURNAME_HEADER);

    claimsTablePage.sortByHeader(CALCULATED_VALUE_HEADER, "descending");
    assertCalculatedValueDescendingAcrossPages();
  }

  @Test
  @DisplayName("Global sort works across pages for Crime lower")
  void shouldApplyGlobalSortingForCrimeLower() {
    navigateToSubmission(CRIME_LOWER.validSubmissionId());

    claimsTablePage.sortByHeader(CLIENT_SURNAME_HEADER, "ascending");
    assertTrue(page.url().contains("page=0"));

    assertTextColumnAscendingAcrossPages(CLIENT_SURNAME_HEADER);

    claimsTablePage.sortByHeader(CALCULATED_VALUE_HEADER, "descending");
    assertCalculatedValueDescendingAcrossPages();
  }

  private void assertTextColumnAscendingAcrossPages(String headerText) {
    var values = claimsTablePage.getAllColumnTextValues(headerText);
    assertEquals(EXPECTED_ROWS_ACROSS_THREE_PAGES, values.size());
    for (int i = 1; i < values.size(); i++) {
      assertTrue(
          values.get(i - 1).compareToIgnoreCase(values.get(i)) <= 0,
          "Expected ascending order at index " + i + ": " + values.get(i - 1) + " <= " + values.get(i));
    }
  }

  private void assertCalculatedValueDescendingAcrossPages() {
    var values = claimsTablePage.getAllColumnNumericValues(CALCULATED_VALUE_HEADER);
    assertEquals(EXPECTED_ROWS_ACROSS_THREE_PAGES, values.size());
    for (int i = 1; i < values.size(); i++) {
      assertTrue(
          values.get(i - 1) >= values.get(i),
          "Expected descending order at index " + i + ": " + values.get(i - 1) + " >= " + values.get(i));
    }
  }

  private void navigateToSubmission(String submissionId) {
    page.navigate(
        appUrl(
            "/view-submission-detail?submissionId="
                + submissionId
                + "&navTab=CLAIM_DETAILS&page=0"));
    claimsTablePage.waitForClaimsTable();
  }
}
