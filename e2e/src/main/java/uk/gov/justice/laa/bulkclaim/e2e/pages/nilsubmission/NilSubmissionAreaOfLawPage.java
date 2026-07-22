package uk.gov.justice.laa.bulkclaim.e2e.pages.nilsubmission;

import static com.microsoft.playwright.options.AriaRole.BUTTON;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;
import uk.gov.justice.laa.bulkclaim.e2e.pages.BasePage;

@Getter
public class NilSubmissionAreaOfLawPage extends BasePage {

  private final Locator crimeLowerRadio;
  private final Locator continueButton;

  public NilSubmissionAreaOfLawPage(Page page) {
    super(page, "Select the area of law");

    crimeLowerRadio = page.getByLabel("Crime lower");
    continueButton = page.getByRole(BUTTON, new Page.GetByRoleOptions().setName("Continue"));
  }
}
