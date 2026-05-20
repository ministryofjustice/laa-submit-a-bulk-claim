package uk.gov.justice.laa.bulkclaim.e2e.utils.scripts;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.nio.file.Files;
import java.nio.file.Path;
import uk.gov.justice.laa.bulkclaim.e2e.utils.TotpUtil;

/**
 * Equivalent to TS `npm run login`: performs auth flow and writes Playwright storage state JSON.
 */
public final class CreateStorageState {

  public static void main(String[] args) throws Exception {
    String baseUrl = envOrDefault("E2E_BASE_URL", "http://localhost:8080");
    String storagePathRaw = envOrDefault("E2E_STORAGE_STATE_PATH", "build/e2e/storageState.json");
    Path storagePath = Path.of(storagePathRaw);
    Files.createDirectories(storagePath.getParent());

    boolean headless = Boolean.parseBoolean(envOrDefault("E2E_HEADLESS", "false"));
    String authMode = envOrDefault("E2E_AUTH_MODE", "aad").toLowerCase();

    try (Playwright playwright = Playwright.create()) {
      Browser browser =
          playwright
              .chromium()
              .launch(new BrowserType.LaunchOptions().setHeadless(headless));

      var context = browser.newContext();
      Page page = context.newPage();

      if ("mock".equals(authMode)) {
        loginMockOidc(page, baseUrl);
      } else {
        loginAad(page, baseUrl);
      }

      context.storageState(new com.microsoft.playwright.BrowserContext.StorageStateOptions().setPath(storagePath));
      browser.close();
    }

    System.out.println("✅ Auth state saved to " + storagePath.toAbsolutePath());
  }

  private static void loginAad(Page page, String baseUrl) {
    String username = requiredEnv("USERNAME");
    String password = requiredEnv("PASSWORD");
    String mfaSecret = requiredEnv("MFA_SECRET");

    page.navigate(baseUrl);

    page.locator("input[name='loginfmt']").waitFor();
    page.locator("input[name='loginfmt']").fill(username);
    page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Next")).click();

    page.getByPlaceholder("Password").waitFor();
    page.getByPlaceholder("Password").fill(password);
    page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign in")).click();

    page.locator("input[name='otc']").waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setTimeout(120000));
    enterOtpWithRetry(page, mfaSecret);

    page.waitForSelector("h1:has-text('Submit a bulk claim')", new Page.WaitForSelectorOptions().setTimeout(120000));
  }

  private static void loginMockOidc(Page page, String baseUrl) {
    page.navigate(baseUrl);

    page.locator("input[name='username']").waitFor();
    page.locator("input[name='username']").fill(envOrDefault("MOCK_USERNAME", "provider.user@provider.com"));
    page.locator("input[name='password']").fill(envOrDefault("MOCK_PASSWORD", "password"));
    page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Sign in")).click();

    page.waitForSelector("h1:has-text('Submit a bulk claim')", new Page.WaitForSelectorOptions().setTimeout(60000));
  }

  private static void enterOtpWithRetry(Page page, String mfaSecret) {
    var otcInput = page.locator("input[name='otc']");
    var verifyButton = page.locator("input[id='idSubmit_SAOTCC_Continue']");
    var errorBanner = page.locator("#idSpan_SAOTCC_Error_OTC");

    int attempts = 5;
    for (int i = 1; i <= attempts; i++) {
      String code = TotpUtil.generateCode(mfaSecret);
      otcInput.fill(code);
      verifyButton.click();

      try {
        otcInput.waitFor(new com.microsoft.playwright.Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN).setTimeout(10000));
        return;
      } catch (Exception ignored) {
        boolean hasError = false;
        try {
          hasError = errorBanner.isVisible();
        } catch (Exception ignore) {
          // ignore
        }

        boolean stillOnOtp = page.url().contains("login.microsoftonline");
        if (!hasError && !stillOnOtp) {
          return;
        }

        if (i == attempts) {
          throw new IllegalStateException("MFA verification failed after " + attempts + " attempts");
        }

        page.waitForTimeout(1500);
      }
    }
  }

  private static String requiredEnv(String name) {
    String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Missing required environment variable: " + name);
    }
    return value;
  }

  private static String envOrDefault(String name, String defaultValue) {
    String value = System.getenv(name);
    return value == null || value.isBlank() ? defaultValue : value;
  }
}

