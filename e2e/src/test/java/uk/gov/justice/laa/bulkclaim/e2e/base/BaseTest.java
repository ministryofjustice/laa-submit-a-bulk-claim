package uk.gov.justice.laa.bulkclaim.e2e.base;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.util.regex.Pattern.compile;

import com.microsoft.playwright.Page;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.justice.laa.bulkclaim.e2e.config.EnvConfig;
import uk.gov.justice.laa.bulkclaim.e2e.persistence.DatabaseQueryExecutor;

/**
 * The {@code BaseTest} class provides a foundation for UI tests using the Playwright library. It
 * handles common setup and cleanup tasks, such as initializing a new browser page and navigating to
 * the application's base URL before each test, and ensuring the page is closed after the test is
 * executed.
 *
 * <p>Subclasses of {@code BaseTest} inherit this functionality to streamline test development,
 * focusing only on the test logic rather than boilerplate setup and teardown.
 *
 * <p>This class is abstract and not meant to be instantiated directly. It is designed to be
 * extended by specific test classes that implement particular test scenarios.
 *
 * <p>Features: - Automatically initializes a browser page and navigates to the base URL before each
 * test. - Safely closes the browser page after each test execution. - Leverages the {@code
 * BrowserSession} and {@code EnvConfig} classes for session management and configuration.
 *
 * <p>Structure: - {@code setup()}: A method annotated with {@code @BeforeEach} to set up the
 * testing environment. It creates a new browser page and navigates to the configured base URL. -
 * {@code tearDown()}: A method annotated with {@code @AfterEach} to ensure that resources are
 * properly released by closing the browser page after the test completes.
 */
public abstract class BaseTest {

  protected DatabaseQueryExecutor dqe;
  protected Page page;

  @BeforeEach
  public void setup() {
    try {
      dqe = new DatabaseQueryExecutor();
      dqe.cleanAll();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to seed database", e);
    }

    page = BrowserSession.getContext().newPage();
    page.navigate(EnvConfig.baseUrl());
  }

  @AfterEach
  public void tearDown() {
    if (page != null) {
      try {
        page.close();
      } catch (Exception ignored) {
      }
    }
    if (dqe != null) {
      try {
        dqe.close();
      } catch (Exception ignored) {
      }
    }
  }

  protected void assertUrlEndsWith(String expectedUrl) {
    assertThat(page).hasURL(compile(".*%s$".formatted(expectedUrl)));
  }
}
