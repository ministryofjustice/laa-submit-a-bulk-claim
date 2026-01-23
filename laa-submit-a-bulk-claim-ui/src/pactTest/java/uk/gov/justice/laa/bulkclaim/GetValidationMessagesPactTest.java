package uk.gov.justice.laa.bulkclaim;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.ClaimsApiPactTestConfig;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessagesResponse;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"app.claims-api.url=http://localhost:1231"})
@PactConsumerTest
@PactTestFor(providerName = AbstractPactTest.PROVIDER)
@MockServerConfig(port = "1231") // Same as Claims API URL port
@Import(ClaimsApiPactTestConfig.class)
@DisplayName("GET: /api/v1/validation-messages PACT tests")
public final class GetValidationMessagesPactTest extends AbstractPactTest {

  @Autowired DataClaimsRestClient dataClaimsRestClient;

  @SneakyThrows
  @Pact(consumer = CONSUMER)
  public RequestResponsePact getValidationMessages200(PactDslWithProvider builder) {
    // Defines expected 200 response for validation messages response
    return builder
        .given("validation messages exist for the search criteria")
        .uponReceiving("a request to search for validation messages")
        .path("/api/v1/validation-messages")
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
        .body(
            LambdaDsl.newJsonBody(
                    body -> {
                      body.numberType("total_pages", 1);
                      body.numberType("total_elements", 3);
                      body.numberType("total_claims", 2);
                      body.numberType("number", 0);
                      body.numberType("size", 10);
                      body.minArrayLike(
                          "content",
                          1,
                          message -> {
                            message.uuid(
                                "id", UUID.fromString("9d21c19c-4eab-4869-ad28-db537ba3e497"));
                            message.uuid(
                                "submission_id",
                                UUID.fromString("0561d67b-30ed-412e-8231-f6296a53538d"));
                            message.stringType("source", "Message source");
                            message.stringType("display_message", "Message description");
                            message.stringMatcher("type", "(ERROR|WARNING)", "ERROR");
                          });
                    })
                .build())
        .toPact();
  }

  @SneakyThrows
  @Pact(consumer = CONSUMER)
  public RequestResponsePact getValidationMessagesEmpty200(PactDslWithProvider builder) {
    // Defines expected 200 response for validation messages response, even when empty
    return builder
        .given("no validation messages exist for the search criteria")
        .uponReceiving("a request to search for validation messages with no results")
        .path("/api/v1/validation-messages")
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
        .body(
            LambdaDsl.newJsonBody(
                    body -> {
                      body.array("content", array -> {});
                      body.numberType("total_pages", 0);
                      body.numberType("total_elements", 0);
                      body.numberType("total_claims", 0);
                      body.numberType("number", 0);
                      body.numberType("size", 10);
                    })
                .build())
        .toPact();
  }

  @Test
  @DisplayName("Verify 200 response")
  @PactTestFor(pactMethod = "getValidationMessages200")
  void verify200Response() {
    ValidationMessagesResponse claims =
        dataClaimsRestClient
            .getValidationMessages(SUBMISSION_ID, CLAIM_ID, "ERROR", "Source", 1, 10)
            .block();

    assertThat(claims.getContent().size()).isEqualTo(1);
  }

  @Test
  @DisplayName("Verify 200 response empty")
  @PactTestFor(pactMethod = "getValidationMessagesEmpty200")
  void verify200ResponseEmpty() {
    ValidationMessagesResponse claims =
        dataClaimsRestClient
            .getValidationMessages(SUBMISSION_ID, CLAIM_ID, "ERROR", "Source", 1, 10)
            .block();

    assertThat(claims.getContent().isEmpty()).isTrue();
  }
}
