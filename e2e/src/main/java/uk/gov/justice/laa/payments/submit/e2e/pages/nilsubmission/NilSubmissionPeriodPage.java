package uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission;

import static com.microsoft.playwright.options.AriaRole.BUTTON;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;
import uk.gov.justice.laa.payments.submit.e2e.pages.BasePage;

@Getter
public class NilSubmissionPeriodPage extends BasePage {

  private final Locator periodSelect;
  private final Locator continueButton;
  private final Locator errorSummary;
  private final Locator errorMessage;

  private final Locator officeAccountNumberValue;
  private final Locator areaOfLawValue;

  public NilSubmissionPeriodPage(Page page) {
    super(page, "Select a submission period");

    periodSelect = page.locator("#submissionPeriod-input");
    continueButton = page.getByRole(BUTTON, new Page.GetByRoleOptions().setName("Continue"));
    errorSummary = page.locator(".govuk-error-summary");
    errorMessage = page.locator(".govuk-error-message");

    officeAccountNumberValue = page.getByTestId("nil-submission-office-value");
    areaOfLawValue = page.getByTestId("nil-submission-areaoflaw-value");
  }

  public void selectFirstAvailablePeriod() {
    String firstPeriodValue =
        periodSelect.locator("option:not([value=''])").first().getAttribute("value");
    periodSelect.selectOption(firstPeriodValue);
  }
}
