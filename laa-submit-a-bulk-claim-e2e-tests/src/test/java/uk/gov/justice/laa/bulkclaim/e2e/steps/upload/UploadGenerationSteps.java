package uk.gov.justice.laa.bulkclaim.e2e.steps.upload;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import uk.gov.justice.laa.bulkclaim.e2e.state.TestContext;
import uk.gov.justice.laa.bulkclaim.e2e.steps.BaseUiSteps;
import uk.gov.justice.laa.bulkclaim.e2e.utils.files.GenerateMatterStartsFile;
import uk.gov.justice.laa.bulkclaim.e2e.utils.data.SubmissionPeriodHelper;
import uk.gov.justice.laa.bulkclaim.e2e.utils.files.CivilFileGenerator;
import uk.gov.justice.laa.bulkclaim.e2e.utils.files.FileGeneratorUtil;

public class UploadGenerationSteps extends BaseUiSteps {

  private static final HttpClient HTTP = HttpClient.newHttpClient();

  @Given("I generate {string} {string} file with {string} outcomes")
  public void iGenerateFileWithOutcomes(String areaOfLaw, String format, String outcomes) {
    int parsedOutcomes = Integer.parseInt(outcomes);
    Path generated = generateMinimalFile(areaOfLaw, format, parsedOutcomes);
    TestContext.current().generatedFile(generated);
    TestContext.current().put("generated.areaOfLaw", areaOfLaw);
    TestContext.current().put("generated.format", format);
    TestContext.current().put("generated.outcomes", parsedOutcomes);

    if (shouldKeepGeneratorSubmissionPeriod(areaOfLaw, List.of())) {
      System.out.println(
          "[UPLOAD-GENERATION] ℹ️ Keeping generator-selected submissionPeriod for " + areaOfLaw);
      return;
    }

    // Use SubmissionPeriodHelper to ensure unique period where generator does not manage this itself.
    try {
      String officeAccount = "0P322F"; // Default office for testing
      SubmissionPeriodHelper.SubmissionPeriodResult result =
          SubmissionPeriodHelper.getUniqueSubmissionPeriod(officeAccount, areaOfLaw, null);
      // Override the generated file with the unique period
      FileGeneratorUtil.overrideField(generated, "submissionPeriod", result.period());
      System.out.println(
          "[UPLOAD-GENERATION] 📅 Using period: "
              + result.period()
              + " for "
              + areaOfLaw);
    } catch (Exception e) {
      System.out.println(
          "[UPLOAD-GENERATION] ⚠️  Failed to get unique period: "
              + e.getMessage()
              + " - proceeding with default");
      // Fall back to default period if helper fails
    }
  }

  @Given("I generate {string} {string} with all matter type file")
  public void iGenerateWithAllMatterTypeFile(String areaOfLaw, String format) {
    Path target = generatedTarget(areaOfLaw.toLowerCase(Locale.ROOT).replace(" ", "-") + "-matter-starts", format);
    try {
      GenerateMatterStartsFile.MatterStartsGenerationResult result =
          GenerateMatterStartsFile.generateFile(areaOfLaw, format, target, true);
      TestContext.current().generatedFile(result.filePath());
      TestContext.current().put("matterStartCounts", result.counts());
      TestContext.current().put("generated.office", result.officeAccount());
      TestContext.current().put("generated.submissionPeriod", result.submissionPeriod());
    } catch (Exception e) {
      throw new PendingException("Unable to generate matter starts file: " + e.getMessage());
    }
  }

  @Given("I generate {string} {string} file with the following claims")
  @Given("I generate {string} {string} file with the following civil claims")
  @Given("I generate {string} {string} file with the following crime claims")
  @Given("I generate single {string} {string} file with the following claims")
  public void iGenerateFileWithClaimsTable(String areaOfLaw, String format, DataTable table) {
    List<Map<String, String>> rows = table == null ? List.of() : table.asMaps(String.class, String.class);
    if (table != null && areaOfLaw != null && areaOfLaw.toLowerCase(java.util.Locale.ROOT).contains("crime")) {
      if (!rows.isEmpty()) {
        TestContext.current().put("expectedCrimeClaim", rows.get(0));
      }
    }
    Path generated = generateFromTable(areaOfLaw, format, table);
    TestContext.current().generatedFile(generated);

    if (shouldKeepGeneratorSubmissionPeriod(areaOfLaw, rows)) {
      System.out.println(
          "[UPLOAD-GENERATION] ℹ️ Keeping generator-selected submissionPeriod for " + areaOfLaw);
      return;
    }

    // ✅ Use SubmissionPeriodHelper to ensure unique period
    try {
      String officeAccount = "0P322F"; // Default office for testing
      SubmissionPeriodHelper.SubmissionPeriodResult result =
          SubmissionPeriodHelper.getUniqueSubmissionPeriod(officeAccount, areaOfLaw, null);
      FileGeneratorUtil.overrideField(generated, "submissionPeriod", result.period());
      System.out.println(
          "[UPLOAD-GENERATION] 📅 Using period: "
              + result.period()
              + " for "
              + areaOfLaw);
    } catch (Exception e) {
      System.out.println(
          "[UPLOAD-GENERATION] ⚠️  Failed to get unique period: "
              + e.getMessage()
              + " - proceeding with default");
    }
  }

