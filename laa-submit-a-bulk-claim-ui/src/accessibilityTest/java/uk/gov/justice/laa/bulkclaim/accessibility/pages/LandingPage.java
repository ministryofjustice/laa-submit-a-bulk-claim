package uk.gov.justice.laa.bulkclaim.accessibility.pages;

import com.microsoft.playwright.Page;

/** Page object for the service landing page. */
public final class LandingPage {

  private final Page page;

  public LandingPage(Page page) {
    this.page = page;
  }

  public void open(String appBaseUrl) {
    page.navigate(appBaseUrl + "/");
  }
}
