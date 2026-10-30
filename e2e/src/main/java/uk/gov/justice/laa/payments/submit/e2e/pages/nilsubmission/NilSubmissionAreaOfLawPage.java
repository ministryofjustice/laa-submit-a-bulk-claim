package uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;
import uk.gov.justice.laa.payments.submit.e2e.pages.AbstractQuestionPage;

@Getter
public class NilSubmissionAreaOfLawPage extends AbstractQuestionPage {

  private final Locator legalHelpRadio;
  private final Locator crimeLowerRadio;
  private final Locator mediationRadio;
  private final Locator officeAccountNumberValue;

  public NilSubmissionAreaOfLawPage(Page page) {
    super(page, "Select the area of law");

    crimeLowerRadio = page.getByLabel("Crime lower");
    mediationRadio = page.getByLabel("Mediation");
    legalHelpRadio = page.getByLabel("Legal help");
    officeAccountNumberValue = page.getByTestId("nil-submission-office-value");
  }
}
