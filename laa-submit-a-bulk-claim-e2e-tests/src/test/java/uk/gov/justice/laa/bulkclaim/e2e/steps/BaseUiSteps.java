package uk.gov.justice.laa.bulkclaim.e2e.steps;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.FilePayload;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.PendingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.gov.justice.laa.bulkclaim.e2e.pages.BulkImportPage;
import uk.gov.justice.laa.bulkclaim.e2e.pages.BulkInProgressPage;
import uk.gov.justice.laa.bulkclaim.e2e.pages.ClaimDetailPage;
import uk.gov.justice.laa.bulkclaim.e2e.pages.SearchPage;
import uk.gov.justice.laa.bulkclaim.e2e.pages.SubmissionSummaryPage;
import uk.gov.justice.laa.bulkclaim.e2e.state.TestContext;
import uk.gov.justice.laa.bulkclaim.e2e.utils.data.SubmissionPeriodHelper;
import uk.gov.justice.laa.bulkclaim.e2e.utils.files.FileGeneratorUtil;
import uk.gov.justice.laa.bulkclaim.e2e.utils.files.CrimeGenerator;
import uk.gov.justice.laa.bulkclaim.e2e.utils.files.LegalHelpGenerator;
import uk.gov.justice.laa.bulkclaim.e2e.utils.files.MediationGenerator;

public abstract class BaseUiSteps {

  private static final DateTimeFormatter SUBMISSION_PERIOD_FORMATTER =
          new DateTimeFormatterBuilder()
                  .parseCaseInsensitive()
                  .appendPattern("MMM-yyyy")
                  .toFormatter(Locale.ENGLISH);
  private static final DateTimeFormatter SLASH_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private static final Pattern QUOTED_STEP_FILE = Pattern.compile("^[A-Za-z ]+\\\"([^\\\"]+)\\\".*$");

  protected Page page() {
    return TestContext.current().page();
  }

  protected String baseUrl() {
    String fromProperty = System.getProperty("e2e.baseUrl");
    if (fromProperty != null && !fromProperty.isBlank()) {
      return fromProperty;
    }
    String fromEnv = System.getenv("E2E_BASE_URL");
    if (fromEnv != null && !fromEnv.isBlank()) {
      return fromEnv;
    }
    return "http://localhost:8080";
  }

  protected BulkImportPage bulkImportPage() {
    return new BulkImportPage(page());
  }

  protected SearchPage searchPage() {
    return new SearchPage(page());
  }

  protected SubmissionSummaryPage summaryPage() {
    return new SubmissionSummaryPage(page());
  }

  protected ClaimDetailPage claimDetailPage() {
    return new ClaimDetailPage(page());
  }

  protected BulkInProgressPage bulkInProgressPage() {
    return new BulkInProgressPage(page());
  }

  protected Path generateMinimalFile(String areaOfLaw, String format, int outcomes) {
    String ext = format.toLowerCase();
    String safeArea = areaOfLaw.toLowerCase().replace(" ", "-");
    Path target =
            Paths.get(
                    "build",
                    "tmp",
                    "e2e",
                    "generated",
                    safeArea + "-" + System.currentTimeMillis() + "." + ext);
    try {
      return switch (normalizeArea(areaOfLaw)) {
        case "legal_help" -> LegalHelpGenerator.generateMinimalSubmissionFile(format, outcomes, target);
        case "mediation" -> MediationGenerator.generateMinimalSubmissionFile(format, outcomes, target);
        case "crime" -> CrimeGenerator.generateMinimalSubmissionFile(format, outcomes, target);
        default -> FileGeneratorUtil.generateMinimalSubmissionFile(areaOfLaw, format, outcomes, target);
      };
    } catch (Exception e) {
      throw new PendingException("Unable to generate minimal submission file: " + e.getMessage());
    }
  }

