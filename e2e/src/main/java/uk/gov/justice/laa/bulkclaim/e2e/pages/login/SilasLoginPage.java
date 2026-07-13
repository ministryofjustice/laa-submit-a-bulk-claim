package uk.gov.justice.laa.bulkclaim.e2e.pages.login;

import com.microsoft.playwright.Page;
import uk.gov.justice.laa.bulkclaim.e2e.config.EnvConfig;
import uk.gov.justice.laa.bulkclaim.e2e.utils.OneTimePasswordUtils;

public class SilasLoginPage implements LoginPage {
  private final Page page;

  private final String userField = "input[name='loginfmt'], input[type='email']";
  private final String nextButton =
      "input[type='submit'], button[type='submit'], button:has-text('Next'), button:has-text('Continue'),"
          + " button:has-text('Sign in')";
  private final String passwdField = "input[name='passwd']";
  private final String signInButton = "input[type='submit'], button:has-text('Sign in')";
  private final String otcInput = "input[name='otc']";
  private final String verifyButton = "input[id='idSubmit_SAOTCC_Continue']";

  public SilasLoginPage(Page page) {
    this.page = page;
  }

  public void navigate() {
    IO.println("[LOGIN] Navigating to silas login page");
    String url = EnvConfig.baseUrl();
    page.navigate(url);
  }

  public void login() {
    IO.println("[LOGIN] Starting login flow...");

    page.waitForSelector(userField);
    page.fill(userField, EnvConfig.silasUsername());
    page.click(nextButton);

    page.waitForSelector(passwdField);
    page.fill(passwdField, EnvConfig.silasPassword());
    page.click(signInButton);

    handleMfa(EnvConfig.silasMfaSecret());

    page.waitForSelector("h1:has-text('Submit a bulk claim')");

    IO.println("[LOGIN] Login successful!");
  }

  private void handleMfa(String secret) {
    IO.println("[LOGIN] Performing MFA step...");
    page.waitForSelector(otcInput);

    String code = OneTimePasswordUtils.generateTotp(secret);
    IO.println("[LOGIN] Generated MFA code securely.");

    page.fill(otcInput, code);
    page.click(verifyButton);
  }
}
