package uk.gov.justice.laa.bulkclaim.e2e.steps.submission;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.nio.file.Path;
import uk.gov.justice.laa.bulkclaim.e2e.state.TestContext;
import uk.gov.justice.laa.bulkclaim.e2e.steps.BaseUiSteps;
import uk.gov.justice.laa.bulkclaim.e2e.utils.db.DatabaseManager;

/**
 * Covers submission-level UI steps: export, error assertions, claims table, matter starts, void,
 * submission total.
 */
public class SubmissionSteps extends BaseUiSteps {

  private static final HttpClient HTTP = HttpClient.newHttpClient();

  // ── Export ────────────────────────────────────────────────────────────────

  @Then("I should be able to export the submission")
  @Then("I should be able to export the {string} submission")
  public void iShouldBeAbleToExportTheSubmission(String areaOfLaw) {
    doExport(areaOfLaw);
  }

  @Then("I should be able to export the submission for {string}")
  public void iShouldBeAbleToExportTheSubmissionFor(@SuppressWarnings("unused") String areaOfLaw) {
    doExport(areaOfLaw);
  }

  private void doExport(String areaOfLaw) {
    if (!summaryPage().exportVisible()) {
      throw new AssertionError("Expected export button to be visible");
    }
    com.microsoft.playwright.Download download = summaryPage().exportDownload();
    if (areaOfLaw == null || areaOfLaw.isBlank()) {
      return;
    }

    String fileName = download.suggestedFilename();
    String areaKebab = areaOfLaw.trim().toLowerCase(java.util.Locale.ROOT).replace(" ", "-");
    String submissionId = TestContext.current().get("mostRecentSubmissionId");
    if (submissionId == null || submissionId.isBlank()) {
      submissionId = summaryPage().submissionReference();
    }
    String office = summaryPage().getSummaryData().getOrDefault("Account", "");
    String today = java.time.LocalDate.now().toString();
    String expected = "submission-claims-" + areaKebab + "-" + submissionId + "-" + office + "-" + today + ".csv";
    if (!fileName.equalsIgnoreCase(expected)) {
      throw new AssertionError("Unexpected export filename. Expected " + expected + " but got " + fileName);
    }
  }

  // ── Error / message banners ───────────────────────────────────────────────

  @Then("I should see an error banner saying {string}")
  public void iShouldSeeAnErrorBannerSaying(String expected) {
    long deadline = System.currentTimeMillis() + 120_000;
    while (System.currentTimeMillis() < deadline) {
      String body = page().locator("body").innerText();
      if (body.contains(expected)) {
        return;
      }

      String url = page().url();
      // Validation can remain on in-progress for a short period before redirecting to summary.
      if (url.contains("upload-is-being-checked")) {
        page().waitForTimeout(1500);
        page().reload();
      } else {
        page().waitForTimeout(1000);
      }
    }

    String finalBody = page().locator("body").innerText();
    String snippet = finalBody.length() > 600 ? finalBody.substring(0, 600) + "..." : finalBody;
    throw new AssertionError(
        "Expected error banner text: "
            + expected
            + " | current URL: "
            + page().url()
            + " | body snippet: "
            + snippet);
  }

  @Then("I should see a submission message saying {string}")
  public void iShouldSeeAMessageSaying(String expected) {
    if ("No submissions were found.".equals(expected)) {
      searchPage().verifyNoSubmissionsMessage();
      return;
    }
    if (!page().locator("body").innerText().contains(expected)) {
      throw new AssertionError("Expected message text: " + expected);
    }
  }

  @Then("the user sees an error message {string}")
  public void theUserSeesAnErrorMessage(String expected) {
    if (!page().locator("body").innerText().contains(expected)) {
      throw new AssertionError("Expected upload error message: " + expected);
    }
  }


  @Then("I should see the following submission error messages:")
  public void iShouldSeeTheFollowingSubmissionErrorMessages(DataTable table) {
    assertTableMessagesPresent(table);
  }

  @Then("I should have {int} submission error for {string}")
  public void iShouldHaveSubmissionErrorFor(int expectedErrorCount, String areaOfLaw, DataTable table) {
    summaryPage().waitForReady();
    List<String> errors = summaryPage().getSubmissionErrors();
    if (errors.size() != expectedErrorCount) {
      throw new AssertionError(
          "Expected " + expectedErrorCount + " submission errors for " + areaOfLaw + " but found " + errors.size());
    }
    if (table != null) {
      assertTableMessagesPresent(table);
    }
  }

