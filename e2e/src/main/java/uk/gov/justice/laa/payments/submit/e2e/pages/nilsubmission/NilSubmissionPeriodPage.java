package uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;
import uk.gov.justice.laa.payments.submit.e2e.pages.AbstractQuestionPage;

@Getter
public class NilSubmissionPeriodPage extends AbstractQuestionPage {

  private final Locator periodSelect;
  private final Locator officeAccountNumberValue;
  private final Locator areaOfLawValue;

  public NilSubmissionPeriodPage(Page page) {
    super(page, "Select a submission period");

    periodSelect = page.locator("#submissionPeriod-input");
    officeAccountNumberValue = page.getByTestId("nil-submission-office-value");
    areaOfLawValue = page.getByTestId("nil-submission-areaoflaw-value");
  }

  public void selectFirstAvailablePeriod() {
    String firstPeriodValue =
        periodSelect.locator("option:not([value=''])").first().getAttribute("value");
    periodSelect.selectOption(firstPeriodValue);
  }
}
