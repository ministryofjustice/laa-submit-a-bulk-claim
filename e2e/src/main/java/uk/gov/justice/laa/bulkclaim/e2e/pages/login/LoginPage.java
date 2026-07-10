package uk.gov.justice.laa.bulkclaim.e2e.pages.login;

import com.microsoft.playwright.Page;

public interface LoginPage {

  void navigate();

  void login();

  static LoginPage fromAuthMethod(Page page, String authMethod) {
    return switch (authMethod) {
      case "silas" -> new SilasLoginPage(page);
      case "mock" -> new MockOidcLoginPage(page);
      default -> throw new IllegalArgumentException("Unsupported auth method: " + authMethod);
    };
  }
}
