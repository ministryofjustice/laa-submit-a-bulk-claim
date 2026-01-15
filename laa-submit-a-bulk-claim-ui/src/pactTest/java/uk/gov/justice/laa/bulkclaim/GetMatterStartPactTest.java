package uk.gov.justice.laa.bulkclaim;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import au.com.dius.pact.consumer.dsl.LambdaDsl;
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
import uk.gov.justice.laa.dstew.payments.claimsdata.model.MatterStartGet;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"claims-api.url=http://localhost:1233"})
@PactConsumerTest
@PactTestFor(providerName = AbstractPactTest.PROVIDER)
@MockServerConfig(port = "1233") // Same as Claims API URL port
@Import(ClaimsApiPactTestConfig.class)
@DisplayName("GET: /api/v0/submissions/{}/matter-starts/{} PACT tests")
public final class GetMatterStartPactTest extends AbstractPactTest {

  @Autowired DataClaimsRestClient dataClaimsRestClient;

  @SneakyThrows
  @Pact(consumer = CONSUMER)
  public RequestResponsePact getMatterStart200(PactDslWithProvider builder) {
    // Defines expected 200 response for existing matter start
    return builder
        .given("a matter start exists for the given submission")
        .uponReceiving("a request to fetch a existing matter start")
        .matchPath("/api/v0/submissions/(" + UUID_REGEX + ")/matter-starts/(" + UUID_REGEX + ")")
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(
            LambdaDsl.newJsonBody(
                    body -> {
                      body.stringType("schedule_reference", "string");
                      body.stringType("category_code", "AAP");
                      body.stringType("procurement_area_code", "string");
                      body.stringType("access_point_code", "string");
                      body.stringType("delivery_location", "string");
                      body.stringType("mediation_type", "MDCS Child Only Sole");
                      body.numberType("number_of_matter_starts", 0);
                      body.stringType("created_by_user_id", "string");
                    })
                .build())
        .toPact();
  }

  @SneakyThrows
  @Pact(consumer = CONSUMER)
  public RequestResponsePact getMatterStart404(PactDslWithProvider builder) {
    // Defines expected 404 response for when matter start does not exist
    return builder
        .given("no matter starts exists")
        .uponReceiving("a request to fetch a non-existent matter start")
        .matchPath("/api/v0/submissions/(" + UUID_REGEX + ")/matter-starts/(" + UUID_REGEX + ")")
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(404)
        .headers(Map.of("Content-Type", "application/json"))
        .toPact();
  }

  @SneakyThrows
  @Pact(consumer = CONSUMER)
  public RequestResponsePact getMatterStartNoSubmission404(PactDslWithProvider builder) {
    // Defines expected 404 response for when matter start does not exist
    return builder
        .given("no submission exists")
        .uponReceiving("a request to fetch a matter start from a non-existent submission")
        .matchPath("/api/v0/submissions/(" + UUID_REGEX + ")/matter-starts/(" + UUID_REGEX + ")")
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(404)
        .headers(Map.of("Content-Type", "application/json"))
        .toPact();
  }

  @Test
  @DisplayName("Verify 200 response")
  @PactTestFor(pactMethod = "getMatterStart200")
  void verify200Response() {
    MatterStartGet matterStart =
        dataClaimsRestClient.getSubmissionMatterStart(submissionId, matterStartId).block();

    assertThat(matterStart).isNotNull();
  }

  @Test
  @DisplayName("Verify 404 response")
  @PactTestFor(pactMethod = "getMatterStart404")
  void verify404Response() {
    assertThrows(
        NotFound.class,
        () -> dataClaimsRestClient.getSubmissionMatterStart(submissionId, matterStartId).block());
  }

  @Test
  @DisplayName("Verify 404 response no submissions")
  @PactTestFor(pactMethod = "getMatterStartNoSubmission404")
  void verify404NoSubmissionResponse() {
    assertThrows(
        NotFound.class,
        () -> dataClaimsRestClient.getSubmissionMatterStart(submissionId, matterStartId).block());
  }
}
