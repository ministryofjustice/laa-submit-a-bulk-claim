package uk.gov.justice.laa.bulkclaim.e2e.pages.nilsubmission;

import static com.microsoft.playwright.options.AriaRole.BUTTON;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;
import uk.gov.justice.laa.bulkclaim.e2e.pages.BasePage;

@Getter
public class NilSubmissionPeriodPage extends BasePage {

  private final Locator periodSelect;
  private final Locator continueButton;

  public NilSubmissionPeriodPage(Page page) {
    super(page, "Select a submission period");

    periodSelect = page.locator("#submission-period");
    continueButton = page.getByRole(BUTTON, new Page.GetByRoleOptions().setName("Continue"));
  }

  public void selectFirstAvailablePeriod() {
    String firstPeriodValue =
        periodSelect.locator("option:not([value=''])").first().getAttribute("value");
    periodSelect.selectOption(firstPeriodValue);
  }
}
