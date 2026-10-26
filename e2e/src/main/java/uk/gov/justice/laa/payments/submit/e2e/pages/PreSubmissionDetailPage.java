package uk.gov.justice.laa.payments.submit.e2e.pages;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;

@Getter
public class PreSubmissionDetailPage extends BasePage {

  private final Locator informationBanner;
  private final Locator claimsTab;
  private final Locator messagesTab;
  private final Locator submitButton;
  private final Locator deleteButton;

  public PreSubmissionDetailPage(Page page) {
    super(page, "Submission summary");

    informationBanner = page.locator(".moj-alert--information");
    claimsTab = page.locator("#claims-tab");
    messagesTab = page.locator("#messages-tab");
    submitButton = page.locator("#submit-submission-button");
    deleteButton = page.locator("#delete-submission-button");
  }

  public void assertSubmissionNotSubmitted() {
    assertThat(informationBanner).isVisible();
    assertThat(claimsTab).isVisible();
    assertThat(messagesTab).isVisible();
    assertThat(informationBanner).containsText("Your bulk claim has been uploaded.");
    assertThat(submitButton).isVisible();
    assertThat(deleteButton).isVisible();
  }
}
