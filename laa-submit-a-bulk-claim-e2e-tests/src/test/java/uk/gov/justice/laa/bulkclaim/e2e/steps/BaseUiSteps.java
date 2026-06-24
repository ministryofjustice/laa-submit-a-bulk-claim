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
import uk.gov.justice.laa.bulkclaim.e2e.state.TestContext;
import uk.gov.justice.laa.bulkclaim.e2e.utils.data.SubmissionPeriodHelper;

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
    return "http://localhost:8082";
  }

  protected void clickButton(String label) {
    page()
            .getByRole(
                    com.microsoft.playwright.options.AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName(label))
            .click();
  }
}
