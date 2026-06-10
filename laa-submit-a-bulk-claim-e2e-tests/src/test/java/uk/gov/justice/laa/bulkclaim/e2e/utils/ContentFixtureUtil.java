package uk.gov.justice.laa.bulkclaim.e2e.utils;

import com.microsoft.playwright.Page;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ContentFixtureUtil {

  private ContentFixtureUtil() {}

  public static void assertMainContentMatchesFixture(Page page, Path fixturePath) {
    try {
      String expected = Files.readString(fixturePath);
      page.locator("main#main-content").waitFor();
      String actual = (String) page.locator("main#main-content").evaluate("node => node.outerHTML");

      String normalizedActual = normalizeHtml(actual);
      String normalizedExpected = normalizeHtml(expected);

      if (!normalizedActual.equals(normalizedExpected)) {
        throw new AssertionError("Page content did not match fixture: " + fixturePath);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Unable to read content fixture: " + fixturePath, e);
    }
  }

  public static Path resolveFixture(Path workspaceRoot, String fixtureName) {
    return workspaceRoot
        .resolve("bulk-submission-and-fee-scheme-tests-")
        .resolve("tests/data/content_div")
        .resolve(fixtureName)
        .normalize();
  }

  private static String normalizeHtml(String html) {
    String normalized = html
        .replaceAll("(<input\\b[^>]*name=[\"']?_csrf[\"'][^>]*?)\\s+value=\"[^\"]*\"", "$1")
        .replaceAll("data-max-date=\"[^\"]*\"", "")
        .replaceAll("data-min-date=\"[^\"]*\"", "")
        .replaceAll("\\s*data-testid=\"[^\"]*\"", "")
        .replaceAll("\\s*style=\"display:\\s*(?:none|block);?\"", "")
        .replaceAll("aria-disabled=\"true\"", "")
        .replaceAll("(<table\\b[^>]*class=[\"'][^\"']*moj-datepicker__calendar[^\"']*[\"'][^>]*?)\\s+role=\"(?:grid|application)\"", "$1")
        .replaceAll("(<h2\\b[^>]*class=[\"'][^\"']*moj-js-datepicker-month-year[^\"']*[\"'][^>]*>)([^<]*)(</h2>)", "$1$3")
        .replaceAll("(<span\\b[^>]*class=[\"'][^\"']*govuk-visually-hidden[^\"']*[\"'][^>]*>)(Excluded date,[^<]*)(</span>)", "$1$3")
        .replaceAll("(<span\\b[^>]*class=[\"'][^\"']*govuk-visually-hidden[^\"']*[\"'][^>]*>)([A-Za-z]+ \\d{1,2} [A-Za-z]+ \\d{4})(</span>)", "$1$3")
        .replaceAll("(<table\\b[^>]*class=[\"'][^\"']*moj-datepicker__calendar[^\"']*[\"'][^>]*>\\s*<thead>[\\s\\S]*?</thead>)[\\s\\S]*?(</table>)", "$1$2")
        .replaceAll("<input[^>]*name=\"_csrf\"[^>]*value=\"[^\"]*\"[^>]*>", "<input type=\"hidden\" name=\"_csrf\" value=\"CSRF_TOKEN\">")
        .replaceAll("<select[^>]*id=\"submission-period-select\"[^>]*>[\\s\\S]*?</select>", "<select id=\"submission-period-select\">DYNAMIC_OPTIONS</select>")
        .replace("\r\n", "\n");

    String[] lines = normalized.split("\n");
    StringBuilder sb = new StringBuilder();
    for (String line : lines) {
      String trimmed = line.trim();
      if (!trimmed.isEmpty()) {
        sb.append(trimmed);
      }
    }
    return sb.toString();
  }
}

