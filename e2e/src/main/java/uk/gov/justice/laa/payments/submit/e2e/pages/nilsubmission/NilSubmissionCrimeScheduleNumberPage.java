package uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission;

import static com.microsoft.playwright.options.AriaRole.BUTTON;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;
import uk.gov.justice.laa.payments.submit.e2e.pages.BasePage;

@Getter
public class NilSubmissionCrimeScheduleNumberPage extends BasePage {

  private final Locator referenceInput;
  private final Locator continueButton;

  public NilSubmissionCrimeScheduleNumberPage(Page page) {
    super(page, "Add your crime schedule number");

    referenceInput = page.locator("#submissionReference-input");
    continueButton = page.getByRole(BUTTON, new Page.GetByRoleOptions().setName("Continue"));
  }
}
