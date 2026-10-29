package uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission;

import static com.microsoft.playwright.options.AriaRole.BUTTON;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;
import uk.gov.justice.laa.payments.submit.e2e.pages.BasePage;

@Getter
public class NilSubmissionAreaOfLawPage extends BasePage {

  private final Locator legalHelpRadio;
  private final Locator crimeLowerRadio;
  private final Locator mediationRadio;
  private final Locator continueButton;
  private final Locator errorSummary;
  private final Locator errorMessage;

  private final Locator officeAccountNumberValue;

  public NilSubmissionAreaOfLawPage(Page page) {
    super(page, "Select the area of law");

    crimeLowerRadio = page.getByLabel("Crime lower");
    mediationRadio = page.getByLabel("Mediation");
    legalHelpRadio = page.getByLabel("Legal help");
    continueButton = page.getByRole(BUTTON, new Page.GetByRoleOptions().setName("Continue"));
    errorSummary = page.locator(".govuk-error-summary");
    errorMessage = page.locator(".govuk-error-message");
    officeAccountNumberValue = page.getByTestId("nil-submission-office-value");
  }
}
