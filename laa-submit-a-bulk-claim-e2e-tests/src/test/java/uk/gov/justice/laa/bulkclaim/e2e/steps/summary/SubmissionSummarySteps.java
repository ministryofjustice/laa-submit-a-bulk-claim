package uk.gov.justice.laa.bulkclaim.e2e.steps.summary;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import uk.gov.justice.laa.bulkclaim.e2e.steps.BaseUiSteps;

public class SubmissionSummarySteps extends BaseUiSteps {

  @When("I open the first claim in the submission")
  @When("I view the first claim")
  public void iOpenTheFirstClaimInTheSubmission() {
    summaryPage().openFirstClaim();
    claimDetailPage().waitForReady();
  }

  @Then("There should be {int} warnings")
  public void thereShouldBeWarnings(int warningCount) {
    summaryPage().verifyWarningBanner(warningCount);
  }

  @Then("I should see the submission summary for {string} with {string} claims")
  public void iShouldSeeTheSubmissionSummaryForWithClaims(String areaOfLaw, String claims) {
    int expectedClaims = Integer.parseInt(claims);
    summaryPage().waitForReady();
    if (!summaryPage().containsAreaOfLaw(areaOfLaw)) {
      throw new AssertionError("Expected summary to include area of law: " + areaOfLaw);
    }
    if (summaryPage().claimsCount() != expectedClaims) {
      throw new AssertionError(
          "Expected " + expectedClaims + " claim rows, got " + summaryPage().claimsCount());
    }
  }

  @Then("I should see the submission summary for {string}")
  public void iShouldSeeTheSubmissionSummaryFor(String areaOfLaw) {
    summaryPage().waitForReady();
    if (!summaryPage().containsAreaOfLaw(areaOfLaw)) {
      throw new AssertionError("Expected summary to include area of law: " + areaOfLaw);
    }
  }

  @Then("I should not be able to export the submission")
  public void iShouldNotBeAbleToExportTheSubmission() {
    if (summaryPage().exportVisible()) {
      throw new AssertionError("Expected export button to be hidden");
    }
  }

  @Then("I click the {string} tab")
  public void iClickTheTab(String tabName) {
    if ("Messages".equalsIgnoreCase(tabName)) {
      page().locator(".moj-sub-navigation__link", new com.microsoft.playwright.Page.LocatorOptions().setHasText("Messages")).first().click();
      return;
    }
    if ("Matter starts".equalsIgnoreCase(tabName)) {
      page().locator(".moj-sub-navigation__link", new com.microsoft.playwright.Page.LocatorOptions().setHasText("Matter starts")).first().click();
      return;
    }
    throw new AssertionError("Unsupported tab name: " + tabName);
  }

}
