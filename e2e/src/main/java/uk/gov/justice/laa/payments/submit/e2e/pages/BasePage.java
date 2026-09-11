package uk.gov.justice.laa.payments.submit.e2e.pages;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import lombok.Getter;

@Getter
public abstract class BasePage {

  protected final Page page;
  protected final Locator heading;
  protected final Locator upload;

  public BasePage(Page page, String heading) {
    this.page = page;

    this.heading =
        page.getByRole(
            AriaRole.HEADING, new Page.GetByRoleOptions().setName(heading).setExact(true));

    this.upload = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Upload"));
    waitForPage();
  }

  private void waitForPage() {
    heading.waitFor();
    assertThat(heading).isVisible();
  }
}
