package uk.gov.justice.laa.bulkclaim.accessibility.pages;

import com.microsoft.playwright.Page;

/** Page object for claim detail navigation. */
public final class ClaimDetailPage {

  private final Page page;

  public ClaimDetailPage(Page page) {
    this.page = page;
  }

  public void open(String appBaseUrl, String submissionId, String claimId) {
    page.navigate(appBaseUrl + "/view-submission-detail?submissionId=" + submissionId);
    page.navigate(appBaseUrl + "/submission/claim/" + claimId);
  }
}
