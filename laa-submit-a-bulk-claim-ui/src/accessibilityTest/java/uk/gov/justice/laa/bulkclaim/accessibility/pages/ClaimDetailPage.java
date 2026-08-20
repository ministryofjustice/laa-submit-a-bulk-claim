package uk.gov.justice.laa.bulkclaim.accessibility.pages;

import com.microsoft.playwright.Page;

public final class ClaimDetailPage {

  private final Page page;

  public ClaimDetailPage(Page page) {
    this.page = page;
  }

  public void open(String appBaseUrl, String submissionId, String claimId) {
    page.navigate(appBaseUrl + "/submissions/%s/claims/%s".formatted(submissionId, claimId));
  }
}