  private boolean shouldKeepGeneratorSubmissionPeriod(String areaOfLaw, List<Map<String, String>> rows) {
    String normalized = areaOfLaw == null ? "" : areaOfLaw.toLowerCase(Locale.ROOT);
    boolean generatorManagesPeriod =
        normalized.contains("legal help")
            || normalized.contains("mediation")
            || normalized.contains("crime");

    if (generatorManagesPeriod) {
      return true;
    }

    // Also preserve period/date alignment in any scenario that uses the special "later" concluded-date marker.
    return rows.stream()
        .map(r -> r.get("workConcludedDate"))
        .anyMatch(v -> v != null && v.toLowerCase(Locale.ROOT).contains("later"));
  }

  @Given("I generate {string} {string} file with the following claims from period {string}")
  public void iGenerateFileWithClaimsFromPeriod(
      String areaOfLaw,
      String format,
      String submissionPeriod,
      DataTable table) {
    Path generated = generateFromTable(areaOfLaw, format, table);
    TestContext.current().generatedFile(generated);
    try {
      FileGeneratorUtil.overrideField(generated, "submissionPeriod", submissionPeriod);
    } catch (Exception e) {
      throw new PendingException("Unable to override submission period on generated file: " + e.getMessage());
    }
  }

  @Given("I generate {string} {string} file with the following claims from period {string} with office {string}")
  public void iGenerateFileWithClaimsFromPeriodWithOffice(
      String areaOfLaw,
      String format,
      String submissionPeriod,
      String office,
      DataTable table) {
    Path generated = generateFromTable(areaOfLaw, format, table);
    TestContext.current().generatedFile(generated);
    try {
      FileGeneratorUtil.overrideField(generated, "submissionPeriod", submissionPeriod);
      FileGeneratorUtil.overrideField(generated, "office", office);
      TestContext.current().put("generated.office", office);
    } catch (Exception e) {
      throw new PendingException("Unable to override submission period/office on generated file: " + e.getMessage());
    }
  }

  @Given("I have generated an {string} bulk submission file named {string}")
  @Given("I have generated a {string} bulk submission file named {string}")
  public void iHaveGeneratedSpecialSubmissionFile(String type, String fileName) {
    if ("restricted".equalsIgnoreCase(type)) {
      try {
        Path staged = extractStepFile("I upload \"tests/data/generated_csv/" + fileName + "\"");
        TestContext.current().generatedFile(staged);
        return;
      } catch (Exception ignored) {
        // Fall back to legacy generator path if fixture is not present.
      }
    }
    Path generated = generateSpecialFile(type, fileName);
    TestContext.current().generatedFile(generated);
  }

  @Given("I duplicate the last record in the generated file")
  public void iDuplicateTheLastRecordInTheGeneratedFile() {
    Path file = TestContext.current().generatedFile();
    if (file == null) {
      throw new PendingException("No generated file available to duplicate last record");
    }
    try {
      FileGeneratorUtil.duplicateLastRow(file);
    } catch (Exception e) {
      throw new PendingException("Failed to duplicate last record: " + e.getMessage());
    }
  }

  @Given("I override the generated file field {string} with value {string}")
  public void iOverrideTheGeneratedFileField(String field, String value) {
    Path file = TestContext.current().generatedFile();
    if (file == null) {
      throw new PendingException("No generated file available to override field");
    }
    try {
      FileGeneratorUtil.overrideField(file, field, value);
    } catch (Exception e) {
      throw new PendingException("Failed to override field: " + e.getMessage());
    }
  }

  @Given("I update the SubmissionPeriod to {string}")
  public void iUpdateTheSubmissionPeriodTo(String periodType) {
    updateSubmissionPeriodValue(periodType);
  }

  @Given("I update only the last record with a new UCN")
  public void iUpdateOnlyTheLastRecordWithANewUCN(DataTable table) {
    Path file = TestContext.current().generatedFile();
    if (file == null) {
      throw new PendingException("No generated file available to update last record");
    }
    try {
      FileGeneratorUtil.updateLastRowFields(file, tableMap(table));
    } catch (Exception e) {
      throw new PendingException("Failed to update last row fields: " + e.getMessage());
    }
  }

  @Given("I update case start date to be on 20 and 2 month before submission period")
  public void iUpdateCaseStartDateTo20And2MonthsBeforeSubmissionPeriod() {
    overrideGeneratedCaseStartDateRelativeToSubmissionPeriod(20, 2);
  }

  @Given("I update case start date to be on 21 and 2 month before submission period")
  public void iUpdateCaseStartDateTo21And2MonthsBeforeSubmissionPeriod() {
    overrideGeneratedCaseStartDateRelativeToSubmissionPeriod(21, 2);
  }

