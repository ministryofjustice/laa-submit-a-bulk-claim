package uk.gov.justice.laa.bulkclaim.ui.pages;

import com.microsoft.playwright.Page;

/** Shared base type for UI test page objects. */
public abstract class BasePage {

  protected final Page page;

  protected BasePage(Page page) {
    this.page = page;
  }
}