  protected Path generateFromTable(String areaOfLaw, String format, DataTable table) {
    Path target =
            Paths.get(
                    "build",
                    "tmp",
                    "e2e",
                    "generated",
                    areaOfLaw.toLowerCase().replace(" ", "-") + "-" + System.currentTimeMillis() + "." + format.toLowerCase());

    List<Map<String, String>> rows = table == null ? List.of() : table.asMaps(String.class, String.class);
    try {
      return switch (normalizeArea(areaOfLaw)) {
        case "legal_help" -> LegalHelpGenerator.generateFromClaimsTable(format, rows, target);
        case "mediation" -> MediationGenerator.generateFromClaimsTable(format, rows, target);
        case "crime" -> CrimeGenerator.generateFromClaimsTable(format, rows, target);
        default -> FileGeneratorUtil.generateFromClaimsTable(areaOfLaw, format, rows, target);
      };
    } catch (Exception e) {
      throw new PendingException("Unable to generate file from DataTable: " + e.getMessage());
    }
  }

  protected Path generateSpecialFile(String type, String fileName) {
    Path dir = Path.of("build", "tmp", "e2e", "generated");
    Path target = dir.resolve(fileName);
    try {
      return switch (type) {
        case "empty" -> FileGeneratorUtil.generateEmptyFile(target);
        case "invalid", "restricted" -> FileGeneratorUtil.generateInvalidFile(target);
        case "large" -> FileGeneratorUtil.generateLargeFile(target, 11);
        default -> throw new IllegalArgumentException("Unsupported generated file type: " + type);
      };
    } catch (Exception e) {
      throw new PendingException("Unable to generate file type " + type + ": " + e.getMessage());
    }
  }

  protected void uploadCurrentFile() {
    Path file = TestContext.current().generatedFile();
    if (file == null || !Files.exists(file)) {
      throw new PendingException(
              "No generated/staged file is available for upload in scenario context."
                      + " Migrate the file generation/staging step first.");
    }
    logCrimeUploadSnapshot(file);
    bulkImportPage().uploadAndSubmit(file);
  }

  private void logCrimeUploadSnapshot(Path file) {
    try {
      List<String> lines = Files.readAllLines(file);
      String scheduleLine = lines.stream().filter(l -> l.startsWith("SCHEDULE,")).findFirst().orElse(null);
      if (scheduleLine == null || !scheduleLine.toUpperCase(Locale.ROOT).contains("AREAOFLAW=CRIME LOWER")) {
        return;
      }

      String outcomeLine = lines.stream().filter(l -> l.startsWith("OUTCOME,")).findFirst().orElse(null);
      if (outcomeLine == null) {
        System.out.println("[CRIME-UPLOAD-DEBUG] file=" + file + " schedule=" + scheduleLine + " firstOutcome=<none>");
        return;
      }

      String office = extractToken(lines.stream().filter(l -> l.startsWith("OFFICE,")).findFirst().orElse(null), "account");
      String period = extractToken(scheduleLine, "submissionPeriod");
      String feeCode = extractToken(outcomeLine, "FEE_CODE");
      String caseStartDate = extractToken(outcomeLine, "CASE_START_DATE");
      String workConcludedDate = extractToken(outcomeLine, "WORK_CONCLUDED_DATE");
      String ufn = extractToken(outcomeLine, "UFN");

      System.out.println(
          "[CRIME-UPLOAD-DEBUG] file="
              + file
              + " office="
              + office
              + " period="
              + period
              + " fee="
              + feeCode
              + " caseStart="
              + caseStartDate
              + " workConcluded="
              + workConcludedDate
              + " ufn="
              + ufn);
    } catch (Exception e) {
      System.out.println("[CRIME-UPLOAD-DEBUG] Unable to inspect file " + file + ": " + e.getMessage());
    }
  }

  private String extractToken(String line, String key) {
    if (line == null || key == null) {
      return "";
    }
    Pattern pattern = Pattern.compile("(?:^|,)" + Pattern.quote(key) + "=([^,]*)");
    Matcher matcher = pattern.matcher(line);
    return matcher.find() ? matcher.group(1) : "";
  }

  protected void uploadCurrentFileWithMime(String mimeType) {
    Path file = TestContext.current().generatedFile();
    if (file == null || !Files.exists(file)) {
      throw new PendingException("No generated/staged file is available for MIME upload.");
    }

    byte[] bytes;
    try {
      bytes = Files.readAllBytes(file);
    } catch (Exception e) {
      throw new PendingException("Could not read generated file for MIME upload: " + e.getMessage());
    }

    FilePayload payload = new FilePayload(file.getFileName().toString(), mimeType, bytes);
    page().locator("input[type='file']").first().setInputFiles(payload);
    bulkImportPage().submitWithoutFile();
  }

