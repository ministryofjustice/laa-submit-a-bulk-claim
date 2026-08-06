package uk.gov.justice.laa.bulkclaim.e2e.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class SubmissionErrorsPage extends BasePage{
    private final Locator failureBanner;

    public SubmissionErrorsPage(Page page) {
        super(page, "Submission summary");

        failureBanner = page.locator(".moj-alert--error");
    }

    public void assertSubmissionErrors() {
        assertThat(failureBanner).isVisible();
        assertThat(failureBanner).containsText("Resolve the errors and upload the file again.");
    }
}
