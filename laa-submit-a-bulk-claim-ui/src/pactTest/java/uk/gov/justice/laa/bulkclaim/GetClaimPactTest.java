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
import java.util.UUID;
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
    properties = {"app.claims-api.url=http://localhost:1232"})
@PactConsumerTest
@PactTestFor(providerName = AbstractPactTest.PROVIDER)
@MockServerConfig(port = "1232") // Same as Claims API URL port
@Import(ClaimsApiPactTestConfig.class)
@DisplayName("GET: /api/v1/submissions/{}/claims/{} PACT tests")
public final class GetClaimPactTest extends AbstractPactTest {

  @Autowired DataClaimsRestClient dataClaimsRestClient;

  @SneakyThrows
  @Pact(consumer = CONSUMER)
  public RequestResponsePact getClaim200(PactDslWithProvider builder) {
    // Defines expected 200 response for existing claim using matchers
    return builder
        .given("a claim exists")
        .uponReceiving("a request to fetch a existing claim")
        .matchPath("/api/v1/submissions/(" + UUID_REGEX + ")/claims/(" + UUID_REGEX + ")")
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(
            LambdaDsl.newJsonBody(
                    body -> {
                      body.uuid("id", CLAIM_ID);
                      body.uuid("submission_id", SUBMISSION_ID);
                      body.stringType("status", "READY_TO_PROCESS");
                      body.numberType("line_number", 0);
                      body.stringType("unique_file_number", "string");
                      body.stringType("case_start_date", "string");
                      body.stringType("case_concluded_date", "string");
                      body.stringType("matter_type_code", "string");
                      body.stringType("crime_matter_type_code", "string");
                      body.stringType("fee_code", "string");
                      body.stringType("client_forename", "string");
                      body.stringType("client_surname", "string");
                      body.stringType("unique_client_number", "string");
                      body.stringType("client_2_forename", "string");
                      body.stringType("client_2_surname", "string");
                      body.stringType("client_2_ucn", "string");
                      body.stringType("stage_reached_code", "string");
                      body.stringType("standard_fee_category_code", "string");
                      body.stringType("outcome_code", "string");
                      body.numberType("net_profit_costs_amount", 0);
                      body.numberType("net_disbursement_amount", 0);
                      body.numberType("net_counsel_costs_amount", 0);
                      body.numberType("disbursements_vat_amount", 0);
                      body.numberType("travel_waiting_costs_amount", 0);
                      body.numberType("net_waiting_costs_amount", 0);
                      body.booleanType("is_vat_applicable", true);
                      body.numberType("adjourned_hearing_fee_amount", 0);
                      body.numberType("detention_travel_waiting_costs_amount", 0);
                      body.numberType("jr_form_filling_amount", 0);
                      body.numberType("cmrh_oral_count", 0);
                      body.numberType("cmrh_telephone_count", 0);
                      body.numberType("ho_interview", 0);
                      body.stringType("submission_period", "string");
                      body.object(
                          "fee_calculation_response",
                          fee -> {
                            fee.stringType("calculated_fee_detail_id", "string");
                            fee.uuid(
                                "claim_summary_fee_id",
                                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"));
                            fee.uuid("claim_id", CLAIM_ID);
                            fee.stringType("fee_code", "string");
                            fee.stringType("fee_code_description", "string");
                            fee.stringType("fee_type", "HOURLY");
                            fee.stringType("category_of_law", "string");
                            fee.numberType("total_amount", 0);
                            fee.booleanType("vat_indicator", true);
                            fee.numberType("calculated_vat_amount", 0);
                            fee.numberType("disbursement_amount", 0);
                            fee.numberType("requested_net_disbursement_amount", 0);
                            fee.numberType("disbursement_vat_amount", 0);
                            fee.numberType("net_profit_costs_amount", 0);
                            fee.numberType("requested_net_profit_costs_amount", 0);
                            fee.numberType("net_cost_of_counsel_amount", 0);
                            fee.numberType("net_travel_costs_amount", 0);
                            fee.numberType("net_waiting_costs_amount", 0);
                            fee.numberType("detention_travel_and_waiting_costs_amount", 0);
                            fee.numberType("jr_form_filling_amount", 0);
                            fee.numberType("travel_and_waiting_costs_amount", 0);
                            fee.object(
                                "bolt_on_details",
                                bolt -> {
                                  bolt.numberType("bolt_on_adjourned_hearing_fee", 0);
                                  bolt.numberType("bolt_on_cmrh_telephone_fee", 0);
                                  bolt.numberType("bolt_on_cmrh_oral_fee", 0);
                                  bolt.numberType("bolt_on_home_office_interview_fee", 0);
                                  bolt.numberType("bolt_on_substantive_hearing_fee", 0);
                                  bolt.booleanType("escape_case_flag", true);
                                });
                          });
                    })
                .build())
        .toPact();
  }

  @SneakyThrows
  @Pact(consumer = CONSUMER)
  public RequestResponsePact getClaim404(PactDslWithProvider builder) {
    // Defines expected 404 response for when either submission or claim does not exist
    return builder
        .given("no claim exists")
        .uponReceiving("a request to fetch a non-existent claim")
        .matchPath("/api/v1/submissions/(" + UUID_REGEX + ")/claims/(" + UUID_REGEX + ")")
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(404)
        .matchHeader("Content-Type", "application/(problem\\+)?json")
        .toPact();
  }

  @SneakyThrows
  @Pact(consumer = CONSUMER)
  public RequestResponsePact getClaimNoSubmission404(PactDslWithProvider builder) {
    // Defines expected 404 response for when either submission or claim does not exist
    return builder
        .given("no submission exists")
        .uponReceiving("a request to fetch a claim from a non-existent submission")
        .matchPath("/api/v1/submissions/(" + UUID_REGEX + ")/claims/(" + UUID_REGEX + ")")
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(404)
        .matchHeader("Content-Type", "application/(problem\\+)?json")
        .toPact();
  }

  @Test
  @DisplayName("Verify 200 response")
  @PactTestFor(pactMethod = "getClaim200")
  void verify200Response() {
    ClaimResponse claimResponse =
        dataClaimsRestClient.getSubmissionClaim(SUBMISSION_ID, CLAIM_ID).block();

    assertThat(claimResponse).isNotNull();
    assertThat(claimResponse.getId()).isEqualTo(CLAIM_ID.toString());
    assertThat(claimResponse.getSubmissionId()).isEqualTo(SUBMISSION_ID.toString());
  }

  @Test
  @DisplayName("Verify 404 response")
  @PactTestFor(pactMethod = "getClaim404")
  void verify404Response() {
    assertThrows(
        NotFound.class,
        () -> dataClaimsRestClient.getSubmissionClaim(SUBMISSION_ID, CLAIM_ID).block());
  }

  @Test
  @DisplayName("Verify 404 response no submission")
  @PactTestFor(pactMethod = "getClaimNoSubmission404")
  void verify404ResponseNoSubmission() {
    assertThrows(
        NotFound.class,
        () -> dataClaimsRestClient.getSubmissionClaim(SUBMISSION_ID, CLAIM_ID).block());
  }
}