  protected void uploadNamedFile(String contextKey) {
    Path file = TestContext.current().get(contextKey);
    if (file == null || !Files.exists(file)) {
      throw new PendingException("No file in context for key: " + contextKey);
    }
    TestContext.current().generatedFile(file);
    bulkImportPage().uploadAndSubmit(file);
  }

  protected void waitForInProgressOrSummary() {
    page().waitForURL(
            url ->
                    url.contains("upload-is-being-checked")
                            || url.contains("view-submission-detail")
                            || url.contains("search"));
  }

  protected void clickButton(String label) {
    page()
            .getByRole(
                    com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName(label))
            .click();
  }

  protected void goToBulkImportPageFromNavigation() {
    page().waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);

    com.microsoft.playwright.Locator importLink = page().locator("#import-claims-link").first();
    if (importLink.count() > 0) {
      importLink.click(new com.microsoft.playwright.Locator.ClickOptions().setTimeout(60_000));
    } else {
      // Fallback keeps legacy scenarios moving even if nav is not rendered on the current page.
      bulkImportPage().open(baseUrl());
      return;
    }

    page().waitForURL(
            url -> url.contains("/upload"),
            new com.microsoft.playwright.Page.WaitForURLOptions().setTimeout(60_000));
    page().waitForSelector(
            "#file-input-input",
            new com.microsoft.playwright.Page.WaitForSelectorOptions()
                    .setState(com.microsoft.playwright.options.WaitForSelectorState.ATTACHED)
                    .setTimeout(60_000));
  }

  protected void storeBulkInProgressContext() {
     if (page().locator("meta#submissionId").count() > 0) {
       String submissionId = page().locator("meta#submissionId").getAttribute("content");
       if (submissionId != null && !submissionId.isBlank()) {
         TestContext.current().put("mostRecentSubmissionId", submissionId);
         TestContext.current().addCleanupSubmissionId(submissionId);
       }
     }
   }

  protected void overrideGeneratedCaseStartDate(String value) {
    Path file = TestContext.current().generatedFile();
    if (file == null) {
      throw new PendingException("No generated file available to update case start date");
    }
    try {
      FileGeneratorUtil.overrideField(file, "caseStartDate", value);
    } catch (Exception e) {
      throw new PendingException("Failed to update case start date: " + e.getMessage());
    }
  }

  protected void overrideGeneratedCaseStartDateRelativeToSubmissionPeriod(int dayOfMonth, int monthsBefore) {
    Path file = TestContext.current().generatedFile();
    if (file == null) {
      throw new PendingException("No generated file available to update case start date");
    }

    try {
      String submissionPeriod = extractSubmissionPeriod(file);
      YearMonth period = YearMonth.parse(submissionPeriod, SUBMISSION_PERIOD_FORMATTER);
      LocalDate newCaseStartDate = period.minusMonths(monthsBefore).atDay(dayOfMonth);
      String formattedNewCaseStart = newCaseStartDate.format(SLASH_DATE_FORMATTER);
      
      // Update CASE_START_DATE
      FileGeneratorUtil.overrideField(file, "caseStartDate", formattedNewCaseStart);
      
      // Recalculate and update dependent dates to ensure they're after the new case start date
      LocalDate latestAllowed = period.plusMonths(1).atDay(20);
      if (latestAllowed.isBefore(LocalDate.now())) {
        latestAllowed = LocalDate.now();
      }
      
      // Work Concluded Date should be at least 1 day after case start, but not past period boundary
      LocalDate newWorkConcludedDate = newCaseStartDate.plusDays(1);
      if (newWorkConcludedDate.isAfter(latestAllowed)) {
        newWorkConcludedDate = latestAllowed;
      }
      String formattedWorkConcluded = newWorkConcludedDate.format(SLASH_DATE_FORMATTER);
      FileGeneratorUtil.overrideField(file, "workConcludedDate", formattedWorkConcluded);
      
      // Rep Order Date should match case start date
      FileGeneratorUtil.overrideField(file, "repOrderDate", formattedNewCaseStart);
      
      // Transfer Date and Surgery Date should be after case start
      FileGeneratorUtil.overrideField(file, "transferDate", formattedWorkConcluded);
      FileGeneratorUtil.overrideField(file, "surgeryDate", formattedWorkConcluded);
      
    } catch (Exception e) {
      throw new PendingException("Failed to update case start date: " + e.getMessage());
    }
  }

  private String extractSubmissionPeriod(Path file) {
    try {
      List<String> lines = Files.readAllLines(file);
      for (String line : lines) {
        if (line != null && line.startsWith("SCHEDULE,")) {
          String[] parts = line.split(",");
          for (String part : parts) {
            if (part.startsWith("submissionPeriod=")) {
              return part.substring("submissionPeriod=".length()).trim();
            }
          }
        }
      }
    } catch (Exception e) {
      throw new PendingException("Failed to read generated file for submission period: " + e.getMessage());
    }
    throw new PendingException("submissionPeriod not found in generated file");
  }

  protected void updateSubmissionPeriodValue(String periodType) {
    Path file = TestContext.current().generatedFile();
    if (file == null) {
      throw new PendingException("No generated file available to update submission period");
    }
    String value;
    YearMonth currentMonth = YearMonth.now();
    String currentSubmissionMonth =
        currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.UK) + " " + currentMonth.getYear();
    TestContext.current().put("currentSubmissionMonth", currentSubmissionMonth);
    if ("CurrentMonth".equalsIgnoreCase(periodType)) {
      value = SubmissionPeriodHelper.getSubmissionPeriod("+0", true);
    } else if ("FutureDate".equalsIgnoreCase(periodType)) {
      value = SubmissionPeriodHelper.getSubmissionPeriod("+2", true);
    } else {
      value = periodType;
    }
    try {
      FileGeneratorUtil.overrideField(file, "submissionPeriod", value);
    } catch (Exception e) {
      throw new PendingException("Failed to update submission period: " + e.getMessage());
    }
  }

  protected Map<String, String> tableMap(DataTable table) {
    Map<String, String> out = new HashMap<>();
    if (table == null) {
      return out;
    }
    List<Map<String, String>> rows = table.asMaps(String.class, String.class);
    if (!rows.isEmpty()) {
      out.putAll(rows.get(0));
    }
    return out;
  }

  protected Path extractStepFile(String step) {
    Matcher matcher = QUOTED_STEP_FILE.matcher(step);
    if (!matcher.matches()) {
      throw new PendingException("Could not parse file path from step: " + step);
    }
    String relative = matcher.group(1);

    Path workspace = findWorkspaceRoot(Path.of("").toAbsolutePath());
    // Working directory when Gradle runs tests is the subproject directory
    Path projectDir = Path.of("").toAbsolutePath();
    Path[] candidates = {
            Path.of(relative).toAbsolutePath().normalize(),
            // Java test resources (primary location for static fixture files)
            projectDir.resolve("src/test/resources").resolve(relative).normalize(),
            workspace.resolve(relative).normalize(),
            workspace.resolve("bulk-submission-and-fee-scheme-tests-").resolve(relative).normalize(),
            workspace.resolve("laa-submit-a-bulk-claim").resolve(relative).normalize(),
            workspace.resolve("laa-submit-a-bulk-claim")
                    .resolve("laa-submit-a-bulk-claim-e2e-tests/src/test/resources")
                    .resolve(relative).normalize()
    };

    for (Path candidate : candidates) {
      if (Files.exists(candidate)) {
        return candidate;
      }
    }

    throw new PendingException(
            "File referenced by step was not found: "
                    + relative
                    + ". Checked candidates under workspace and TS test project.");
  }

  protected Path findWorkspaceRoot(Path start) {
    Path current = start;
    while (current != null) {
      if (Files.exists(current.resolve("laa-submit-a-bulk-claim"))
              && Files.exists(current.resolve("bulk-submission-and-fee-scheme-tests-"))) {
        return current;
      }
      current = current.getParent();
    }
    return start;
  }

  private String normalizeArea(String areaOfLaw) {
    if (areaOfLaw == null) {
      return "";
    }
    String key = areaOfLaw.trim().toUpperCase();
    return switch (key) {
      case "LEGAL HELP" -> "legal_help";
      case "MEDIATION" -> "mediation";
      case "CRIME", "CRIME LOWER" -> "crime";
      default -> key.toLowerCase();
    };
  }
}
