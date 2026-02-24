package uk.gov.justice.laa.bulkclaim;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import uk.gov.justice.laa.bulkclaim.client.ExportDataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.ClaimsApiPactTestConfig;

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
 *
 * @author Jamie Briggs
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"app.claims-api.url=http://localhost:1234"})
@PactConsumerTest
@PactTestFor(providerName = AbstractPactTest.PROVIDER)
@MockServerConfig(port = "1234") // Same as Claims API URL port
@Import(ClaimsApiPactTestConfig.class)
@DisplayName("GET: /exports/submission_claims_{area-of-law}.csv PACT tests")
public final class GetExportSubmissionPactTest extends AbstractPactTest {

  @Autowired ExportDataClaimsRestClient exportDataClaimsRestClient;

  @SneakyThrows
  @Pact(consumer = CONSUMER)
  public RequestResponsePact getSubmission200(PactDslWithProvider builder) {
    // Defines expected 200 response for existing submission
    return builder
        .given("multiple claims exist for the same submission")
        .uponReceiving("a request to export a submission")
        .matchPath("/exports/submission_claims_(legal-help|crime-lower|mediation).csv")
        .matchQuery("submissionId", UUID_REGEX)
        .matchQuery("office", ANY_FORMAT_REGEX)
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "text/csv"))
        .body("claim_id,status\n" + CLAIM_ID + ",READY_TO_PROCESS\n")
        .toPact();
  }

  @Test
  @DisplayName("Verify 200 response")
  @PactTestFor(pactMethod = "getSubmission200")
  void verify200Response() {
    byte[] csvData =
        exportDataClaimsRestClient
            .getSubmissionExport("crime-lower", SUBMISSION_ID, "testOffice")
            .map(response -> response.getBody())
            .block();

    assertThat(csvData).isNotNull();
  }
}
