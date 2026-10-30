package uk.gov.justice.laa.payments.submit.e2e.pages.nilsubmission;

import com.microsoft.playwright.Page;
import uk.gov.justice.laa.payments.submit.e2e.pages.AbstractQuestionPage;

public class NilSubmissionOfficePage extends AbstractQuestionPage {

  public NilSubmissionOfficePage(Page page) {
    super(page, "Select the office account number");
  }

  public void selectOffice(String officeCode) {
    page.getByLabel(officeCode).click();
  }
}
