package uk.gov.justice.laa.payments.submit.e2e.pages;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;

@Getter
public class SubmissionDetailPage extends BasePage {

  private final Locator successBanner;
  private final Locator claimsTab;
  private final Locator messagesTab;
  private final Locator matterStartsTab;

  public SubmissionDetailPage(Page page) {
    super(page, "Submission summary");

    successBanner = page.locator(".govuk-notification-banner--success");
    claimsTab = page.locator("#claims-tab");
    messagesTab = page.locator("#messages-tab");
    matterStartsTab = page.locator("#matter-starts-tab");
  }

  public void assertSubmissionAccepted() {
    assertThat(successBanner).isVisible();
    assertThat(claimsTab).isVisible();
    assertThat(messagesTab).isVisible();
    assertThat(successBanner).containsText("Your submission has been accepted.");
  }

}