  @Then("I should now see the following detailed submission error messages for {string}:")
  public void iShouldNowSeeTheFollowingDetailedSubmissionErrorMessagesFor(String areaOfLaw, String docString) {
    if (docString == null || docString.isBlank()) {
      throw new AssertionError("Expected detailed submission messages for " + areaOfLaw + " but doc string was empty");
    }
    java.util.Set<String> actual = summaryPage().getPaginatedSubmissionErrors(10);
    List<String> expectedMessages = docString.lines().map(String::trim).filter(s -> !s.isBlank()).toList();
    for (String expected : expectedMessages) {
      boolean found = actual.stream().anyMatch(a -> a.contains(expected));
      if (!found) {
        throw new AssertionError("Expected detailed submission message not found for " + areaOfLaw + ": " + expected);
      }
    }
  }

  @Then("I should see a submission error message for {string}")
  public void iShouldSeeASubmissionErrorMessageFor(@SuppressWarnings("unused") String context, String docString) {
    if (docString == null || docString.isBlank()) {
      throw new PendingException("Expected doc string submission error message");
    }
    if (!page().locator("body").innerText().contains(docString.trim())) {
      throw new AssertionError("Expected submission error message: " + docString.trim());
    }
  }

  // ── Claims table assertions ───────────────────────────────────────────────

  @Then("The claims should have the following information for {string}:")
  public void theClaimsShouldHaveTheFollowingInformationFor(@SuppressWarnings("unused") String areaOfLaw, DataTable table) {
    assertClaimsTableContainsExpectedValues(table);
  }

  // ── Matter starts ─────────────────────────────────────────────────────────

  @Then("I should see the submission summary for {string} with matter starts matching the generated file")
  public void iShouldSeeTheSubmissionSummaryWithMatterStartsMatchingGeneratedFile(String areaOfLaw) {
    summaryPage().waitForReady();
    if (!summaryPage().containsAreaOfLaw(areaOfLaw)) {
      throw new AssertionError("Expected summary to include area of law: " + areaOfLaw);
    }

    List<Map<String, String>> matterStarts = summaryPage().getMatterStartsData();
    if (matterStarts.isEmpty()) {
      throw new AssertionError("Expected matter starts data to be visible");
    }

    Map<String, Integer> expectedMatterStarts = TestContext.current().get("matterStartCounts");
    if (expectedMatterStarts == null || expectedMatterStarts.isEmpty()) {
      throw new AssertionError("Expected generated matter start counts in scenario context");
    }

    String areaKey = areaOfLaw.trim().toLowerCase(java.util.Locale.ROOT);
    if ("mediation".equals(areaKey)) {
      int totalExpected = expectedMatterStarts.values().stream().mapToInt(Integer::intValue).sum();
      Map<String, String> totalRow = matterStarts.stream()
          .filter(row -> row.getOrDefault("code", "").toLowerCase(java.util.Locale.ROOT).contains("new matter starts"))
          .findFirst()
          .orElse(matterStarts.get(0));

      int actualTotal;
      try {
        actualTotal = Integer.parseInt(totalRow.getOrDefault("count", "0").replace(",", ""));
      } catch (NumberFormatException e) {
        throw new AssertionError("Could not parse mediation matter starts total from row: " + totalRow, e);
      }

      if (actualTotal != totalExpected) {
        throw new AssertionError(
            "Expected mediation matter starts total " + totalExpected + " but got " + actualTotal + ". Rows: " + matterStarts);
      }
      return;
    }

    Map<String, Integer> actualByCode = new java.util.LinkedHashMap<>();
    for (Map<String, String> row : matterStarts) {
      String code = row.getOrDefault("code", "").trim();
      String rawCount = row.getOrDefault("count", "0").replace(",", "").trim();
      if (code.isBlank()) {
        continue;
      }
      try {
        actualByCode.put(code, Integer.parseInt(rawCount));
      } catch (NumberFormatException e) {
        throw new AssertionError("Could not parse matter starts count for code " + code + ": " + rawCount, e);
      }
    }

    for (Map.Entry<String, Integer> expected : expectedMatterStarts.entrySet()) {
      Integer actual = actualByCode.get(expected.getKey());
      if (actual == null) {
        throw new AssertionError("Expected matter start code not found: " + expected.getKey() + ". Actual rows: " + actualByCode);
      }
      if (!actual.equals(expected.getValue())) {
        throw new AssertionError(
            "Expected matter start code " + expected.getKey() + " to have count " + expected.getValue() + " but got " + actual);
      }
    }
  }

