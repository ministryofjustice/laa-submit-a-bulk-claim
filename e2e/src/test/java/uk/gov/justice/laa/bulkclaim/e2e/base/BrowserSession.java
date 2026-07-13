package uk.gov.justice.laa.bulkclaim.e2e.base;

import static com.microsoft.playwright.options.AriaRole.BUTTON;
import static com.microsoft.playwright.options.WaitForSelectorState.VISIBLE;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import uk.gov.justice.laa.bulkclaim.e2e.config.EnvConfig;
import uk.gov.justice.laa.bulkclaim.e2e.pages.login.LoginPage;

public class BrowserSession {
  private static Playwright playwright;
  private static Browser browser;
  private static BrowserContext context;
  private static boolean initialized = false;

  public static synchronized void initializeIfNeeded() {
    if (!initialized) {
      playwright = Playwright.create();
      browser =
          playwright
              .chromium()
              .launch(new BrowserType.LaunchOptions().setHeadless(EnvConfig.headless()));
      context = browser.newContext();

      try (var page = context.newPage()) {
        var login = LoginPage.fromAuthMethod(page, EnvConfig.authMethod());
        login.navigate();
        login.login();

        page.waitForSelector(
            "h1:has-text('Submit a bulk claim')",
            new Page.WaitForSelectorOptions().setState(VISIBLE));
      }

      // Register shutdown hook to ensure cleanup
      Runtime.getRuntime().addShutdownHook(new Thread(BrowserSession::cleanup));
    }

    initialized = true;
  }

  public static BrowserContext getContext() {
    initializeIfNeeded();
    return context;
  }

  public static synchronized void cleanup() {
    if (initialized) {
      try {
        Page signoutPage = context.newPage();
        signoutPage.navigate(EnvConfig.baseUrl());
        signoutPage.getByRole(BUTTON, new Page.GetByRoleOptions().setName("Sign out")).click();
        signoutPage.waitForLoadState();

      } finally {
        if (context != null) {
          context.close();
        }
        if (browser != null) {
          browser.close();
        }
        if (playwright != null) {
          playwright.close();
        }
        initialized = false;
      }
    }
  }
}
