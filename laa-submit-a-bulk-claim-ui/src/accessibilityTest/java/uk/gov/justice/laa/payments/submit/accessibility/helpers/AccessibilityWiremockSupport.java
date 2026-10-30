package uk.gov.justice.laa.payments.submit.accessibility.helpers;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

final class AccessibilityWiremockSupport {

  private AccessibilityWiremockSupport() {}

  static WireMockServer createStartedServer(
      String bulkSubmissionId,
      AbstractAccessibilityTest.AreaScenario legalHelp,
      AbstractAccessibilityTest.AreaScenario crimeLower,
      AbstractAccessibilityTest.AreaScenario mediation) {
    WireMockServer wiremock = new WireMockServer(0);
    wiremock.start();
    registerWiremockStubs(wiremock, bulkSubmissionId, legalHelp, crimeLower, mediation);
    return wiremock;
  }

  private static void registerWiremockStubs(
      WireMockServer wiremock,
      String bulkSubmissionId,
      AbstractAccessibilityTest.AreaScenario legalHelp,
      AbstractAccessibilityTest.AreaScenario crimeLower,
      AbstractAccessibilityTest.AreaScenario mediation) {
    String wiremockBase = wiremock.baseUrl();

    wiremock.stubFor(
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

    wiremock.stubFor(
        get(urlPathEqualTo("/oidc/moj/.well-known/openid-configuration"))
            .willReturn(
                okJson(
                    template("wiremock/oidc/moj-openid-configuration.json")
                        .formatted(wiremockBase, wiremockBase, wiremockBase, wiremockBase))));

    wiremock.stubFor(
        get(urlPathEqualTo("/oidc/silas/jwks"))
            .willReturn(okJson(template("wiremock/oidc/jwks-empty.json"))));
    wiremock.stubFor(
        get(urlPathEqualTo("/oidc/moj/jwks"))
            .willReturn(okJson(template("wiremock/oidc/jwks-empty.json"))));

    wiremock.stubFor(
        post(urlPathEqualTo("/api/v1/bulk-submissions"))
            .atPriority(1)
            .withRequestBody(containing("forbidden-office.csv"))
            .willReturn(
                aResponse()
                    .withStatus(403)
                    .withHeader("Content-Type", "application/problem+json")
                    .withBody(
                        template("wiremock/claim-api/post-bulk-submissions-forbidden.json"))));

    wiremock.stubFor(
        post(urlPathEqualTo("/api/v1/bulk-submissions"))
            .atPriority(5)
            .willReturn(
                okJson(
                    template("wiremock/claim-api/post-bulk-submissions.json")
                        .formatted(legalHelp.validSubmissionId(), bulkSubmissionId))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v1/bulk-submissions/" + bulkSubmissionId + "/summary"))
            .willReturn(okJson(template("wiremock/claim-api/get-bulk-submission-summary.json"))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v1/submissions"))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-submissions.json")
                        .formatted(
                            legalHelp.validSubmissionId(),
                            legalHelp.invalidSubmissionId(),
                            crimeLower.validSubmissionId(),
                            crimeLower.invalidSubmissionId(),
                            mediation.validSubmissionId(),
                            mediation.invalidSubmissionId()))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v1/submissions"))
            .atPriority(2)
            .withQueryParam("offices", equalTo(AbstractAccessibilityTest.FULLY_SUBMITTED_OFFICE))
            .willReturn(okJson(fullySubmittedOfficeResponse())));

    for (AbstractAccessibilityTest.AreaScenario scenario :
        List.of(legalHelp, crimeLower, mediation)) {
      registerAreaStubs(wiremock, scenario);
    }
  }

  private static void registerAreaStubs(
      WireMockServer wiremock, AbstractAccessibilityTest.AreaScenario scenario) {
    wiremock.stubFor(
        get(urlPathEqualTo("/api/v1/submissions/" + scenario.validSubmissionId()))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-submission-valid.json")
                        .formatted(
                            scenario.validSubmissionId(),
                            scenario.apiAreaOfLaw(),
                            scenario.validClaimId()))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v1/submissions/" + scenario.invalidSubmissionId()))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-submission-invalid.json")
                        .formatted(
                            scenario.invalidSubmissionId(),
                            scenario.apiAreaOfLaw(),
                            scenario.validClaimId()))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v1/claims"))
            .withQueryParam("submission_id", equalTo(scenario.validSubmissionId()))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-claims-v1.json")
                        .formatted(
                            scenario.validClaimId(),
                            scenario.validSubmissionId(),
                            scenario.validClaimId()))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v1/claims"))
            .withQueryParam("submission_id", equalTo(scenario.invalidSubmissionId()))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-claims-v1.json")
                        .formatted(
                            scenario.validClaimId(),
                            scenario.invalidSubmissionId(),
                            scenario.validClaimId()))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v2/claims"))
            .withQueryParam("submission_id", equalTo(scenario.validSubmissionId()))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-claims-v2.json")
                        .formatted(
                            scenario.validClaimId(),
                            scenario.validSubmissionId(),
                            scenario.validClaimId()))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v2/claims"))
            .withQueryParam("submission_id", equalTo(scenario.invalidSubmissionId()))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-claims-v2.json")
                        .formatted(
                            scenario.validClaimId(),
                            scenario.invalidSubmissionId(),
                            scenario.validClaimId()))));

    wiremock.stubFor(
        get(urlPathEqualTo(
                "/api/v2/submissions/"
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

    wiremock.stubFor(
        get(urlPathEqualTo(
                "/api/v2/submissions/"
                    + scenario.invalidSubmissionId()
                    + "/claims/"
                    + scenario.validClaimId()))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-claim-detail.json")
                        .formatted(
                            scenario.validClaimId(),
                            scenario.invalidSubmissionId(),
                            scenario.validClaimId()))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v1/validation-messages"))
            .withQueryParam("submission-id", equalTo(scenario.validSubmissionId()))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-validation-messages.json")
                        .formatted(scenario.validSubmissionId(), scenario.validClaimId()))));

    wiremock.stubFor(
        get(urlPathEqualTo("/api/v1/validation-messages"))
            .withQueryParam("submission-id", equalTo(scenario.invalidSubmissionId()))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-validation-messages.json")
                        .formatted(scenario.invalidSubmissionId(), scenario.validClaimId()))));

    if (scenario.hasMatterStarts()) {
      wiremock.stubFor(
          get(urlPathEqualTo(
                  "/api/v1/submissions/" + scenario.validSubmissionId() + "/matter-starts"))
              .willReturn(
                  okJson(
                      template("wiremock/claim-api/get-matter-starts.json")
                          .formatted(scenario.validSubmissionId()))));
    } else {
      wiremock.stubFor(
          get(urlPathEqualTo(
                  "/api/v1/submissions/" + scenario.validSubmissionId() + "/matter-starts"))
              .willReturn(
                  okJson(
                      template("wiremock/claim-api/get-matter-starts-empty.json")
                          .formatted(scenario.validSubmissionId()))));
    }

    wiremock.stubFor(
        get(urlPathEqualTo(
                "/api/v1/submissions/" + scenario.invalidSubmissionId() + "/matter-starts"))
            .willReturn(
                okJson(
                    template("wiremock/claim-api/get-matter-starts-empty.json")
                        .formatted(scenario.invalidSubmissionId()))));
  }

  /** One submission for each of the twelve months the nil-submission period page considers. */
  private static String fullySubmittedOfficeResponse() {
    DateTimeFormatter periodFormat = DateTimeFormatter.ofPattern("MMM-uuuu", Locale.ENGLISH);
    YearMonth now = YearMonth.now();
    String content =
        IntStream.rangeClosed(1, 12)
            .mapToObj(
                monthsAgo ->
                    """
                    {
                      "submission_id": "%s",
                      "office_account_number": "%s",
                      "submission_period": "%s",
                      "is_nil_submission": false,
                      "number_of_claims": 1,
                      "area_of_law": "LEGAL HELP",
                      "status": "VALIDATION_SUCCEEDED",
                      "submitted": "2025-05-01T12:32:00Z"
                    }"""
                        .formatted(
                            java.util.UUID.randomUUID(),
                            AbstractAccessibilityTest.FULLY_SUBMITTED_OFFICE,
                            now.minusMonths(monthsAgo).format(periodFormat).toUpperCase()))
            .collect(Collectors.joining(","));
    return """
        {"total_pages": 1, "total_elements": 12, "number": 0, "size": 12, "content": [%s]}"""
        .formatted(content);
  }

  private static String template(String resourcePath) {
    try (InputStream stream =
        AccessibilityWiremockSupport.class.getClassLoader().getResourceAsStream(resourcePath)) {
      if (stream == null) {
        throw new IllegalStateException("Resource not found: " + resourcePath);
      }
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException(
          "Failed to load accessibility test resource: " + resourcePath, e);
    }
  }
}
