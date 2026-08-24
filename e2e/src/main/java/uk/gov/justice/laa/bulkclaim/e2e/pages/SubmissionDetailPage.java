package uk.gov.justice.laa.bulkclaim.e2e.pages;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;

@Getter
public class SubmissionDetailPage extends BasePage {

  private final Locator successBanner;

  public SubmissionDetailPage(Page page) {
    super(page, "Submission summary");

    successBanner = page.locator(".govuk-notification-banner--success");
  }

  public void assertSubmissionAccepted() {
    assertThat(successBanner).isVisible();
    assertThat(successBanner).containsText("Your submission has been accepted.");
  }
}
