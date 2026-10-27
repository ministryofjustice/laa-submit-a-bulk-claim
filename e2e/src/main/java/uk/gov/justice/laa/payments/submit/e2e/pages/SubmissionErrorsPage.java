package uk.gov.justice.laa.payments.submit.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;

@Getter
public class SubmissionErrorsPage extends BasePage {

  private final Locator failureBanner;
  private final Locator failureBannerHeading;
  private final Locator statusTag;
  private final Locator claimErrorsHeading;
  private final Locator claimErrorRows;
  private final Locator submissionErrorsHeading;
  private final Locator submissionErrorRows;

  public SubmissionErrorsPage(Page page) {
    super(page, "Submission summary");

    failureBanner = page.locator(".moj-alert--error");
    failureBannerHeading = failureBanner.locator(".moj-alert__heading");
    statusTag = page.locator(".govuk-tag--red");
    claimErrorsHeading = page.locator("#claim-messages-errors-heading");
    claimErrorRows = page.locator("[id^=claim-message-error-]");
    submissionErrorsHeading = page.locator("#messages-errors-heading");
    submissionErrorRows = page.locator("[id^=message-error-]");
  }
}
