package uk.gov.justice.laa.bulkclaim.accessibility.helpers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.justice.laa.bulkclaim.accessibility.config.TestOidcUserConfig;
import uk.gov.justice.laa.bulkclaim.accessibility.config.TokenProviderStubConfig;
import uk.gov.justice.laa.bulkclaim.service.VirusCheckService;
import uk.gov.justice.laa.bulkclaim.validation.BulkImportFileValidator;
import uk.gov.justice.laa.bulkclaim.validation.BulkImportFileVirusValidator;

@ActiveProfiles({"test", "accessibility"})
@Import({TestOidcUserConfig.class, TokenProviderStubConfig.class})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(Lifecycle.PER_CLASS)
public abstract class AbstractAccessibilityTest {

  protected static final String BULK_SUBMISSION_ID = "86d8c0df-9f4f-4688-acdc-98f6b4a16ed1";

  protected static final AreaScenario LEGAL_HELP =
      new AreaScenario(
          "Legal help",
          "LH",
          "945dfedc-ad7f-4af7-b20c-06082ffa3cb3",
          "6ed32748-64af-4d95-94ea-a78b6397033c",
          "0a5e6578-8f8f-4f57-b4a0-830d11f4f7aa",
          "LEGAL HELP",
          true);

  protected static final AreaScenario CRIME_LOWER =
      new AreaScenario(
          "Crime lower",
          "CL",
          "f72ce9eb-8b35-407a-b49e-5a6f4f76dc4a",
          "9d2fbf18-4d41-489d-94f4-675347cb88a0",
          "4f9b7cf0-41cf-4fba-af3f-fc56cf2f7354",
          "CRIME LOWER",
          false);

  protected static final AreaScenario MEDIATION =
      new AreaScenario(
          "Mediation",
          "M",
          "035755f9-ba9e-475d-8920-1518e5a7861f",
          "f95fb194-f802-4e57-aea8-a8de143eb53f",
          "0a88f0ee-dd5b-40b3-a2a0-4857abcebe56",
          "MEDIATION",
          true);

  private static final WireMockServer WIREMOCK =
      AccessibilityWiremockSupport.createStartedServer(
          BULK_SUBMISSION_ID, LEGAL_HELP, CRIME_LOWER, MEDIATION);

  @MockitoBean protected VirusCheckService virusCheckService;
  @MockitoBean protected BulkImportFileValidator bulkImportFileValidator;
  @MockitoBean protected BulkImportFileVirusValidator bulkImportFileVirusValidator;

  @LocalServerPort protected int port;

  protected Playwright playwright;
  protected Browser browser;
  protected BrowserContext context;
  protected Page page;

  @DynamicPropertySource
  static void registerProperties(DynamicPropertyRegistry registry) {
    registry.add("CLAIM_API_URL", WIREMOCK::baseUrl);
    registry.add("CLAIMS_API_ACCESS_TOKEN", () -> "dummy-token");
    registry.add("REDIS_HOST", () -> "redis://localhost:6379");
    registry.add("spring.session.store-type", () -> "none");
    registry.add("UPLOAD_MAX_FILE_SIZE", () -> "10MB");
    registry.add("SERVER_MAX_FILE_SIZE", () -> "50MB");
    registry.add("CANONICAL_BASE_URL", () -> "http://localhost:8080");
    registry.add("SDS_API_URL", () -> "http://localhost:8099");
    registry.add("SILAS_CLIENT_ID", () -> "test-client-id");
    registry.add("SILAS_CLIENT_SECRET", () -> "test-client-secret");
    registry.add("SILAS_SCOPE", () -> "openid,profile");
    registry.add("SILAS_TENANT_ID", () -> "test-tenant");
    registry.add("AUTH_CLIENT_ID", () -> "test-auth-client-id");
    registry.add("AUTH_CLIENT_SECRET", () -> "test-auth-client-secret");
    registry.add("AUTH_SCOPE", () -> "api://test/.default");
    registry.add("AUTH_TENANT_ID", () -> "test-tenant");
    registry.add(
        "spring.security.oauth2.client.provider.silas-identity.issuer-uri",
        () -> WIREMOCK.baseUrl() + "/oidc/silas");
    registry.add(
        "spring.security.oauth2.client.provider.moj-identity.issuer-uri",
        () -> WIREMOCK.baseUrl() + "/oidc/moj");
  }

  @BeforeAll
  void setUpPlaywright() {
    boolean headless =
        Boolean.parseBoolean(
            System.getProperty(
                "accessibility.headless",
                System.getenv().getOrDefault("ACCESSIBILITY_HEADLESS", "true")));
    playwright = Playwright.create();
    browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
  }

  @BeforeEach
  void createBrowserContext() {
    doNothing().when(virusCheckService).checkVirus(any());
    doNothing().when(bulkImportFileValidator).validate(any(), any());
    doNothing().when(bulkImportFileVirusValidator).validate(any(), any());
    context = browser.newContext();
    page = context.newPage();
  }

  @AfterEach
  void closeContext() {
    if (context != null) {
      context.close();
    }
  }

  @AfterAll
  void tearDownPlaywright() {
    if (browser != null) {
      browser.close();
    }
    if (playwright != null) {
      playwright.close();
    }
  }

  protected String appUrl(String path) {
    return "http://localhost:" + port + path;
  }

  protected void openSubmissionDetail(AreaScenario scenario) {
    page.navigate(appUrl("/view-submission-detail?submissionId=" + scenario.validSubmissionId()));
  }

  protected void openInvalidSubmissionDetail(AreaScenario scenario) {
    page.navigate(appUrl("/view-submission-detail?submissionId=" + scenario.invalidSubmissionId()));
  }

  protected static String areaScenarioName(
      String pageName, String areaOfLawAbbr, String scenarioSuffix) {
    if (scenarioSuffix == null || scenarioSuffix.isBlank()) {
      return pageName + "-" + areaOfLawAbbr;
    }
    return pageName + "-" + areaOfLawAbbr + "-" + scenarioSuffix;
  }

  protected Path writeFile(String fileName, String content) throws IOException {
    Path file = Path.of("build", "tmp", "accessibility", fileName);
    Files.createDirectories(file.getParent());
    Files.writeString(file, content);
    return file;
  }

  protected static Stream<AreaScenario> allAreas() {
    return Stream.of(LEGAL_HELP, CRIME_LOWER, MEDIATION);
  }

  protected static Stream<AreaScenario> costWarningAreas() {
    return Stream.of(LEGAL_HELP, CRIME_LOWER);
  }

  protected static Stream<AreaScenario> matterStartAreas() {
    return Stream.of(LEGAL_HELP, MEDIATION);
  }

  protected static Stream<Arguments> allAreaArguments() {
    return toArguments(allAreas());
  }

  protected static Stream<Arguments> costWarningAreaArguments() {
    return toArguments(costWarningAreas());
  }

  protected static Stream<Arguments> matterStartAreaArguments() {
    return toArguments(matterStartAreas());
  }

  protected static Stream<Arguments> legalHelpArguments() {
    return toArguments(Stream.of(LEGAL_HELP));
  }

  private static Stream<Arguments> toArguments(Stream<AreaScenario> scenarios) {
    return scenarios.map(scenario -> Arguments.of(scenario.areaOfLawAbbr(), scenario));
  }

  protected record AreaScenario(
      String areaOfLaw,
      String areaOfLawAbbr,
      String validSubmissionId,
      String validClaimId,
      String invalidSubmissionId,
      String apiAreaOfLaw,
      boolean hasMatterStarts) {}
}
