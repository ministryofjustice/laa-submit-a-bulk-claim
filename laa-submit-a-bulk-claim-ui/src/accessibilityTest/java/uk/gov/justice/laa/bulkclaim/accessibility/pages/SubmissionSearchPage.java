package uk.gov.justice.laa.bulkclaim.accessibility.pages;

import com.microsoft.playwright.Page;

/** Page object for submission search and results views. */
public final class SubmissionSearchPage {

  private static final String SEARCH_RESULTS_QUERY =
      "/submissions/search/results?page=0&offices=0P322F&areaOfLaw=LEGAL_HELP&submissionStatuses=ALL";

  private final Page page;

  public SubmissionSearchPage(Page page) {
    this.page = page;
  }

  public void open(String appBaseUrl) {
    page.navigate(appBaseUrl + "/submissions/search");
  }

  public void openResults(String appBaseUrl) {
    page.navigate(appBaseUrl + SEARCH_RESULTS_QUERY);
  }
}
