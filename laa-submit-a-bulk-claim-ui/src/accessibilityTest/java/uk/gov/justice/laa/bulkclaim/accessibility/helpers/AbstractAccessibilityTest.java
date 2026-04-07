package uk.gov.justice.laa.bulkclaim.accessibility.helpers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
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

  private static final WireMockServer WIREMOCK = new WireMockServer(0);

  static {
    WIREMOCK.start();
    registerWiremockStubs();
  }

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

  private static void registerWiremockStubs() {
    String wiremockBase = WIREMOCK.baseUrl();

    WIREMOCK.stubFor(
        get(urlPathEqualTo("/oidc/silas/.well-known/openid-configuration"))
            .willReturn(
                okJson(
                    template("wiremock/oidc/silas-openid-configuration.json")
                        .formatted(
                            wiremockBase,
                            wiremockBase,
                            wiremockBase,
                            wiremockBase,
                            wiremockBase))));

    WIREMOCK.stubFor(
        get(urlPathEqualTo("/oidc/moj/.well-known/openid-configuration"))
            .willReturn(
                okJson(
                    template("wiremock/oidc/moj-openid-configuration.json")
                        .formatted(wiremockBase, wiremockBase, wiremockBase, wiremockBase))));

    WIREMOCK.stubFor(
        get(urlPathEqualTo("/oidc/silas/jwks"))
            .willReturn(okJson(template("wiremock/oidc/jwks-empty.json"))));
    WIREMOCK.stubFor(
        get(urlPathEqualTo("/oidc/moj/jwks"))
            .willReturn(okJson(template("wiremock/oidc/jwks-empty.json"))));

    WIREMOCK.stubFor(
        post(urlPathEqualTo("/api/v1/bulk-submissions"))
            .atPriority(1)
            .withRequestBody(containing("forbidden-office.csv"))
            .willReturn(
                aResponse()
                    .withStatus(403)
                    .withHeader("Content-Type", "application/problem+json")
                    .withBody(
                        template("wiremock/claim-api/post-bulk-submissions-forbidden.json"))));

    WIREMOCK.stubFor(
        post(urlPathEqualTo("/api/v1/bulk-submissions"))
            .atPriority(5)
            .willReturn(
                okJson(
                    template("wiremock/claim-api/post-bulk-submissions.json")
                        .formatted(LEGAL_HELP.validSubmissionId(), BULK_SUBMISSION_ID))));

    WIREMOCK.stubFor(
        get(urlPathEqualTo("/api/v1/bulk-submissions/" + BULK_SUBMISSION_ID + "/summary"))
            .willReturn(okJson(template("wiremock/claim-api/get-bulk-submission-summary.json"))));

    WIREMOCK.stubFor(
        get(urlPathEqualTo("/api/v1/submissions"))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-submissions.json")
                        .formatted(
                            LEGAL_HELP.validSubmissionId(),
                            LEGAL_HELP.invalidSubmissionId(),
                            CRIME_LOWER.validSubmissionId(),
                            CRIME_LOWER.invalidSubmissionId(),
                            MEDIATION.validSubmissionId(),
                            MEDIATION.invalidSubmissionId()))));

    for (AreaScenario scenario : allAreas().toList()) {
      registerAreaStubs(scenario);
    }
  }

  private static void registerAreaStubs(AreaScenario scenario) {
    WIREMOCK.stubFor(
        get(urlPathEqualTo("/api/v1/submissions/" + scenario.validSubmissionId()))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-submission-valid.json")
                        .formatted(
                            scenario.validSubmissionId(),
                            scenario.apiAreaOfLaw(),
                            scenario.validClaimId()))));

    WIREMOCK.stubFor(
        get(urlPathEqualTo("/api/v1/submissions/" + scenario.invalidSubmissionId()))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-submission-invalid.json")
                        .formatted(
                            scenario.invalidSubmissionId(),
                            scenario.apiAreaOfLaw(),
                            scenario.validClaimId()))));

    WIREMOCK.stubFor(
        get(urlPathEqualTo("/api/v1/claims"))
            .withQueryParam("submission_id", equalTo(scenario.validSubmissionId()))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-claims-v1.json")
                        .formatted(
                            scenario.validClaimId(),
                            scenario.validSubmissionId(),
                            scenario.validClaimId()))));

    WIREMOCK.stubFor(
        get(urlPathEqualTo("/api/v1/claims"))
            .withQueryParam("submission_id", equalTo(scenario.invalidSubmissionId()))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-claims-v1.json")
                        .formatted(
                            scenario.validClaimId(),
                            scenario.invalidSubmissionId(),
                            scenario.validClaimId()))));

    WIREMOCK.stubFor(
        get(urlPathEqualTo("/api/v2/claims"))
            .withQueryParam("submission_id", equalTo(scenario.validSubmissionId()))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-claims-v2.json")
                        .formatted(
                            scenario.validClaimId(),
                            scenario.validSubmissionId(),
                            scenario.validClaimId()))));

    WIREMOCK.stubFor(
        get(urlPathEqualTo("/api/v2/claims"))
            .withQueryParam("submission_id", equalTo(scenario.invalidSubmissionId()))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-claims-v2.json")
                        .formatted(
                            scenario.validClaimId(),
                            scenario.invalidSubmissionId(),
                            scenario.validClaimId()))));

    WIREMOCK.stubFor(
        get(urlPathEqualTo(
                "/api/v1/submissions/"
                    + scenario.validSubmissionId()
                    + "/claims/"
                    + scenario.validClaimId()))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-claim-detail.json")
                        .formatted(
                            scenario.validClaimId(),
                            scenario.validSubmissionId(),
                            scenario.validClaimId()))));

    WIREMOCK.stubFor(
        get(urlPathEqualTo("/api/v1/validation-messages"))
            .withQueryParam("submission-id", equalTo(scenario.validSubmissionId()))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-validation-messages.json")
                        .formatted(scenario.validSubmissionId(), scenario.validClaimId()))));

    WIREMOCK.stubFor(
        get(urlPathEqualTo("/api/v1/validation-messages"))
            .withQueryParam("submission-id", equalTo(scenario.invalidSubmissionId()))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-validation-messages.json")
                        .formatted(scenario.invalidSubmissionId(), scenario.validClaimId()))));

    if (scenario.hasMatterStarts()) {
      WIREMOCK.stubFor(
          get(urlPathEqualTo(
                  "/api/v1/submissions/" + scenario.validSubmissionId() + "/matter-starts"))
              .willReturn(
                  okJson(
                      template("wiremock/claim-api/get-matter-starts.json")
                          .formatted(scenario.validSubmissionId()))));
    } else {
      WIREMOCK.stubFor(
          get(urlPathEqualTo(
                  "/api/v1/submissions/" + scenario.validSubmissionId() + "/matter-starts"))
              .willReturn(
                  okJson(
                      template("wiremock/claim-api/get-matter-starts-empty.json")
                          .formatted(scenario.validSubmissionId()))));
    }

    WIREMOCK.stubFor(
        get(urlPathEqualTo(
                "/api/v1/submissions/" + scenario.invalidSubmissionId() + "/matter-starts"))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-matter-starts-empty.json")
                        .formatted(scenario.invalidSubmissionId()))));
  }

  private static String template(String resourcePath) {
    try (InputStream stream =
        AbstractAccessibilityTest.class.getClassLoader().getResourceAsStream(resourcePath)) {
      if (stream == null) {
        throw new IllegalStateException("Resource not found: " + resourcePath);
      }
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException(
          "Failed to load accessibility test resource: " + resourcePath, e);
    }
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
