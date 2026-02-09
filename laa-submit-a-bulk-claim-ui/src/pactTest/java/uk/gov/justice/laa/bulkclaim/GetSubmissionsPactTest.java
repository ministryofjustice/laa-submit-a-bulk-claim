package uk.gov.justice.laa.bulkclaim;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import java.util.List;
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
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

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
    properties = {"app.claims-api.url=http://localhost:1235"})
@PactConsumerTest
@PactTestFor(providerName = AbstractPactTest.PROVIDER)
@MockServerConfig(port = "1235") // Same as Claims API URL port
@Import(ClaimsApiPactTestConfig.class)
@DisplayName("GET: /api/v1/submissions PACT tests")
public final class GetSubmissionsPactTest extends AbstractPactTest {

  @Autowired DataClaimsRestClient dataClaimsRestClient;

  @SneakyThrows
  @Pact(consumer = CONSUMER)
  public RequestResponsePact getSubmissions200(PactDslWithProvider builder) {
    // Defines expected 200 response for submission search
    return builder
        .given("a submission exists for the search criteria")
        .uponReceiving("a search request for submissions")
        .path("/api/v1/submissions")
        .matchQuery("submission_period", "([A-Z]{3}-[0-9]{4})")
        .matchQuery("offices", "([A-Z0-9]{6})")
        .matchQuery("area_of_law", "(LEGAL_HELP|CRIME_LOWER|MEDIATION)")
        .matchQuery(
            "submission_statuses",
            "(CREATED|READY_FOR_VALIDATION|VALIDATION_IN_PROGRESS|VALIDATION_SUCCEEDED"
                + "|VALIDATION_FAILED|REPLACED)")
        .matchQuery("page", ANY_NUMBER_REGEX)
        .matchQuery("size", ANY_NUMBER_REGEX)
        .matchQuery("sort", "(asc|desc)")
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("GET")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(
            LambdaDsl.newJsonBody(
                    body -> {
                      body.minArrayLike(
                          "content",
                          1,
                          submission -> {
                            submission.uuid(
                                "submission_id",
                                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"));
                            submission.uuid(
                                "bulk_submission_id",
                                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"));
                            submission.stringType("office_account_number", "string");
                            submission.stringType("submission_period", "string");
                            submission.stringType("area_of_law", "CRIME LOWER");
                            submission.stringType("provider_user_id", "string");
                            submission.stringType("status", "CREATED");
                            submission.stringType("crime_lower_schedule_number", "string");
                            submission.stringType("legal_help_submission_reference", "string");
                            submission.stringType("mediation_submission_reference", "string");
                            submission.uuid(
                                "previous_submission_id",
                                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"));
                            submission.booleanType("is_nil_submission", true);
                            submission.numberType("number_of_claims", 0);
                            submission.datetime("submitted", "yyyy-MM-dd'T'HH:mm:ssXXX");
                            submission.stringType("created_by_user_id", "string");
                          });
                      body.numberType("total_pages", 1);
                      body.numberType("total_elements", 1);
                      body.numberType("number", 0);
                      body.numberType("size", 10);
                    })
                .build())
        .toPact();
  }

  @SneakyThrows
  @Pact(consumer = CONSUMER)
  public RequestResponsePact getSubmissionsEmpty200(PactDslWithProvider builder) {
    // Defines expected 200 response for submission search, even when empty
    return builder
        .given("no submissions exist for the search criteria")
        .uponReceiving("a search request for submissions that returns no results")
        .path("/api/v1/submissions")
        .matchQuery("submission_period", "([A-Z]{3}-[0-9]{4})")
        .matchQuery("offices", "([A-Z0-9]{6})")
        .matchQuery("area_of_law", "(LEGAL_HELP|CRIME_LOWER|MEDIATION)")
        .matchQuery(
            "submission_statuses",
            "(CREATED|READY_FOR_VALIDATION|VALIDATION_IN_PROGRESS|VALIDATION_SUCCEEDED"
                + "|VALIDATION_FAILED|REPLACED)")
        .matchQuery("page", ANY_NUMBER_REGEX)
        .matchQuery("size", ANY_NUMBER_REGEX)
        .matchQuery("sort", "(asc|desc)")
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
                      body.numberType("number", 0);
                      body.numberType("size", 10);
                    })
                .build())
        .toPact();
  }

  @Test
  @DisplayName("Verify 200 response")
  @PactTestFor(pactMethod = "getSubmissions200")
  void verify200Response() {
    String submissionPeriod = "JAN-2025";
    try {
      SubmissionsResultSet submission =
          dataClaimsRestClient
              .search(
                  USER_OFFICES,
                  submissionPeriod,
                  AreaOfLaw.LEGAL_HELP,
                  List.of(SubmissionStatus.CREATED),
                  10,
                  10,
                  "asc")
              .block();
      assertThat(submission.getContent().size()).isEqualTo(1);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  @DisplayName("Verify 200 response empty")
  @PactTestFor(pactMethod = "getSubmissionsEmpty200")
  void verify200ResponseEmpty() {
    String submissionPeriod = "JAN-2025";
    SubmissionsResultSet submission =
        dataClaimsRestClient
            .search(
                USER_OFFICES,
                submissionPeriod,
                AreaOfLaw.LEGAL_HELP,
                List.of(SubmissionStatus.CREATED),
                10,
                10,
                "asc")
            .block();

    assertThat(submission.getContent().isEmpty()).isTrue();
  }
}
