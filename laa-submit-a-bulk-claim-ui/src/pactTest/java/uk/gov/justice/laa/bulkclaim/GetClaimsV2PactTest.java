package uk.gov.justice.laa.bulkclaim;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClientV2;
import uk.gov.justice.laa.bulkclaim.config.ClaimsApiPactTestConfig;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResultSetV2;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"app.claims-api.url=http://localhost:1231"})
@PactConsumerTest
@PactTestFor(providerName = AbstractPactTest.PROVIDER)
@MockServerConfig(port = "1231") // Same as Claims API URL port
@Import(ClaimsApiPactTestConfig.class)
@DisplayName("GET: /api/v2/claims PACT tests")
public class GetClaimsV2PactTest extends AbstractPactTest {
  @Autowired DataClaimsRestClientV2 dataClaimsRestClient;

  @Pact(consumer = CONSUMER)
  public RequestResponsePact getClaims200(PactDslWithProvider builder) {
    return builder
        .given("claims exist for the search criteria")
        .uponReceiving("a request to search for claims")
        .path("/api/v2/claims")
        .matchQuery("submission_id", UUID_REGEX)
        .matchQuery("office_code", OFFICE_CODE_REGEX)
        .matchQuery("page", ANY_NUMBER_REGEX)
        .matchQuery("size", ANY_NUMBER_REGEX)
        .matchQuery("sort", SORT_REGEX_V2, "client_name,asc")
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(EXPECTED_CLAIMS_SEARCH_RESULTS_V2.build())
        .toPact();
  }

  @Pact(consumer = CONSUMER)
  public RequestResponsePact getClaimsEmpty200(PactDslWithProvider builder) {
    // Defines expected 200 response for empty search using matchers
    return builder
        .given("no claims exist for the search criteria")
        .uponReceiving("a request to search for claims with no results")
        .path("/api/v2/claims")
        .matchQuery("office_code", OFFICE_CODE_REGEX)
        .matchQuery("submission_id", UUID_REGEX)
        .matchQuery("page", ANY_NUMBER_REGEX)
        .matchQuery("size", ANY_NUMBER_REGEX)
        .matchQuery("sort", SORT_REGEX_V2, "status,asc")
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(EMPTY_BODY.build())
        .toPact();
  }

  @Test
  @DisplayName("Verify 200 response for get claims v2")
  @PactTestFor(pactMethod = "getClaims200")
  void verify200Response() {
    ClaimResultSetV2 claims =
        dataClaimsRestClient
            .getClaims(USER_OFFICES.get(0), SUBMISSION_ID, 1, 10, "client_surname,asc")
            .getBody();

    assertThat(claims.getContent().size()).isEqualTo(1);
  }

  @Test
  @DisplayName("Verify 200 response empty for get claims v2")
  @PactTestFor(pactMethod = "getClaimsEmpty200")
  void verify200ResponseEmpty() {
    ClaimResultSetV2 claims =
        dataClaimsRestClient
            .getClaims(USER_OFFICES.get(0), SUBMISSION_ID, 1, 10, "status,asc")
            .getBody();

    assertThat(claims.getContent().isEmpty()).isTrue();
  }
}
