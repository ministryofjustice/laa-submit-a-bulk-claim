package uk.gov.justice.laa.bulkclaim.accessibility.helpers;

import com.deque.html.axecore.playwright.AxeBuilder;
import com.deque.html.axecore.results.AxeResults;
import com.deque.html.axecore.results.Rule;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Assertions;

public final class AccessibilityAxeHelper {

  private static final List<String> TAGS =
      List.of("wcag2a", "wcag2aa", "wcag21a", "wcag21aa", "wcag22a", "wcag22aa", "best-practice");

  private AccessibilityAxeHelper() {}

  public static void assertAccessible(Page page, String scenarioName) throws IOException {
    assertAccessible(page, scenarioName, List.of());
  }

  public static void assertAccessible(Page page, String scenarioName, List<String> disabledRules)
      throws IOException {
    page.waitForLoadState(LoadState.NETWORKIDLE);
    waitForDynamicWidgets(page);

    AxeBuilder axeBuilder =
        new AxeBuilder(page)
            .withTags(TAGS)
            .exclude("#meta-refresh")
            .exclude("#govuk-back-link-container")
            .exclude(".moj-scrollable-pane")
            .exclude(".govuk-pagination__item.govuk-pagination__item--ellipses");

    if (disabledRules != null && !disabledRules.isEmpty()) {
      axeBuilder = axeBuilder.disableRules(disabledRules);
    }

    AxeResults results = axeBuilder.analyze();
    saveDiagnostics(page, scenarioName, results);

    String violations = formatResultIds(results.getViolations());
    String incomplete = formatResultIds(results.getIncomplete());

    Assertions.assertEquals(List.of(), results.getViolations(), "Violations: " + violations);
    Assertions.assertEquals(List.of(), results.getIncomplete(), "Incompletes: " + incomplete);
  }

  private static void waitForDynamicWidgets(Page page) {
    List<String> selectors = List.of("[id*='datepicker']", "[role='dialog']", "[aria-haspopup]");
    for (String selector : selectors) {
      Locator locator = page.locator(selector).first();
      try {
        locator.waitFor(
            new Locator.WaitForOptions().setState(WaitForSelectorState.ATTACHED).setTimeout(1000));
      } catch (RuntimeException ignored) {
        // Optional widgets are not present on every screen.
      }
    }
  }

  private static String formatResultIds(List<? extends Rule> rules) {
    return rules.stream().map(Rule::getId).sorted(Comparator.naturalOrder()).toList().toString();
  }

  private static void saveDiagnostics(Page page, String scenarioName, AxeResults results)
      throws IOException {
    String safeName = scenarioName.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9-]+", "-");
    Path reportDir = Path.of("build", "reports", "accessibility");
    Files.createDirectories(reportDir);

    String prefix = Instant.now().toEpochMilli() + "-" + safeName;
    Path screenshot = reportDir.resolve(prefix + ".png");
    page.screenshot(new Page.ScreenshotOptions().setPath(screenshot).setFullPage(true));

    Path summary = reportDir.resolve(prefix + ".txt");
    String content =
        "Scenario: "
            + scenarioName
            + System.lineSeparator()
            + "Violations: "
            + formatResultIds(results.getViolations())
            + System.lineSeparator()
            + "Incomplete: "
            + formatResultIds(results.getIncomplete())
            + System.lineSeparator();
    Files.writeString(summary, content);
  }
}
