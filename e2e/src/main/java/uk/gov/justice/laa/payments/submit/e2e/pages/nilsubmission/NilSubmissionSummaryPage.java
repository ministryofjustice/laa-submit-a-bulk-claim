package uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission;

import static com.microsoft.playwright.options.AriaRole.BUTTON;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;
import uk.gov.justice.laa.payments.submit.e2e.pages.BasePage;

@Getter
public class NilSubmissionSummaryPage extends BasePage {

  private final Locator submitButton;

  private final Locator changeOfficeLink;
  private final Locator changeAreaOfLawLink;
  private final Locator changeSubmissionPeriodLink;
  private final Locator changeSubmissionReferenceLink;

  public NilSubmissionSummaryPage(Page page) {
    super(page, "Check your nil submission");

    submitButton = page.getByRole(BUTTON, new Page.GetByRoleOptions().setName("Submit"));
    changeOfficeLink = page.getByTestId("nil-submission-office");
    changeAreaOfLawLink = page.getByTestId("nil-submission-areaoflaw");
    changeSubmissionPeriodLink = page.getByTestId("nil-submission-period");
    changeSubmissionReferenceLink = page.getByTestId("nil-submission-reference");
  }
}
