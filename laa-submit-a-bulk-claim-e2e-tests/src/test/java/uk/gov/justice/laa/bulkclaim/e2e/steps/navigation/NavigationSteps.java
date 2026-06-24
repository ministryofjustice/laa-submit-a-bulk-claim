package uk.gov.justice.laa.bulkclaim.e2e.steps.navigation;

import io.cucumber.java.en.Given;
import uk.gov.justice.laa.bulkclaim.e2e.steps.BaseUiSteps;
import uk.gov.justice.laa.bulkclaim.e2e.utils.AuthenticationHelper;

public class NavigationSteps extends BaseUiSteps {

  @Given("I start from a clean logged-in state")
  public void iStartFromCleanLoggedInState() {
    AuthenticationHelper.logoutAndWipe(page());
    AuthenticationHelper.navigateWithAuthRetry(page(), baseUrl());
    page().locator("a.govuk-button--start").click();
  }
}