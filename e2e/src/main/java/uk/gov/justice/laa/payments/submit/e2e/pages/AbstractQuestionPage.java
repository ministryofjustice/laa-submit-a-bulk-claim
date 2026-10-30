package uk.gov.justice.laa.payments.submit.e2e.pages;

import static com.microsoft.playwright.options.AriaRole.BUTTON;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;

@Getter
public abstract class AbstractQuestionPage extends BasePage {

  private final Locator continueButton;
  private final Locator errorSummary;
  private final Locator errorMessage;

  protected AbstractQuestionPage(Page page, String heading) {
    super(page, heading);

    continueButton = page.getByRole(BUTTON, new Page.GetByRoleOptions().setName("Continue"));
    errorSummary = page.locator(".govuk-error-summary");
    errorMessage = page.locator(".govuk-error-message");
  }
}
