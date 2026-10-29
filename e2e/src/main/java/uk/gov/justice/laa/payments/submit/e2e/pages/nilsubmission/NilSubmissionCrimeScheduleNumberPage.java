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
  private final Locator errorSummary;
  private final Locator errorMessage;
  private final Locator officeAccountNumberValue;
  private final Locator areaOfLawValue;
  private final Locator submissionPeriodValue;

  public NilSubmissionCrimeScheduleNumberPage(Page page) {
    super(page, "Add your crime schedule number");

    referenceInput = page.locator("#submissionReference-input");
    continueButton = page.getByRole(BUTTON, new Page.GetByRoleOptions().setName("Continue"));

    errorSummary = page.locator(".govuk-error-summary");
    errorMessage = page.locator(".govuk-error-message");
    officeAccountNumberValue = page.getByTestId("nil-submission-office-value");
    areaOfLawValue = page.getByTestId("nil-submission-areaoflaw-value");
    submissionPeriodValue = page.getByTestId("nil-submission-period-value");
  }
}
