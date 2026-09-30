package uk.gov.justice.laa.payments.submit;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.matchingrules.RegexMatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import uk.gov.justice.laa.payments.submit.client.ExportDataClaimsRestClient;
import uk.gov.justice.laa.payments.submit.config.ClaimsApiPactTestConfig;

/**
 * For this PactTest, it spins up a MockWebServer which is used to act as the API we're testing
 * against (in this case the claims API). After all the tests have run, a pact is generated based on
 * all the passing tests. This pact will be published to the Pact Broker server. The Claims API will
 * then verify itself against the generated pact to ensure it remains compatible with it's
 * consumers.
 *
 * <p>For the various {@link Pact} annotations, a scenario is created. There are multiple parts of a
 * {@link RequestResponsePact}:
 *
 * <ul>
 *   <li>Given: This explains the state of what the Claims API should be in when expecting this
 *       request. For example, if "a claim exists", then the API should make sure it has a Claim to
 *       be used for the request. Given values can be reused across multiple scenarios.
 *   <li>Upon Receiving: This value details the scenario we are testing.
 *   <li>Match Path: The path we wish to match against for the contract.
 *   <li>Match Header: The header we wish to match against (authorization key).
 *   <li>Method: The HTTP method.
 * </ul>
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"app.claims-api.url=http://localhost:1234"})
@PactConsumerTest
@PactTestFor(providerName = AbstractPactTest.PROVIDER)
@MockServerConfig(port = "1234") // Same as Claims API URL port
@Import(ClaimsApiPactTestConfig.class)
@DisplayName("GET: /exports/submission-claims-{area-of-law} PACT tests")
public final class GetExportSubmissionPactTest extends AbstractPactTest {

  private static final String EXAMPLE_CSV_BODY = "Client name, UFN\n";
  private static final String NON_EMPTY_BODY_REGEX = "(?s).+";

  @Autowired ExportDataClaimsRestClient exportDataClaimsRestClient;

  @Pact(consumer = CONSUMER)
  public RequestResponsePact getLegalHelpSubmission200(PactDslWithProvider builder) {
    // Defines expected 200 response for existing submission
    return requireNonEmptyBody(
        builder
            .given("no data exists for the export")
            .uponReceiving("a request to export a legal help submission")
            .matchPath("/exports/submission-claims-legal-help")
            .matchQuery("submission-id", UUID_REGEX)
            .matchQuery("office", ANY_FORMAT_REGEX, "testOffice")
            .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
            .method("GET")
            .willRespondWith()
            .status(200)
            .body(EXAMPLE_CSV_BODY)
            .toPact());
  }

  @Pact(consumer = CONSUMER)
  public RequestResponsePact getCrimeLowerSubmission200(PactDslWithProvider builder) {
    // Defines expected 200 response for existing submission
    return requireNonEmptyBody(
        builder
            .given("no data exists for the export")
            .uponReceiving("a request to export a crime lower submission")
            .matchPath("/exports/submission-claims-crime-lower")
            .matchQuery("submission-id", UUID_REGEX)
            .matchQuery("office", ANY_FORMAT_REGEX, "testOffice")
            .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
            .method("GET")
            .willRespondWith()
            .status(200)
            .body(EXAMPLE_CSV_BODY)
            .toPact());
  }

  @Pact(consumer = CONSUMER)
  public RequestResponsePact getMediationSubmission200(PactDslWithProvider builder) {
    // Defines expected 200 response for existing submission
    return requireNonEmptyBody(
        builder
            .given("no data exists for the export")
            .uponReceiving("a request to export a mediation submission")
            .matchPath("/exports/submission-claims-mediation")
            .matchQuery("submission-id", UUID_REGEX)
            .matchQuery("office", ANY_FORMAT_REGEX, "testOffice")
            .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
            .method("GET")
            .willRespondWith()
            .status(200)
            .body(EXAMPLE_CSV_BODY)
            .toPact());
  }

  private static RequestResponsePact requireNonEmptyBody(RequestResponsePact pact) {
    pact.getInteractions()
        .forEach(
            interaction ->
                interaction
                    .asSynchronousRequestResponse()
                    .getResponse()
                    .getMatchingRules()
                    .addCategory("body")
                    .addRule("$", new RegexMatcher(NON_EMPTY_BODY_REGEX)));
    return pact;
  }

  @Test
  @DisplayName("Verify 200 response - Legal Help")
  @PactTestFor(pactMethod = "getLegalHelpSubmission200")
  void verifyLegalHelp200Response() {
    byte[] csvData =
        exportDataClaimsRestClient
            .getSubmissionExport("legal-help", SUBMISSION_ID, "testOffice")
            .map(HttpEntity::getBody)
            .block();

    assertThat(csvData).isNotEmpty();
  }

  @Test
  @DisplayName("Verify 200 response - Crime Lower")
  @PactTestFor(pactMethod = "getCrimeLowerSubmission200")
  void verifyCrimeLower200Response() {
    byte[] csvData =
        exportDataClaimsRestClient
            .getSubmissionExport("crime-lower", SUBMISSION_ID, "testOffice")
            .map(HttpEntity::getBody)
            .block();

    assertThat(csvData).isNotEmpty();
  }

  @Test
  @DisplayName("Verify 200 response - Mediation")
  @PactTestFor(pactMethod = "getMediationSubmission200")
  void verifyMediation200Response() {
    byte[] csvData =
        exportDataClaimsRestClient
            .getSubmissionExport("mediation", SUBMISSION_ID, "testOffice")
            .map(HttpEntity::getBody)
            .block();

    assertThat(csvData).isNotEmpty();
  }
}