  @Then("I should see the submission summary for {string} with no matter starts message")
  public void iShouldSeeTheSubmissionSummaryWithNoMatterStartsMessage(@SuppressWarnings("unused") String areaOfLaw) {
    summaryPage().waitForReady();
    String message = summaryPage().validateNoMatterStartsMessage();
    if (message.isBlank()) {
      throw new AssertionError("Expected no matter starts message to be displayed");
    }
  }

  @Then("I should see the submission summary for {string} without a matter starts tab")
  public void iShouldSeeTheSubmissionSummaryWithoutAMatterStartsTab(@SuppressWarnings("unused") String areaOfLaw) {
    summaryPage().waitForReady();
    summaryPage().ensureMatterStartsTabHidden();
  }

  // ── Duplicate submission error ────────────────────────────────────────────

  @Then("I should have duplicate submission error for {string} {string}")
  public void iShouldHaveDuplicateSubmissionErrorFor(
      @SuppressWarnings("unused") String office,
      String areaOfLaw,
      @SuppressWarnings("unused") DataTable table) {
    long deadline = System.currentTimeMillis() + 120_000;
    while (System.currentTimeMillis() < deadline) {
      String url = page().url();
      if (url.contains("upload-is-being-checked")) {
        page().waitForTimeout(1500);
        page().reload();
        continue;
      }

      com.microsoft.playwright.Locator duplicateLocator =
          page().locator("[data-sort-value*='Submission already exists']").first();
      if (duplicateLocator.count() > 0) {
        String text = duplicateLocator.getAttribute("data-sort-value");
        String normalized = text == null ? "" : text.trim();
        if (!normalized.isBlank()) {
          if (!normalized.toUpperCase().contains(areaOfLaw.toUpperCase())) {
            throw new AssertionError(
                "Expected duplicate submission error for " + areaOfLaw + ". Actual: " + normalized);
          }
          if (!normalized.contains("Submission already exists for Office")) {
            throw new AssertionError(
                "Expected duplicate submission message to mention office. Actual: " + normalized);
          }
          if (!normalized.matches(".*Period \\([A-Z]{3}-\\d{4}\\).*")) {
            throw new AssertionError(
                "Expected duplicate submission message to mention submission period. Actual: " + normalized);
          }
          return;
        }
      }

      String allText = String.join("\n", summaryPage().getSubmissionErrors());
      if (!allText.isBlank() && allText.toUpperCase().contains(areaOfLaw.toUpperCase())) {
        return;
      }

      page().waitForTimeout(500);
    }

    String finalBody = page().locator("body").innerText();
    String snippet = finalBody.length() > 600 ? finalBody.substring(0, 600) + "..." : finalBody;
    throw new AssertionError(
        "Expected duplicate submission error for "
            + areaOfLaw
            + ". URL: "
            + page().url()
            + " | body snippet: "
            + snippet);
  }

  // ── Submission total ──────────────────────────────────────────────────────

  @Then("the submission summary total should be {string}")
  public void theSubmissionSummaryTotalShouldBe(String expected) {
    // Wait for validation to complete and redirect away from in-progress page
    long deadline = System.currentTimeMillis() + 120_000;
    while (System.currentTimeMillis() < deadline) {
      String url = page().url();
      if (url.contains("upload-is-being-checked")) {
        page().waitForTimeout(1500);
        page().reload();
        continue;
      }
      break;
    }
    summaryPage().waitForReady();
    String actual = summaryPage().getSummaryData().getOrDefault("Calculated bulk claim value", "");
    if (!actual.equals(expected)) {
      throw new AssertionError("Expected submission total " + expected + " but got " + actual);
    }
  }

  @Then("the import is complete and submission details are displayed")
  public void theImportIsCompleteAndSubmissionDetailsAreDisplayed() {
    summaryPage().waitForReady();
    String reference = summaryPage().submissionReference();
    if (reference == null || reference.isBlank()) {
      throw new AssertionError("Expected submission reference to be visible on submission summary");
    }

    Path generated = TestContext.current().generatedFile();
    if (generated != null) {
      String expectedName = generated.getFileName().toString();
      String body = page().locator("body").innerText();
      if (!body.contains(expectedName)) {
        throw new AssertionError("Expected uploaded filename in page body: " + expectedName);
      }
    }
  }

  // ── Void claim via endpoint ───────────────────────────────────────────────

