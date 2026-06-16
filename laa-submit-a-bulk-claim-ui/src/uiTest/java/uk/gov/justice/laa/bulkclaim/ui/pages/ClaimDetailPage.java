package uk.gov.justice.laa.bulkclaim.ui.pages;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import java.util.List;

/** Page object for claim detail navigation. */
public final class ClaimDetailPage extends BasePage {

  public ClaimDetailPage(Page page) {
    super(page);
  }

  public void open(String appBaseUrl, String submissionId, String claimId) {
    page.navigate(appBaseUrl + "/view-submission-detail?submissionId=" + submissionId);
    page.navigate(appBaseUrl + "/submission/claim/" + claimId);
    page.waitForLoadState(LoadState.DOMCONTENTLOADED);
  }

  public void waitForClaimDetail() {
    page.locator(".govuk-table__caption:has-text('Fee calculation')").waitFor();
  }

  public void assertFeeCalculationHeadings(List<String> expectedHeadings) {
    String headingsText = String.join("\n", feeCalculationHeadings()).toLowerCase();
    for (String expectedHeading : expectedHeadings) {
      assertTrue(
          headingsText.contains(expectedHeading.toLowerCase()),
          "Expected fee heading to be present: " + expectedHeading);
    }
  }

  public List<String> feeCalculationHeadings() {
    Locator headingCells = page.locator("table.govuk-table tbody tr th.govuk-table__header");
    return headingCells.allInnerTexts();
  }
}

