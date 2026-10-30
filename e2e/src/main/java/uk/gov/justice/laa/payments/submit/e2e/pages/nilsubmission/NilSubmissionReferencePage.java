package uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.Getter;
import uk.gov.justice.laa.payments.submit.e2e.pages.AbstractQuestionPage;

/** Reference page shared by the crime, civil and mediation journeys, which differ by heading. */
@Getter
public abstract class NilSubmissionReferencePage extends AbstractQuestionPage {

  private final Locator referenceInput;
  private final Locator officeAccountNumberValue;
  private final Locator areaOfLawValue;
  private final Locator submissionPeriodValue;

  protected NilSubmissionReferencePage(Page page, String heading) {
    super(page, heading);

    referenceInput = page.locator("#submissionReference-input");
    officeAccountNumberValue = page.getByTestId("nil-submission-office-value");
    areaOfLawValue = page.getByTestId("nil-submission-areaoflaw-value");
    submissionPeriodValue = page.getByTestId("nil-submission-period-value");
  }
}
