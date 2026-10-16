package uk.gov.justice.laa.payments.submit;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.LambdaDslObject;
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
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryEvent;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryEventType;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimHistoryResultSet;
import uk.gov.justice.laa.payments.submit.builder.AmendedFieldsBuilder;
import uk.gov.justice.laa.payments.submit.client.DataClaimsRestClient;
import uk.gov.justice.laa.payments.submit.config.ClaimsApiPactTestConfig;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"app.claims-api.url=http://localhost:1239"})
@PactConsumerTest
@PactTestFor(providerName = AbstractPactTest.PROVIDER)
@MockServerConfig(port = "1239") // Same as Claims API URL port
@Import(ClaimsApiPactTestConfig.class)
@DisplayName("GET: /api/v1/claims/{}/history PACT tests")
public final class GetClaimHistoryPactTest extends AbstractPactTest {

  private static final String EVENT_TYPE_REGEX = "(SUBMISSION|AMENDMENT|ASSESSMENT|VOID)";

  @Autowired DataClaimsRestClient dataClaimsRestClient;

  @Pact(consumer = CONSUMER)
  public RequestResponsePact getClaimHistory200(PactDslWithProvider builder) {
    return builder
        .given("a claim history exists")
        .uponReceiving("a request to fetch the history of a claim")
        .matchPath("/api/v1/claims/(" + UUID_REGEX + ")/history")
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(
            LambdaDsl.newJsonBody(
                    body ->
                        body.minArrayLike(
                            "events", 1, event -> historyEvent(event, "SUBMISSION", null)))
                .build())
        .toPact();
  }

  @Pact(consumer = CONSUMER)
  public RequestResponsePact getClaimHistoryWithAmendment200(PactDslWithProvider builder) {
    return builder
        .given("a claim history with an amendment event exists")
        .uponReceiving("a request to fetch the history of a claim that has been amended")
        .matchPath("/api/v1/claims/(" + UUID_REGEX + ")/history")
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(
            LambdaDsl.newJsonBody(
                    body ->
                        body.array(
                            "events",
                            events -> {
                              events.object(
                                  event ->
                                      historyEvent(
                                          event,
                                          "AMENDMENT",
                                          metadata ->
                                              // "before" is deliberately not pinned: it is an
                                              // explicit null for values not previously set
                                              metadata.minArrayLike(
                                                  "changes",
                                                  1,
                                                  change -> {
                                                    change.stringType(
                                                        "field_identifier",
                                                        "client.clientForename");
                                                    change.stringType("after", "Alexandra");
                                                    change.stringType("change_source", "REQUESTED");
                                                  })));
                              events.object(event -> historyEvent(event, "SUBMISSION", null));
                            }))
                .build())
        .toPact();
  }

  @Test
  @DisplayName("Verify 200 response")
  @PactTestFor(pactMethod = "getClaimHistory200")
  void verify200Response() {
    ClaimHistoryResultSet history = dataClaimsRestClient.getClaimHistory(CLAIM_ID).block();

    assertThat(history).isNotNull();
    assertThat(history.getEvents()).isNotEmpty();
    ClaimHistoryEvent event = history.getEvents().getFirst();
    assertThat(event.getEventType()).isNotNull();
    assertThat(event.getEventTimestamp()).isNotNull();
  }

  @Test
  @DisplayName("Verify 200 response with an amendment event")
  @PactTestFor(pactMethod = "getClaimHistoryWithAmendment200")
  void verify200ResponseWithAmendment() {
    ClaimHistoryResultSet history = dataClaimsRestClient.getClaimHistory(CLAIM_ID).block();

    assertThat(history).isNotNull();
    assertThat(history.getEvents())
        .anyMatch(event -> event.getEventType() == ClaimHistoryEventType.AMENDMENT);
    assertThat(AmendedFieldsBuilder.build(history.getEvents())).isNotEmpty();
  }

  private static void historyEvent(
      LambdaDslObject event,
      String exampleEventType,
      java.util.function.Consumer<LambdaDslObject> metadataFields) {
    event.stringMatcher("event_type", EVENT_TYPE_REGEX, exampleEventType);
    event.stringMatcher("event_timestamp", TIMESTAMP_REGEX, "2025-05-01T12:32:00Z");
    event.stringType("actor_id", "provider@example.com");
    event.uuid("source_id", CLAIM_ID);
    event.object(
        "metadata",
        metadata -> {
          if (metadataFields != null) {
            metadataFields.accept(metadata);
          }
        });
  }
}
