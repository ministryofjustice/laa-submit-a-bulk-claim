package uk.gov.justice.laa.bulkclaim.e2e.hooks;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import uk.gov.justice.laa.bulkclaim.e2e.state.TestContext;
import uk.gov.justice.laa.bulkclaim.e2e.utils.data.SubmissionPeriodHelper;
import uk.gov.justice.laa.bulkclaim.e2e.utils.db.DatabaseManager;

public class PlaywrightHooks {

  private static String resolveAppBaseUrl() {
    String fromProperty = System.getProperty("e2e.baseUrl");
    if (fromProperty != null && !fromProperty.isBlank()) {
      return fromProperty;
    }

    String fromE2eEnv = System.getenv("E2E_BASE_URL");
    if (fromE2eEnv != null && !fromE2eEnv.isBlank()) {
      return fromE2eEnv;
    }

    String fromUiEnv = System.getenv("UI_BASE_URL");
    if (fromUiEnv != null && !fromUiEnv.isBlank()) {
      return fromUiEnv;
    }

    return "http://localhost:8082";
  }

  @Before(order = 0)
  public void beforeScenario(Scenario scenario) {
    // ⭐ CRITICAL: Clear submission period cache for parallel execution isolation
    SubmissionPeriodHelper.clearThreadContext();
    
    TestContext contextState = TestContext.current();
    
    // Log scenario start with worker identification
    String workerId = Thread.currentThread().getName();
    System.out.println("\n[" + workerId + "] 🚀 Starting scenario: " + scenario.getName());
    System.out.println("[" + workerId + "] 📌 Tags: " + scenario.getSourceTagNames());
    
    boolean headless =
        Boolean.parseBoolean(System.getProperty("e2e.headless", System.getenv("E2E_HEADLESS") != null ? System.getenv("E2E_HEADLESS") : "true"));

    Playwright playwright = Playwright.create();
    contextState.playwright(playwright);

    Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
    contextState.browser(browser);

    // ⭐ Create worker-specific storage state (parity with TypeScript)
    // This prevents auth context bleeding between parallel scenarios
    Path globalStorageStatePath = resolveStorageStatePath();
    Path workerStorageStatePath = null;
    
    if (globalStorageStatePath != null && Files.exists(globalStorageStatePath)) {
      try {
        // Create worker-specific copy in temp directory
        String workerName = workerId.replaceAll("[^a-zA-Z0-9_-]", "_");
        workerStorageStatePath = Path.of(System.getProperty("java.io.tmpdir"), 
            "storageState-" + workerName + "-" + System.currentTimeMillis() + ".json");
        
        Files.copy(globalStorageStatePath, workerStorageStatePath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("[" + workerId + "] 🔐 Copied auth state to worker-specific path");
      } catch (Exception e) {
        System.err.println("[" + workerId + "] ⚠️ Failed to create worker storage state: " + e.getMessage());
        workerStorageStatePath = null;
      }
    }

    // Create browser context with optional storage state support.
    String appBaseUrl = resolveAppBaseUrl();
    Browser.NewContextOptions options = new Browser.NewContextOptions()
        .setAcceptDownloads(true)
        .setBaseURL(appBaseUrl);

    if (workerStorageStatePath != null && Files.exists(workerStorageStatePath)) {
      options.setStorageStatePath(workerStorageStatePath);
      System.out.println("[" + workerId + "] 📦 Loading worker-specific auth state");
    }

    BrowserContext context = browser.newContext(options);
    contextState.browserContext(context);
    
    Page page = context.newPage();
    contextState.page(page);
    
    // ⭐ Navigate to base URL to load auth state
    try {
      page.navigate(appBaseUrl);
      page.waitForLoadState();
      System.out.println("[" + workerId + "] ✅ Navigated to: " + appBaseUrl);
    } catch (Exception e) {
      System.out.println("[" + workerId + "] ⚠️ Navigation warning: " + e.getMessage());
    }
    
    System.out.println("[" + workerId + "] ✅ Browser context ready for scenario");
  }

  @After(order = 0)
  public void afterScenario(Scenario scenario) {
    String workerId = Thread.currentThread().getName();
    TestContext contextState = TestContext.current();
    Page page = contextState.page();
    try {
      if (scenario.isFailed() && page != null) {
        Path dir = Path.of("build", "reports", "cucumber", "failure-screenshots");
        Files.createDirectories(dir);
        Path screenshot = dir.resolve(System.currentTimeMillis() + "-" + sanitize(scenario.getName()) + ".png");
        page.screenshot(new Page.ScreenshotOptions().setPath(screenshot).setFullPage(true));
        byte[] screenshotBytes = Files.readAllBytes(screenshot);
        scenario.attach(screenshotBytes, "image/png", "Failure screenshot");
      }
    } catch (Exception ignored) {
      // Best-effort screenshot capture.
    }

    if (contextState.browserContext() != null) {
      contextState.browserContext().close();
    }
    if (contextState.browser() != null) {
      contextState.browser().close();
    }
    if (contextState.playwright() != null) {
      contextState.playwright().close();
    }
    
    // Database cleanup - clean up test submissions created during scenario
    try {
      java.util.Set<String> cleanupSubmissionIds = contextState.getCleanupSubmissionIds();
      if (cleanupSubmissionIds != null && !cleanupSubmissionIds.isEmpty()) {
        DatabaseManager.cleanupSubmissions(new java.util.ArrayList<>(cleanupSubmissionIds));
        System.out.println("[" + workerId + "] 🧹 Cleaned DB submissions: " + cleanupSubmissionIds);
      }
    } catch (Exception e) {
      System.err.println("[" + workerId + "] ❌ DB cleanup failed: " + e.getMessage());
      e.printStackTrace();
    }
    
    // ⭐ CRITICAL: Clear thread-local contexts to prevent bleeding in parallel execution
    TestContext.clearThread();
    SubmissionPeriodHelper.clearThreadContext();
    
    System.out.println("[" + workerId + "] ✅ Scenario cleanup complete");
  }

  private static String sanitize(String value) {
    return value.replaceAll("[^a-zA-Z0-9._-]", "_");
  }

  private static Path resolveStorageStatePath() {
    String fromProperty = System.getProperty("e2e.storageStatePath");
    if (fromProperty != null && !fromProperty.isBlank()) {
      return Path.of(fromProperty);
    }

    String fromEnv = System.getenv("E2E_STORAGE_STATE_PATH");
    if (fromEnv != null && !fromEnv.isBlank()) {
      return Path.of(fromEnv);
    }

    return Path.of("build", "e2e", "storageState.json");
  }
}