  @When("I void claim {int} via the void endpoint")
  public void iVoidClaimViaTheVoidEndpoint(int claimNumber) {
    String submissionId = TestContext.current().get("mostRecentSubmissionId");
    if (submissionId == null || submissionId.isBlank()) {
      submissionId = TestContext.current().get("last.submission.reference");
    }
    if (submissionId == null || submissionId.isBlank()) {
      submissionId = summaryPage().submissionReference();
    }
    if (submissionId == null || submissionId.isBlank()) {
      throw new PendingException("No submission reference available to void claim");
    }

    String claimId = resolveClaimId(submissionId, Math.max(claimNumber - 1, 0));
    String baseUrl = requiredEnv("DSTEW_API_BASE_URL");
    String token = requiredEnv("DSTEW_API_TOKEN");

    String payload = "{\"created_by_user_id\":\"" + UUID.randomUUID() + "\",\"assessment_reason\":\"String\"}";

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/v1/claims/" + claimId + "/void"))
        .header("accept", "application/json")
        .header("Content-Type", "application/json")
        .header("Authorization", token)
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .build();

    try {
      HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new AssertionError("Void endpoint failed. Status: " + response.statusCode());
      }
      page().reload();
    } catch (AssertionError ae) {
      throw ae;
    } catch (Exception e) {
      throw new IllegalStateException("Void endpoint call failed", e);
    }
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private void assertTableMessagesPresent(DataTable table) {
    if (table == null) {
      throw new PendingException("Expected DataTable with messages but none provided");
    }

    long deadline = System.currentTimeMillis() + 120_000;
    while (System.currentTimeMillis() < deadline) {
      String url = page().url();
      if (url.contains("upload-is-being-checked")) {
        page().waitForTimeout(1500);
        page().reload();
        continue;
      }

      String body = page().locator("body").innerText();
      boolean allFound = true;
      String missing = null;
      for (List<String> row : table.asLists()) {
        if (row.isEmpty()) {
          continue;
        }
        String message = row.getFirst();
        if ("Error Message".equalsIgnoreCase(message) || "Heading".equalsIgnoreCase(message)) {
          continue;
        }
        if (message.contains("CURRENT_MONTH")) {
          String currentSubmissionMonth = TestContext.current().get("currentSubmissionMonth");
          if (currentSubmissionMonth != null && !currentSubmissionMonth.isBlank()) {
            message = message.replace("CURRENT_MONTH", currentSubmissionMonth);
          }
        }
        if (!body.contains(message)) {
          allFound = false;
          missing = message;
          break;
        }
      }

      if (allFound) {
        return;
      }

      page().waitForTimeout(500);
    }

    String finalBody = page().locator("body").innerText();
    String snippet = finalBody.length() > 600 ? finalBody.substring(0, 600) + "..." : finalBody;
    throw new AssertionError(
        "Expected submission messages were not present after waiting. URL: "
            + page().url()
            + " | body snippet: "
            + snippet);
  }

  private void assertClaimsTableContainsExpectedValues(DataTable table) {
    if (table == null) {
      throw new PendingException("Expected DataTable for claim values");
    }
    String tableText = page().locator("table.govuk-table").innerText();
    for (Map<String, String> row : table.asMaps(String.class, String.class)) {
      for (Map.Entry<String, String> entry : row.entrySet()) {
        String expected = entry.getValue();
        if (expected == null || expected.isBlank()) continue;
        if (!tableText.contains(expected)) {
          throw new AssertionError("Expected claim table to contain value: " + expected);
        }
      }
    }
  }

  private String resolveClaimId(String submissionId, int offset) {
    DatabaseManager manager = new DatabaseManager("void-claim-step");
    if (!manager.ensureInitialized()) {
      throw new IllegalStateException("Database not available to resolve claim ID");
    }
    try {
      List<Object[]> rows = manager.query(
          "SELECT id FROM claims.claim WHERE submission_id = '" + submissionId + "' ORDER BY id ASC");
      if (rows.size() <= offset) {
        throw new IllegalStateException("Could not resolve claim row " + offset + " for submission " + submissionId);
      }
      return String.valueOf(rows.get(offset)[0]);
    } catch (java.sql.SQLException e) {
      throw new IllegalStateException("Failed to resolve claim ID for submission " + submissionId, e);
    } finally {
      manager.destroy();
    }
  }

  private String requiredEnv(String name) {
    String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Missing required env: " + name);
    }
    return value;
  }
}

