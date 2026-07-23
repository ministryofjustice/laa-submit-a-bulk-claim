package uk.gov.justice.laa.bulkclaim.e2e.pages.nilsubmission;

import static com.microsoft.playwright.options.AriaRole.BUTTON;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;
import uk.gov.justice.laa.bulkclaim.e2e.pages.BasePage;

@Getter
public class NilSubmissionReferencePage extends BasePage {

  private final Locator referenceInput;
  private final Locator continueButton;

  public NilSubmissionReferencePage(Page page) {
    super(page, "Add your submission reference");

    referenceInput = page.locator("#schedule-reference");
    continueButton = page.getByRole(BUTTON, new Page.GetByRoleOptions().setName("Continue"));
  }
}
