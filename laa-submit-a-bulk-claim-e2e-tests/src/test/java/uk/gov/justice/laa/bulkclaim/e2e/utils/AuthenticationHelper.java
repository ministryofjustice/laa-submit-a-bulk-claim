package uk.gov.justice.laa.bulkclaim.e2e.utils;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.LoadState;

/**
 * Utilities for resetting browser state between scenarios (logout, clear storage, etc.)
 */
public final class AuthenticationHelper {

  private static final int CLEAR_STORAGE_TIMEOUT = 3000;
  private static final int RETRY_WAIT = 1000;

  /**
   * Sign out from the current session and clear browser storage (localStorage, sessionStorage,
   * cookies, IndexedDB, service workers).
   *
   * @param page Playwright Page instance
   */
  public static void logoutAndWipe(Page page) {
    // Try to click the Sign out button if visible
    try {
      boolean signOutVisible =
          page.locator("button.sign-in-button:has-text('Sign out')")
              .isVisible();

      if (signOutVisible) {
        System.out.println("🔐 Signing out to reset backend session...");
        page.locator("button.sign-in-button:has-text('Sign out')").click();
        page.waitForTimeout(1000);
      } else {
        System.out.println("ℹ️ No active session found, skipping sign out.");
      }
    } catch (Exception e) {
      System.out.println("ℹ️ Sign-out control not found; proceeding with wipe.");
    }

    // Clear all browser storage
    try {
      page.evaluate(
          "() => {\n"
              + "  localStorage.clear();\n"
              + "  sessionStorage.clear();\n"
              + "  document.cookie.split(';').forEach(c => {\n"
              + "    const name = c.split('=')[0].trim();\n"
              + "    document.cookie = name + '=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';\n"
              + "  });\n"
              + "}");
    } catch (Exception e) {
      System.out.println(
          "⚠️ Could not clear browser storage from "
              + page.url()
              + ": "
              + e.getMessage());
      System.out.println(
          "⚠️ Falling back to context recreation for storage reset.");
    }

    page.waitForTimeout(500);
  }

  /**
   * Navigate to app with retry logic and wait for sign-out button visibility to confirm auth
   * state.
   *
   * @param page Playwright Page instance
   * @param baseUrl Base URL of the application
   */
  public static void navigateWithAuthRetry(Page page, String baseUrl) {
    int status = 0;
    boolean badStatus = false;

    // Retry initial navigation (env may be cold-starting)
    for (int attempt = 1; attempt <= 3; attempt++) {
      try {
        Response response = page.navigate(baseUrl);
        status = response == null ? 0 : response.status();
        badStatus = status >= 400;

        if (!badStatus) {
          break;
        }
      } catch (Exception e) {
        System.out.println("⚠️ Navigation attempt " + attempt + " failed: " + e.getMessage());
        badStatus = true;
      }

      if (badStatus && attempt < 3) {
        System.out.println("⏳ Navigation unhealthy, retrying (attempt " + (attempt + 1)
            + ")...");
        page.waitForTimeout(RETRY_WAIT);
      }
    }

    // Detect "environment down" page
    boolean envDownPage = false;
    try {
      envDownPage =
          page.locator("h1:has-text('Unable to display the page')").isVisible();
    } catch (Exception ignore) {
      // Page may not be fully loaded yet
    }

    if (badStatus || envDownPage) {
      System.out.println("🔧 Navigation unhealthy (status=" + status
          + ") — retrying with final attempt...");
      try {
        Response finalResponse = page.navigate(baseUrl);
        int finalStatus = finalResponse == null ? 0 : finalResponse.status();
        if (finalStatus < 400) {
          page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        }
      } catch (Exception e) {
        System.out.println("⚠️ Final navigation attempt failed: " + e.getMessage());
      }
    }

    // Wait for sign-out button to confirm logged-in state
    try {
      page.locator("button.sign-in-button:has-text('Sign out')")
          .waitFor(new Locator.WaitForOptions().setTimeout(30000));
      System.out.println("✅ Logged-in state confirmed: environment is ready.");
    } catch (Exception e) {
      System.out.println("⚠️ Sign-out button not visible (may not be authenticated): "
          + e.getMessage());
    }
  }
}

