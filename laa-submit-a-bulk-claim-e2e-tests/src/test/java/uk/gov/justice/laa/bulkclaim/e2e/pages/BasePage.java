package uk.gov.justice.laa.bulkclaim.e2e.pages;

import com.microsoft.playwright.Page;

public abstract class BasePage {
  protected final Page page;

  protected BasePage(Page page) {
    this.page = page;
  }

  protected void clickButtonByName(String name) {
    page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON,
        new Page.GetByRoleOptions().setName(name)).first().click();
  }

  protected boolean containsText(String text) {
    return page.locator("body").innerText().contains(text);
  }
}

