package uk.gov.justice.laa.bulkclaim.e2e.state;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class TestContext {

  private static final ThreadLocal<TestContext> CURRENT = ThreadLocal.withInitial(TestContext::new);

  private Browser browser;
  private BrowserContext browserContext;
  private Page page;
  private Playwright playwright;
  private Path generatedFile;
  private final Map<String, Object> bag = new HashMap<>();
  private final Set<String> cleanupSubmissionIds = new HashSet<>();

  public static TestContext current() {
    return CURRENT.get();
  }

  public static void clearThread() {
    CURRENT.remove();
  }

  public Browser browser() {
    return browser;
  }

  public void browser(Browser browser) {
    this.browser = browser;
  }

  public BrowserContext browserContext() {
    return browserContext;
  }

  public void browserContext(BrowserContext browserContext) {
    this.browserContext = browserContext;
  }

  public Page page() {
    return page;
  }

  public void page(Page page) {
    this.page = page;
  }

  public Playwright playwright() {
    return playwright;
  }

  public void playwright(Playwright playwright) {
    this.playwright = playwright;
  }

  public Path generatedFile() {
    return generatedFile;
  }

  public void generatedFile(Path generatedFile) {
    this.generatedFile = generatedFile;
  }

  public void put(String key, Object value) {
    bag.put(key, value);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(String key) {
    return (T) bag.get(key);
  }

  public Set<String> getCleanupSubmissionIds() {
    return cleanupSubmissionIds;
  }

  public void addCleanupSubmissionId(String submissionId) {
    cleanupSubmissionIds.add(submissionId);
  }
}

