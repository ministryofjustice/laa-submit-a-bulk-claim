package uk.gov.justice.laa.bulkclaim.e2e.pages;

import static com.microsoft.playwright.options.AriaRole.BUTTON;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;

@Getter
public class LandingPagePage extends BasePage {

  private final Locator startNowButton;

  public LandingPagePage(Page page) {
    super(page, "Submit a bulk claim");

    startNowButton = page.getByRole(BUTTON, new Page.GetByRoleOptions().setName("Start now"));
  }
}
