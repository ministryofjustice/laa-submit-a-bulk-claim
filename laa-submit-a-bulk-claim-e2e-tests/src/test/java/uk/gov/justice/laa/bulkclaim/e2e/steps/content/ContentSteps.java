package uk.gov.justice.laa.bulkclaim.e2e.steps.content;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.nio.file.Path;
import java.util.regex.Pattern;
import uk.gov.justice.laa.bulkclaim.e2e.state.TestContext;
import uk.gov.justice.laa.bulkclaim.e2e.steps.BaseUiSteps;
import uk.gov.justice.laa.bulkclaim.e2e.utils.ContentFixtureUtil;

public class ContentSteps extends BaseUiSteps {

  @Then("the page content matches {string}")
  @Then("the search page content matches {string}")
  public void thePageContentMatches(String fixtureName) {
    Path fixture =
        ContentFixtureUtil.resolveFixture(findWorkspaceRoot(Path.of("").toAbsolutePath()), fixtureName);
    ContentFixtureUtil.assertMainContentMatchesFixture(page(), fixture);
  }

  @Then("I should see the bulk in progress page")
  public void iShouldSeeTheBulkInProgressPage() {
    bulkInProgressPage().waitForReady();
    if (!bulkInProgressPage().heading().equals("Your file is being uploaded")) {
      throw new AssertionError(
          "Unexpected bulk in progress heading: " + bulkInProgressPage().heading());
    }
  }

  @Then("the bulk upload details are displayed")
  public void theBulkUploadDetailsAreDisplayed() {
    bulkInProgressPage().waitForReady();
    String dateOfUpload = bulkInProgressPage().dateOfUpload();
    String submissionReference = bulkInProgressPage().submissionReference();
    String fileName = bulkInProgressPage().fileName();

    Pattern datePattern = Pattern.compile("^\\d{1,2}\\s+[A-Za-z]+\\s+\\d{4}\\s+at\\s+\\d{1,2}:\\d{2}\\s*(am|pm)$", Pattern.CASE_INSENSITIVE);
    if (!datePattern.matcher(dateOfUpload).matches()) {
      throw new AssertionError("Unexpected date of upload format: " + dateOfUpload);
    }

    Pattern uuidPattern = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", Pattern.CASE_INSENSITIVE);
    if (!uuidPattern.matcher(submissionReference).matches()) {
      throw new AssertionError("Unexpected submission reference format: " + submissionReference);
    }

    Path generated = TestContext.current().generatedFile();
    if (generated != null) {
      String expectedFileName = generated.getFileName().toString();
      if (!fileName.equals(expectedFileName)) {
        throw new AssertionError("Expected file name " + expectedFileName + " but got " + fileName);
      }
    }
  }

  @Then("the bulk in progress actions are visible")
  public void theBulkInProgressActionsAreVisible() {
    if (!bulkInProgressPage().goToSearchVisible() || !bulkInProgressPage().copyReferenceVisible()) {
      throw new AssertionError("Expected bulk in progress actions to be visible");
    }
  }

  @When("I click the go to search button")
  public void iClickTheGoToSearchButton() {
    bulkInProgressPage().clickGoToSearch();
  }

  @When("I click the copy reference button")
  public void iClickTheCopyReferenceButton() {
    bulkInProgressPage().clickCopyReference();
  }

  @Then("I click on page {string}")
  public void iClickOnPage(String pageNumber) {
    page().locator(".govuk-pagination__link", new com.microsoft.playwright.Page.LocatorOptions().setHasText(pageNumber)).first().click();
  }
}

