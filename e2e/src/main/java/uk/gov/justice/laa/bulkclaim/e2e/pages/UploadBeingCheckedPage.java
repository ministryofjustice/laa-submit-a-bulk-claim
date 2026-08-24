package uk.gov.justice.laa.bulkclaim.e2e.pages;

import static com.microsoft.playwright.options.AriaRole.BUTTON;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;

@Getter
public class UploadBeingCheckedPage extends BasePage {

  private final Locator goToSearchButton;

  public UploadBeingCheckedPage(Page page) {
    super(page, "Your file is being checked");

    goToSearchButton =
        page.getByRole(BUTTON, new Page.GetByRoleOptions().setName("Go to search"));
  }
}
