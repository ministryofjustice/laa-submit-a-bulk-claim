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
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.ClaimsApiPactTestConfig;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessagesResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"claims-api.url=http://localhost:1231"})
@PactConsumerTest
@PactTestFor(providerName = AbstractPactTest.PROVIDER)
@MockServerConfig(port = "1231") // Same as Claims API URL port
@Import(ClaimsApiPactTestConfig.class)
@DisplayName("GET: /api/v0/validation-messages PACT tests")
public final class GetValidationMessagesPactTest extends AbstractPactTest {

  @Autowired
  DataClaimsRestClient dataClaimsRestClient;


  @SneakyThrows
  @Pact(consumer = CONSUMER)
  public RequestResponsePact getValidationMessages200(PactDslWithProvider builder) {
    String claimsResponse = readJsonFromFile("get-validation-messages-200.json");
    // Defines expected 200 response for validation messages response
    return builder
        .given("validation messages exist for the search criteria")
        .uponReceiving("a request to search for validation messages")
        .path("/api/v0/validation-messages")
        .matchQuery("submission-id", UUID_REGEX)
        .matchQuery("claim-id", UUID_REGEX)
        .matchQuery("type", "(ERROR|WARNING)")
        .matchQuery("source", ANY_FORMAT_REGEX)
        .matchQuery("page", ANY_NUMBER_REGEX)
        .matchQuery("size", ANY_NUMBER_REGEX)
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(claimsResponse)
        .toPact();
  }

  @SneakyThrows
  @Pact(consumer = CONSUMER)
  public RequestResponsePact getValidationMessagesEmpty200(PactDslWithProvider builder) {
    String clamsResponse = readJsonFromFile("get-empty-search-200.json");
    // Defines expected 200 response for validation messages response, even when empty
    return builder
        .given("no validation messages exist for the search criteria")
        .uponReceiving("a request to search for validation messages with no results")
        .path("/api/v0/validation-messages")
        .matchQuery("submission-id", UUID_REGEX)
        .matchQuery("claim-id", UUID_REGEX)
        .matchQuery("type", "(ERROR|WARNING)")
        .matchQuery("source", ANY_FORMAT_REGEX)
        .matchQuery("page", ANY_NUMBER_REGEX)
        .matchQuery("size", ANY_NUMBER_REGEX)
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(clamsResponse)
        .toPact();
  }


  @Test
  @DisplayName("Verify 200 response")
  @PactTestFor(pactMethod = "getValidationMessages200")
  void verify200Response() {
    ValidationMessagesResponse claims = dataClaimsRestClient.getValidationMessages(submissionId,
        claimId,
        "ERROR",
        "Source",
        1,
        10).block();

    assertThat(claims.getContent().size()).isEqualTo(1);
  }

  @Test
  @DisplayName("Verify 200 response empty")
  @PactTestFor(pactMethod = "getValidationMessagesEmpty200")
  void verify200ResponseEmpty() {
    ValidationMessagesResponse claims = dataClaimsRestClient.getValidationMessages(submissionId,
        claimId,
        "ERROR",
        "Source",
        1,
        10).block();

    assertThat(claims.getContent().isEmpty()).isTrue();
  }



}
