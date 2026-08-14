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
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClientV2;
import uk.gov.justice.laa.bulkclaim.config.ClaimsApiPactTestConfig;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;

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
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"app.claims-api.url=http://localhost:1232"})
@PactConsumerTest
@PactTestFor(providerName = AbstractPactTest.PROVIDER)
@MockServerConfig(port = "1232") // Same as Claims API URL port
@Import(ClaimsApiPactTestConfig.class)
@DisplayName("GET: /api/v1/submissions/{}/claims/{} PACT tests")
public final class GetClaimV2PactTest extends AbstractPactTest {

  @Autowired DataClaimsRestClientV2 dataClaimsRestClient;

  @SneakyThrows
  @Pact(consumer = CONSUMER)
  public RequestResponsePact getClaim200(PactDslWithProvider builder) {
    // Defines expected 200 response for existing claim using matchers
    return builder
        .given("a claim exists")
        .uponReceiving("a request to fetch a existing claim")
        .matchPath("/api/v2/submissions/(" + UUID_REGEX + ")/claims/(" + UUID_REGEX + ")")
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(
            LambdaDsl.newJsonBody(
                    // Only fields actually consumed by ClaimService / the area-of-law detail
                    // mappers (CrimeLowerClaimDetailsMapper, LegalHelpClaimDetailsMapper,
                    // MediationClaimDetailsMapper) are included here, to keep this pact flexible.
                    body -> {
                      body.uuid("id", CLAIM_ID);
                      body.uuid("submission_id", SUBMISSION_ID);
                      body.stringType("area_of_law", "LEGAL_HELP");
                      body.stringType("derived_claim_status", "READY_TO_PROCESS");
                      body.stringType("office_code", "string");
                      body.datetime("date_submitted", "yyyy-MM-dd'T'HH:mm:ssXXX");
                      body.stringType("unique_file_number", "string");
                      body.stringType("case_start_date", "string");
                      body.stringType("case_concluded_date", "string");
                      body.stringType("matter_type_code", "string");
                      body.stringType("fee_code", "string");
                      body.stringType("representation_order_date", "string");
                      body.stringType("stage_reached_code", "string");
                      body.stringType("outcome_code", "string");
                      body.stringType("client_forename", "string");
                      body.stringType("client_surname", "string");
                      body.stringType("unique_client_number", "string");
                      body.stringType("client_2_forename", "string");
                      body.stringType("client_2_surname", "string");
                      body.stringType("client_2_ucn", "string");
                      body.numberType("net_profit_costs_amount", 0);
                      body.numberType("net_disbursement_amount", 0);
                      body.numberType("disbursements_vat_amount", 0);
                      body.numberType("travel_waiting_costs_amount", 0);
                      body.numberType("net_waiting_costs_amount", 0);
                      body.booleanType("is_vat_applicable", true);
                      body.booleanType("is_london_rate", true);
                      body.object(
                          "fee_calculation_response",
                          fee -> {
                            fee.stringType("fee_code_description", "string");
                            fee.stringType("category_of_law", "string");
                            fee.numberType("total_amount", 0);
                            fee.booleanType("vat_indicator", true);
                            fee.numberType("calculated_vat_amount", 0);
                            fee.numberType("disbursement_amount", 0);
                            fee.numberType("disbursement_vat_amount", 0);
                            fee.numberType("fixed_fee_amount", 0);
                            fee.numberType("net_profit_costs_amount", 0);
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
        .matchPath("/api/v2/submissions/(" + UUID_REGEX + ")/claims/(" + UUID_REGEX + ")")
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
        .matchPath("/api/v2/submissions/(" + UUID_REGEX + ")/claims/(" + UUID_REGEX + ")")
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
    ClaimResponseV2 claimResponse =
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
