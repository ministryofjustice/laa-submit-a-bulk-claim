package uk.gov.justice.laa.bulkclaim.e2e.pages.login;

import static com.microsoft.playwright.options.AriaRole.BUTTON;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import uk.gov.justice.laa.bulkclaim.e2e.config.EnvConfig;

public class MockOidcLoginPage implements LoginPage {

  private static final String MOCK_USERNAME = "provider.user@provider.com";
  private static final String MOCK_PASSWORD = "password";

  private final Page page;

  private final Locator usernameInput;
  private final Locator passwordInput;
  private final Locator submitButton;

  public MockOidcLoginPage(Page page) {
    this.page = page;

    this.usernameInput = page.locator("input#username");
    this.passwordInput = page.locator("input#password");
    this.submitButton = page.getByRole(BUTTON, new Page.GetByRoleOptions().setName("Sign in"));
  }

  public void navigate() {
    IO.println("[LOGIN] Navigating to mock OIDC login page");
    String url = EnvConfig.baseUrl();
    page.navigate(url);
  }

  public void login() {
    IO.println("[LOGIN] Starting login flow...");

    usernameInput.fill(MOCK_USERNAME);
    passwordInput.fill(MOCK_PASSWORD);
    submitButton.click();

    page.waitForSelector("h1:has-text('Submit a bulk claim')");

    IO.println("[LOGIN] Login successful!");
  }
}