  @Given("I generate {string} {string} file for office {string} with the following immigration claims")
  public void iGenerateFileForOfficeWithImmigrationClaims(
      String areaOfLaw,
      String format,
      String office,
      DataTable table) {
    if (!"Legal help".equalsIgnoreCase(areaOfLaw)) {
      throw new PendingException("Immigration claim generator currently supports only Legal help");
    }

    List<Map<String, String>> rows = table == null ? List.of() : table.asMaps(String.class, String.class);
    Path target = generatedTarget("legal-help", format);
    try {
      Path generated = CivilFileGenerator.generateLegalHelpImmigrationFile(format, office, rows, target);
      TestContext.current().generatedFile(generated);
      TestContext.current().put("generated.office", office);
    } catch (Exception e) {
      throw new PendingException("Unable to generate immigration claim file: " + e.getMessage());
    }
  }

  @Given("I generate two Legal help files in {string} format for office {string} that are {string} months apart with the following claims")
  public void iGenerateTwoLegalHelpFilesMonthsApart(
      String format,
      String office,
      String monthsDifference,
      DataTable table) {
    generateTwoLegalHelpFiles(format, office, Integer.parseInt(monthsDifference), table, false);
  }

  @Given("I generate two Legal help files outside the duplicate cutoff in {string} format for office {string} with the following claims")
  public void iGenerateTwoLegalHelpFilesOutsideDuplicateCutoff(
      String format,
      String office,
      DataTable table) {
    generateTwoLegalHelpFiles(format, office, 3, table, true);
  }

  private void generateTwoLegalHelpFiles(
      String format,
      String office,
      int monthsDifference,
      DataTable table,
      boolean outsideCutoff) {
    List<Map<String, String>> rows = table == null ? List.of() : table.asMaps(String.class, String.class);
    Map<String, String> claimRow = rows.isEmpty() ? Map.of() : rows.get(0);

    Path firstTarget = generatedTarget("legal-help-duplicate-first", format);
    Path secondTarget = generatedTarget("legal-help-duplicate-second", format);

    try {
      FileGeneratorUtil.GeneratedFilePair files =
          outsideCutoff
              ? CivilFileGenerator.generateTwoLegalHelpFilesOutsideDuplicateCutoff(
                  format, office, claimRow, firstTarget, secondTarget)
              : CivilFileGenerator.generateTwoLegalHelpFilesMonthsApart(
                  format, office, monthsDifference, claimRow, firstTarget, secondTarget);

      TestContext.current().put("first.generated.file", files.firstFile());
      TestContext.current().put("second.generated.file", files.secondFile());
      TestContext.current().generatedFile(files.firstFile());
      TestContext.current().put("generated.office", office);
    } catch (Exception e) {
      throw new PendingException("Unable to generate two Legal help files: " + e.getMessage());
    }
  }

  @Given("I make the generated file invalid")
  public void iMakeTheGeneratedFileInvalid() {
    String submissionId = TestContext.current().get("mostRecentSubmissionId");
    if (submissionId == null || submissionId.isBlank()) {
      throw new PendingException("No latest submission id found in scenario context");
    }

    String baseUrl = System.getenv("DSTEW_API_BASE_URL");
    String token = System.getenv("DSTEW_API_TOKEN");
    if (baseUrl == null || baseUrl.isBlank() || token == null || token.isBlank()) {
      throw new PendingException("DSTEW_API_BASE_URL or DSTEW_API_TOKEN is missing");
    }

    String payload = "{\"status\":\"VALIDATION_FAILED\"}";
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/v1/submissions/" + submissionId))
        .header("Content-Type", "application/json")
        .header("accept", "application/json")
        .header("Authorization", token)
        .method("PATCH", HttpRequest.BodyPublishers.ofString(payload))
        .build();

    try {
      HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new AssertionError("Failed to patch submission status. HTTP " + response.statusCode());
      }
    } catch (Exception e) {
      throw new PendingException("Failed to patch submission status: " + e.getMessage());
    }
  }

  @Given("today's date/time in Europe/London falls in the {string}")
  public void todaysDateTimeInEuropeLondonFallsInThe(String offset) {
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/London"));
    int monthOffset = parseMonthOffset(offset);
    ZonedDateTime shifted = now.plusMonths(monthOffset);
    String monthName = shifted.getMonth().getDisplayName(TextStyle.FULL, Locale.UK);
    String value = monthName + " " + shifted.getYear();
    TestContext.current().put("currentSubmissionMonth", value);
  }

  private int parseMonthOffset(String offset) {
    if (offset == null) {
      return 0;
    }
    String normalized = offset.trim().toLowerCase(Locale.ROOT);
    java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("month\\+(\\d+)").matcher(normalized);
    if (matcher.find()) {
      return Integer.parseInt(matcher.group(1));
    }
    return 0;
  }

  private Path generatedTarget(String prefix, String format) {
    return Path.of(
        "build",
        "tmp",
        "e2e",
        "generated",
        prefix + "-" + System.currentTimeMillis() + "." + format.toLowerCase());
  }
}
