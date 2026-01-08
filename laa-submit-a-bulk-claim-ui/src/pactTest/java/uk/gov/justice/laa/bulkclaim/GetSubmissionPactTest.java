package uk.gov.justice.laa.bulkclaim;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
@DisplayName("/api/v0/submission/{} PACT tests")
public final class GetSubmissionPactTest extends AbstractPactTest {

  final UUID SUBMISSION_ID = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");

  @Autowired
  DataClaimsRestClient dataClaimsRestClient;

  @SneakyThrows
  @Pact(consumer = CONSUMER)
  public RequestResponsePact getSubmission200(PactDslWithProvider builder) {
    String submissionResponse = readJsonFromFile("get-submission-200.json");
    return builder
        .given("a submission exists")
        .uponReceiving("a request for a submission")
        .path("/api/v0/submissions/" + SUBMISSION_ID)
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
    return builder
        .given("a submission does not exists")
        .uponReceiving("a request for a submission")
        .path("/api/v0/submissions/" + SUBMISSION_ID)
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
    SubmissionResponse submission = dataClaimsRestClient.getSubmission(SUBMISSION_ID).block();

    assertThat(submission).isNotNull();
    assertThat(submission.getSubmissionId()).isEqualTo(SUBMISSION_ID);
  }

  @Test
  @DisplayName("Verify 404 response")
  @PactTestFor(pactMethod = "getSubmission404")
  void verify404Response() {
    assertThrows(
        NotFound.class,
        () ->
            dataClaimsRestClient.getSubmission(SUBMISSION_ID).block());
  }


}
