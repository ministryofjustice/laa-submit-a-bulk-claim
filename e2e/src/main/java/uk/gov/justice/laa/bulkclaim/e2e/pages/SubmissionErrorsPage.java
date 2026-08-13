package uk.gov.justice.laa.bulkclaim.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;

@Getter
public class SubmissionErrorsPage extends BasePage {

  private final Locator failureBanner;

  public SubmissionErrorsPage(Page page) {
    super(page, "Submission summary");

    failureBanner = page.locator(".moj-alert--error");
  }
}
