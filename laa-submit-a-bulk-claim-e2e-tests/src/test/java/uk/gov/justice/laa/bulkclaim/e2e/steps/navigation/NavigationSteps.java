package uk.gov.justice.laa.bulkclaim.e2e.steps.navigation;

import io.cucumber.java.en.Given;
import uk.gov.justice.laa.bulkclaim.e2e.steps.BaseUiSteps;
import uk.gov.justice.laa.bulkclaim.e2e.utils.AuthenticationHelper;

public class NavigationSteps extends BaseUiSteps {

  @Given("I start from a clean logged-in state")
  public void iStartFromCleanLoggedInState() {
    AuthenticationHelper.logoutAndWipe(page());
    AuthenticationHelper.navigateWithAuthRetry(page(), baseUrl());
  }

  @Given("I am on the bulk import page")
  public void iAmOnTheBulkImportPage() {
    bulkImportPage().open();
  }

  @Given("I am on the bulk submission landing page")
  public void iAmOnTheBulkSubmissionLandingPage() {
    page().navigate(baseUrl());
  }

  @Given("I am on the Search page")
  public void iAmOnTheSearchPage() {
    searchPage().open(baseUrl());
  }
}
