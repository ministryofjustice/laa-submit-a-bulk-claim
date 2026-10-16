package uk.gov.justice.laa.payments.submit.accessibility.pages;

import com.microsoft.playwright.Page;

public final class NilSubmissionPage {

  private final Page page;
  private final String appBaseUrl;

  public NilSubmissionPage(Page page, String appBaseUrl) {
    this.page = page;
    this.appBaseUrl = appBaseUrl;
  }

  public void openOffice() {
    page.navigate(appBaseUrl + "/nil-submission/office");
  }

  public void selectOffice(String office) {
    page.locator("input[name='office'][value='%s']".formatted(office)).check();
    continueToNextPage();
  }

  public void selectAreaOfLaw(String areaOfLaw) {
    page.locator("input[name='areaOfLaw'][value='%s']".formatted(areaOfLaw)).check();
    continueToNextPage();
  }

  public void selectSubmissionPeriod() {
    page.locator("#submissionPeriod-input").selectOption(new String[] {firstPeriodValue()});
    continueToNextPage();
  }

  public void enterReference(String reference) {
    page.locator("#submissionReference-input").fill(reference);
    continueToNextPage();
  }

  private String firstPeriodValue() {
    return page.locator("#submissionPeriod-input option:not([value=''])")
        .first()
        .getAttribute("value");
  }

  private void continueToNextPage() {
    page.locator("button[type='submit']").click();
  }
}
