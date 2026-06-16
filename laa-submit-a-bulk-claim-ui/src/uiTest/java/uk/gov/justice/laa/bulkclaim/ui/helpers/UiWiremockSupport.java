package uk.gov.justice.laa.bulkclaim.ui.helpers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import uk.gov.justice.laa.bulkclaim.accessibility.helpers.AbstractAccessibilityTest.AreaScenario;

/**
 * Registers higher-priority WireMock stubs for UI flow tests that need richer claim data (e.g.
 * multi-claim sorting fixtures). Registers separate responses per page so that cross-page
 * sorting validation uses genuinely distinct data on each page.
 */
public final class UiWiremockSupport {

  private UiWiremockSupport() {}

  /**
   * Registers page-specific sorting stubs for the given scenario:
   * - page=0 returns claims with surnames A–K (page 1)
   * - page=1 returns claims with surnames N–W (page 2)
   * - Any other page falls back to empty / last page sentinel.
   */
  public static void registerSortingStubs(WireMockServer wiremock, AreaScenario scenario) {
    String claimId = scenario.validClaimId();
    String submissionId = scenario.validSubmissionId();

    registerSortAwarePageOneStubs(wiremock, claimId, submissionId);

    // Page 0 stub (first page)
    wiremock.stubFor(
        get(urlPathEqualTo("/api/v2/claims"))
            .atPriority(3)
            .withQueryParam("submission_id", equalTo(submissionId))
            .withQueryParam("page", equalTo("0"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(buildBody("wiremock/claim-api/get-claims-sorting.json", claimId, submissionId))));

    // Page 1 stub (second page)
    wiremock.stubFor(
        get(urlPathEqualTo("/api/v2/claims"))
            .atPriority(3)
            .withQueryParam("submission_id", equalTo(submissionId))
            .withQueryParam("page", equalTo("1"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        buildBody("wiremock/claim-api/get-claims-sorting-page2.json", claimId,
                            submissionId))));

    // Page 2 stub (third page)
    wiremock.stubFor(
        get(urlPathEqualTo("/api/v2/claims"))
            .atPriority(3)
            .withQueryParam("submission_id", equalTo(submissionId))
            .withQueryParam("page", equalTo("2"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        buildBody("wiremock/claim-api/get-claims-sorting-page3.json", claimId,
                            submissionId))));

    // Fallback for out-of-range pages
    wiremock.stubFor(
        get(urlPathEqualTo("/api/v2/claims"))
            .atPriority(20)
            .withQueryParam("submission_id", equalTo(submissionId))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        "{\"content\":[],\"total_pages\":3,\"total_elements\":30,\"number\":3,\"size\":10}")));
  }

  /** Registers accepted-submission export stubs for the given scenario. */
  public static void registerExportStubs(WireMockServer wiremock, AreaScenario scenario) {
    String areaOfLawPath = scenario.areaOfLaw().toLowerCase().replace(" ", "-");

    wiremock.stubFor(
        get(urlPathEqualTo("/exports/submission-claims-" + areaOfLawPath))
            .atPriority(1)
            .withQueryParam("submission-id", equalTo(scenario.validSubmissionId()))
            .withQueryParam("office", equalTo("0P322F"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "text/csv")
                    .withHeader(
                        "Content-Disposition",
                        "attachment; filename=\""
                            + exportFilenameFor(scenario)
                            + "\"")
                    .withBody(exportCsvFor(scenario))));
  }

  /** Registers search-result stubs for deterministic UI search tests (no upload setup required). */
  public static void registerSearchStubs(WireMockServer wiremock) {
    wiremock.stubFor(
        get(urlPathEqualTo("/api/v1/submissions"))
            .atPriority(1)
            .withQueryParam("submission_period", equalTo("MAY-2025"))
            .withQueryParam("area_of_law", containing("LEGAL"))
            .withQueryParam("submission_statuses", equalTo("VALIDATION_FAILED"))
            .withQueryParam("offices", containing("0P322F"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(loadTemplate("wiremock/claim-api/get-submissions-search-all-fields.json"))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v1/submissions"))
            .atPriority(1)
            .withQueryParam("submission_period", equalTo("MAY-2025"))
            .withQueryParam("submission_statuses", equalTo("VALIDATION_SUCCEEDED"))
            .withQueryParam("offices", containing("0P322F"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(loadTemplate("wiremock/claim-api/get-submissions-search-period-only.json"))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v1/submissions"))
            .atPriority(1)
            .withQueryParam("area_of_law", containing("LEGAL"))
            .withQueryParam("submission_statuses", equalTo("VALIDATION_SUCCEEDED"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        loadTemplate("wiremock/claim-api/get-submissions-search-area-legal-help-only.json"))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v1/submissions"))
            .atPriority(1)
            .withQueryParam("area_of_law", containing("MEDIATION"))
            .withQueryParam("submission_statuses", equalTo("VALIDATION_SUCCEEDED"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        loadTemplate("wiremock/claim-api/get-submissions-search-area-mediation-only.json"))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v1/submissions"))
            .atPriority(1)
            .withQueryParam("area_of_law", containing("CRIME"))
            .withQueryParam("submission_statuses", equalTo("VALIDATION_SUCCEEDED"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(loadTemplate("wiremock/claim-api/get-submissions-search-area-only.json"))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v1/submissions"))
            .atPriority(1)
            .withQueryParam("offices", equalTo("2Q779P"))
            .withQueryParam("submission_statuses", equalTo("VALIDATION_SUCCEEDED"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(loadTemplate("wiremock/claim-api/get-submissions-search-office-only.json"))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v1/submissions"))
            .atPriority(2)
            .withQueryParam("submission_period", equalTo("APR-2025"))
            .withQueryParam("submission_statuses", equalTo("VALIDATION_SUCCEEDED"))
            .withQueryParam("offices", containing("0P322F"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(loadTemplate("wiremock/claim-api/get-submissions-search-no-results.json"))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v1/submissions"))
            .atPriority(4)
            .withQueryParam("submission_statuses", equalTo("VALIDATION_FAILED"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(loadTemplate("wiremock/claim-api/get-submissions-search-status-failed.json"))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v1/submissions"))
            .atPriority(3)
            .withQueryParam("submission_statuses", equalTo("VALIDATION_SUCCEEDED"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(loadTemplate("wiremock/claim-api/get-submissions-search-default.json"))));
  }

  private static void registerSortAwarePageOneStubs(
      WireMockServer wiremock, String claimId, String submissionId) {
    wiremock.stubFor(
        get(urlPathEqualTo("/api/v2/claims"))
            .atPriority(1)
            .withQueryParam("submission_id", equalTo(submissionId))
            .withQueryParam("page", equalTo("0"))
            .withQueryParam("sort", equalTo("client_surname,asc"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        buildBody(
                            "wiremock/claim-api/get-claims-sorting-client-surname-asc-page1.json",
                            claimId,
                            submissionId))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v2/claims"))
            .atPriority(1)
            .withQueryParam("submission_id", equalTo(submissionId))
            .withQueryParam("page", equalTo("1"))
            .withQueryParam("sort", equalTo("client_surname,asc"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        buildBody(
                            "wiremock/claim-api/get-claims-sorting-client-surname-asc-page2.json",
                            claimId,
                            submissionId))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v2/claims"))
            .atPriority(1)
            .withQueryParam("submission_id", equalTo(submissionId))
            .withQueryParam("page", equalTo("2"))
            .withQueryParam("sort", equalTo("client_surname,asc"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        buildBody(
                            "wiremock/claim-api/get-claims-sorting-client-surname-asc-page3.json",
                            claimId,
                            submissionId))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v2/claims"))
            .atPriority(1)
            .withQueryParam("submission_id", equalTo(submissionId))
            .withQueryParam("page", equalTo("0"))
            .withQueryParam("sort", equalTo("total_amount,desc"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        buildBody(
                            "wiremock/claim-api/get-claims-sorting-total-amount-desc-page1.json",
                            claimId,
                            submissionId))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v2/claims"))
            .atPriority(1)
            .withQueryParam("submission_id", equalTo(submissionId))
            .withQueryParam("page", equalTo("1"))
            .withQueryParam("sort", equalTo("total_amount,desc"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        buildBody(
                            "wiremock/claim-api/get-claims-sorting-total-amount-desc-page2.json",
                            claimId,
                            submissionId))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v2/claims"))
            .atPriority(1)
            .withQueryParam("submission_id", equalTo(submissionId))
            .withQueryParam("page", equalTo("2"))
            .withQueryParam("sort", equalTo("total_amount,desc"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        buildBody(
                            "wiremock/claim-api/get-claims-sorting-total-amount-desc-page3.json",
                            claimId,
                            submissionId))));
  }

  private static String buildBody(String resource, String claimId, String submissionId) {
    // Each claim entry in the fixture needs 3 placeholders: id, submission_id, fee_calc claim_id
    Object[] args = new Object[30];
    for (int i = 0; i < 10; i++) {
      args[i * 3] = UUID.randomUUID().toString();
      args[i * 3 + 1] = submissionId;
      args[i * 3 + 2] = claimId;
    }
    return loadTemplate(resource).formatted(args);
  }

  private static String loadTemplate(String resourcePath) {
    try (InputStream stream =
        UiWiremockSupport.class.getClassLoader().getResourceAsStream(resourcePath)) {
      if (stream == null) {
        throw new IllegalStateException("Resource not found: " + resourcePath);
      }
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load UI test resource: " + resourcePath, e);
    }
  }

  private static String exportFilenameFor(AreaScenario scenario) {
    return "submission-claims-%s-%s-0P322F-2025-05-01.csv"
        .formatted(
            scenario.areaOfLaw().toLowerCase().replace(" ", "-"), scenario.validSubmissionId());
  }

  private static String exportCsvFor(AreaScenario scenario) {
    return switch (scenario.areaOfLaw()) {
      case "Legal help" ->
          "Providers LAA Office Number,Submission Month,Area of Law,Legal Help Submission Reference,Fee Code\n"
              + "0P322F,MAY-2025,LEGAL HELP,LH-REF,PROL1\n";
      case "Mediation" ->
          "Providers LAA Office Number,Submission Month,Area of Law,Mediation Submission Reference,Fee Code\n"
              + "0P322F,MAY-2025,MEDIATION,MED-REF,MED1\n";
      case "Crime lower" ->
          "Providers LAA Office Number,Submission Month,Area of Law,Crime Lower Schedule Number,Fee Code\n"
              + "0P322F,MAY-2025,CRIME LOWER,CL-REF,CRIM1\n";
      default -> throw new IllegalArgumentException("Unsupported export area of law: " + scenario.areaOfLaw());
    };
  }
}
