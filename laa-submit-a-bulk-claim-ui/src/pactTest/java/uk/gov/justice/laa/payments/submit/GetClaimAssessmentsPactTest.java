package uk.gov.justice.laa.payments.submit;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.dsl.LambdaDsl;
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
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AssessmentResultSet;
import uk.gov.justice.laa.payments.submit.client.DataClaimsRestClient;
import uk.gov.justice.laa.payments.submit.config.ClaimsApiPactTestConfig;

/**
 * The UI only ever asks for the latest assessment, so the paging and sort parameters are pinned to
 * the exact values {@code LatestAssessmentResolver} sends.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"app.claims-api.url=http://localhost:1240"})
@PactConsumerTest
@PactTestFor(providerName = AbstractPactTest.PROVIDER)
@MockServerConfig(port = "1240") // Same as Claims API URL port
@Import(ClaimsApiPactTestConfig.class)
@DisplayName("GET: /api/v1/claims/{}/assessments PACT tests")
public final class GetClaimAssessmentsPactTest extends AbstractPactTest {

  @Autowired DataClaimsRestClient dataClaimsRestClient;

  @Pact(consumer = CONSUMER)
  public RequestResponsePact getClaimAssessments200(PactDslWithProvider builder) {
    return builder
        .given("assessments exist for the claim")
        .uponReceiving("a request to fetch the latest assessment of a claim")
        .matchPath("/api/v1/claims/(" + UUID_REGEX + ")/assessments")
        .matchQuery(QUERY_PARAM_PAGE, "0")
        .matchQuery(QUERY_PARAM_SIZE, "1")
        .matchQuery(QUERY_PARAM_SORT, "createdOn,desc")
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(
            LambdaDsl.newJsonBody(
                    body ->
                        body.minArrayLike(
                            "assessments",
                            1,
                            assessment -> {
                              assessment.uuid("id", CLAIM_ID);
                              assessment.stringMatcher(
                                  "assessment_type",
                                  "(ESCAPE_CASE_ASSESSMENT|STAGE_DISBURSEMENT_ASSESSMENT|VOID)",
                                  "ESCAPE_CASE_ASSESSMENT");
                              assessment.stringMatcher(
                                  "assessment_outcome",
                                  "(PAID_IN_FULL|REDUCED_STILL_ESCAPED|REDUCED_TO_FIXED_FEE|NILLED)",
                                  "PAID_IN_FULL");
                              assessment.stringType("assessment_reason", "Reason");
                              assessment.numberType("fixed_fee_amount", 10.5);
                              assessment.numberType("net_profit_costs_amount", 10.5);
                              assessment.numberType("net_travel_costs_amount", 10.5);
                              assessment.numberType("net_waiting_costs_amount", 10.5);
                              assessment.numberType("net_cost_of_counsel_amount", 10.5);
                              assessment.numberType("disbursement_amount", 10.5);
                              assessment.numberType("disbursement_vat_amount", 10.5);
                              assessment.numberType(
                                  "detention_travel_and_waiting_costs_amount", 10.5);
                              assessment.numberType("jr_form_filling_amount", 10.5);
                              assessment.numberType("bolt_on_adjourned_hearing_fee", 10.5);
                              assessment.numberType("bolt_on_cmrh_oral_fee", 10.5);
                              assessment.numberType("bolt_on_cmrh_telephone_fee", 10.5);
                              assessment.numberType("bolt_on_substantive_hearing_fee", 10.5);
                              assessment.numberType("bolt_on_home_office_interview_fee", 10.5);
                              assessment.booleanType("is_vat_applicable", true);
                              assessment.numberType("allowed_total_vat", 20.1);
                              assessment.numberType("allowed_total_incl_vat", 120.6);
                              assessment.stringMatcher(
                                  "created_on", TIMESTAMP_REGEX, "2025-05-01T12:32:00Z");
                            }))
                .build())
        .toPact();
  }

  @Pact(consumer = CONSUMER)
  public RequestResponsePact getClaimAssessmentsEmpty200(PactDslWithProvider builder) {
    return builder
        .given("no assessments exist for the claim")
        .uponReceiving("a request to fetch the latest assessment of a claim with no assessments")
        .matchPath("/api/v1/claims/(" + UUID_REGEX + ")/assessments")
        .matchQuery(QUERY_PARAM_PAGE, "0")
        .matchQuery(QUERY_PARAM_SIZE, "1")
        .matchQuery(QUERY_PARAM_SORT, "createdOn,desc")
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(LambdaDsl.newJsonBody(body -> body.array("assessments", array -> {})).build())
        .toPact();
  }

  @Test
  @DisplayName("Verify 200 response")
  @PactTestFor(pactMethod = "getClaimAssessments200")
  void verify200Response() {
    AssessmentResultSet result =
        dataClaimsRestClient.getClaimAssessments(CLAIM_ID, 0, 1, "createdOn,desc").block();

    assertThat(result).isNotNull();
    assertThat(result.getAssessments()).hasSize(1);
    AssessmentGet assessment = result.getAssessments().getFirst();
    assertThat(assessment.getAssessmentType()).isNotNull();
    assertThat(assessment.getFixedFeeAmount()).isNotNull();
    assertThat(assessment.getNetProfitCostsAmount()).isNotNull();
    assertThat(assessment.getNetTravelCostsAmount()).isNotNull();
    assertThat(assessment.getNetWaitingCostsAmount()).isNotNull();
    assertThat(assessment.getNetCostOfCounselAmount()).isNotNull();
    assertThat(assessment.getDisbursementAmount()).isNotNull();
    assertThat(assessment.getDisbursementVatAmount()).isNotNull();
    assertThat(assessment.getDetentionTravelAndWaitingCostsAmount()).isNotNull();
    assertThat(assessment.getJrFormFillingAmount()).isNotNull();
    assertThat(assessment.getBoltOnAdjournedHearingFee()).isNotNull();
    assertThat(assessment.getBoltOnCmrhOralFee()).isNotNull();
    assertThat(assessment.getBoltOnCmrhTelephoneFee()).isNotNull();
    assertThat(assessment.getBoltOnSubstantiveHearingFee()).isNotNull();
    assertThat(assessment.getBoltOnHomeOfficeInterviewFee()).isNotNull();
    assertThat(assessment.getAllowedTotalVat()).isNotNull();
    assertThat(assessment.getAllowedTotalInclVat()).isNotNull();
    assertThat(assessment.getIsVatApplicable()).isNotNull();
  }

  @Test
  @DisplayName("Verify 200 response with no assessments")
  @PactTestFor(pactMethod = "getClaimAssessmentsEmpty200")
  void verify200ResponseWithNoAssessments() {
    AssessmentResultSet result =
        dataClaimsRestClient.getClaimAssessments(CLAIM_ID, 0, 1, "createdOn,desc").block();

    assertThat(result).isNotNull();
    assertThat(result.getAssessments()).isEmpty();
  }
}
