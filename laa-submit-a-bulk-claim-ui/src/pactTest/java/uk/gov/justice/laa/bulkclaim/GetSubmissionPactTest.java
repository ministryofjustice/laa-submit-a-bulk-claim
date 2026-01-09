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
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"claims-api.url=http://localhost:1234"})
@PactConsumerTest
@PactTestFor(providerName = AbstractPactTest.PROVIDER)
@MockServerConfig(port = "1234") // Same as Claims API URL port
@Import(ClaimsApiPactTestConfig.class)
@DisplayName("GET: /api/v0/submissions/{} PACT tests")
public final class GetSubmissionPactTest extends AbstractPactTest {

  @Autowired
  DataClaimsRestClient dataClaimsRestClient;

  @SneakyThrows
  @Pact(consumer = CONSUMER)
  public RequestResponsePact getSubmission200(PactDslWithProvider builder) {
    String submissionResponse = readJsonFromFile("get-submission-200.json");
    // Defines expected 200 response for existing submission
    return builder
        .given("a submission exists")
        .uponReceiving("a request to fetch a specific submission")
        .matchPath("/api/v0/submissions/(" + UUID_REGEX + ")")
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
  public RequestResponsePact getSubmission404(PactDslWithProvider builder) {
    // Defines expected 404 response for missing submission
    return builder
        .given("no submission exists")
        .uponReceiving("a request to fetch a non-existent submission")
        .matchPath("/api/v0/submissions/(" + UUID_REGEX + ")")
        .method("GET")
        .willRespondWith()
        .status(404)
        .headers(Map.of("Content-Type", "application/json"))
        .toPact();
  }


  @Test
  @DisplayName("Verify 200 response")
  @PactTestFor(pactMethod = "getSubmission200")
  void verify200Response() {
    SubmissionResponse submission = dataClaimsRestClient.getSubmission(submissionId).block();

    assertThat(submission).isNotNull();
    assertThat(submission.getSubmissionId()).isEqualTo(submissionId);
  }

  @Test
  @DisplayName("Verify 404 response")
  @PactTestFor(pactMethod = "getSubmission404")
  void verify404Response() {
    assertThrows(
        NotFound.class,
        () ->
            dataClaimsRestClient.getSubmission(submissionId).block());
  }



}
