package uk.gov.justice.laa.bulkclaim;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.ClaimsApiPactTestConfig;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"claims-api.url=http://localhost:1234"})
@PactConsumerTest
@PactTestFor(providerName = AbstractPactTest.PROVIDER)
@MockServerConfig(port = "1234") // Same as Claims API URL port
@Import(ClaimsApiPactTestConfig.class)
@DisplayName("GET: /api/v0/submissions/{}/claims/{} PACT tests")
public final class GetClaimPactTest extends AbstractPactTest {

  @Autowired
  DataClaimsRestClient dataClaimsRestClient;

  @SneakyThrows
  @Pact(consumer = CONSUMER)
  public RequestResponsePact getClaim200(PactDslWithProvider builder) {
    String submissionResponse = readJsonFromFile("get-claim-200.json");
    // Defines expected 200 response for existing submission
    return builder
        .given("a claim exists for a submission")
        .uponReceiving("a request for a claim within a submission")
        .matchPath("/api/v0/submissions/(" + UUID_REGEX + ")/claims/(" + UUID_REGEX + ")")
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(submissionResponse)
        .toPact();
  }

  @SneakyThrows
  @Pact(consumer = CONSUMER)
  public RequestResponsePact getClaim404(PactDslWithProvider builder) {
    // Defines expected 404 response for when either submission or claim does not exist
    return builder
        .given("a claim or submission does not exists for the given id's")
        .uponReceiving("a request for a claim within a submission")
        .matchPath("/api/v0/submissions/(" + UUID_REGEX + ")/claims/(" + UUID_REGEX + ")")
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(404)
        .headers(Map.of("Content-Type", "application/json"))
        .toPact();
  }


  @Test
  @DisplayName("Verify 200 response")
  @PactTestFor(pactMethod = "getClaim200")
  void verify200Response() {
    ClaimResponse claimResponse = dataClaimsRestClient.getSubmissionClaim(submissionId, claimId).block();

    assertThat(claimResponse).isNotNull();
    assertThat(claimResponse.getId()).isEqualTo(claimId.toString());
    assertThat(claimResponse.getSubmissionId()).isEqualTo(submissionId.toString());
  }

  @Test
  @DisplayName("Verify 404 response")
  @PactTestFor(pactMethod = "getClaim404")
  void verify404Response() {
    assertThrows(
        NotFound.class,
        () ->
            dataClaimsRestClient.getSubmissionClaim(submissionId, claimId).block());
  }



}
