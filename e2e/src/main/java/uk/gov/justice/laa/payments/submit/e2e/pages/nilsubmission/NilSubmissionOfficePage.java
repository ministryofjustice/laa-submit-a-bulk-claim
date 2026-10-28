package uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission;

import static com.microsoft.playwright.options.AriaRole.BUTTON;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;
import uk.gov.justice.laa.payments.submit.e2e.pages.BasePage;

@Getter
public class NilSubmissionOfficePage extends BasePage {

  private final Locator continueButton;
  private final Locator errorSummary;
  private final Locator errorMessage;

  public NilSubmissionOfficePage(Page page) {
    super(page, "Select the office account number");

    continueButton = page.getByRole(BUTTON, new Page.GetByRoleOptions().setName("Continue"));
    errorSummary = page.locator(".govuk-error-summary");
    errorMessage = page.locator(".govuk-error-message");
  }

  public void selectOffice(String officeCode) {
    page.getByLabel(officeCode).click();
  }
}
