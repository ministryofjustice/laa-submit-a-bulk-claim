package uk.gov.justice.laa.payments.submit.e2e.pages;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions.ContainsTextOptions;
import lombok.Getter;

@Getter
public class SubmissionDetailPage extends BasePage {

  private final Locator successBanner;
  private final Locator warningBanner;
  private final Locator claimsTab;
  private final Locator messagesTab;
  private final Locator matterStartsTab;

  private final Locator officeAccount;
  private final Locator areaOfLaw;
  private final Locator submissionPeriod;
  private final Locator submissionValue;

  private final Locator claimsTable;
  private final Locator messagesTable;
  private final Locator matterStartsList;

  public SubmissionDetailPage(Page page) {
    super(page, "Submission summary");

    successBanner = page.locator(".govuk-notification-banner--success");
    warningBanner = page.locator("#submission-summary-warning");

    claimsTab = page.locator("#claims-tab");
    messagesTab = page.locator("#messages-tab");
    matterStartsTab = page.locator("#matter-starts-tab");

    officeAccount = page.locator("#submission-summary__office dd");
    areaOfLaw = page.locator("#submission-summary__area-of-law dd");
    submissionPeriod = page.locator("#submission-summary__submission-period dd");
    submissionValue = page.locator("#submission-summary__submission-value dd");

    claimsTable = page.locator("#claims-table");
    messagesTable = page.locator("#messages-table");
    matterStartsList = page.locator("#matter-starts-list");
  }

  public void assertSubmissionAccepted() {
    assertThat(successBanner).isVisible();
    assertThat(claimsTab).isVisible();
    assertThat(messagesTab).isVisible();
    assertThat(successBanner).containsText("Your submission has been accepted");
  }

  public void assertTotalWarnings(int total) {
    assertThat(warningBanner).isVisible();
    var expectedText =
        total == 1 ? "1 claim has a warning message" : total + " claims have warning messages";
    assertThat(warningBanner).containsText(expectedText);
  }

  public void assertSubmissionSummary(String officeAccount, String areaOfLaw,
      String submissionPeriod, String submissionValue) {
    assertThat(this.officeAccount).containsText(officeAccount);
    assertThat(this.areaOfLaw).containsText(areaOfLaw);
    assertThat(this.submissionPeriod).containsText(submissionPeriod);
    assertThat(this.submissionValue).containsText(submissionValue);
  }

  public void assertTotalClaims(int total) {
    var claims = claimsTable.locator("tbody tr");
    assertThat(claimsTable).isVisible();
    assertThat(claims).hasCount(total);
  }

  public void assertTotalMessages(int total) {
    var messages = messagesTable.locator("tbody tr");
    assertThat(messagesTable).isVisible();
    assertThat(messages).hasCount(total);
  }

  public void assertTotalMatterStarts(int total) {
    var matterStarts = matterStartsList.locator("div");
    assertThat(matterStartsList).isVisible();
    assertThat(matterStarts).hasCount(total);
  }
}
