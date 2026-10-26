package uk.gov.justice.laa.payments.submit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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
import org.springframework.http.MediaType;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionPatch;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.payments.submit.client.DataClaimsRestClient;
import uk.gov.justice.laa.payments.submit.config.ClaimsApiPactTestConfig;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"app.claims-api.url=http://localhost:1238"})
@PactConsumerTest
@PactTestFor(providerName = AbstractPactTest.PROVIDER)
@MockServerConfig(port = "1238")
@Import(ClaimsApiPactTestConfig.class)
@DisplayName("PATCH: /api/v1/submissions/{} PACT tests")
public final class PatchSubmissionPactTest extends AbstractPactTest {

  private static final String OFFICE_CODE = "OFF_123";
  private static final String APPROVING_USER = "provider@example.com";

  @Autowired DataClaimsRestClient dataClaimsRestClient;

  @Pact(consumer = CONSUMER)
  public RequestResponsePact patchSubmission204(PactDslWithProvider builder) {
    return builder
        .given(
            "the system is ready to update a submission",
            Map.of("status", "VALIDATED_PENDING_APPROVAL"))
        .uponReceiving("a request to approve a submission for final validation")
        .path("/api/v1/submissions/" + SUBMISSION_ID)
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("PATCH")
        .headers(Map.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .body(
            LambdaDsl.newJsonBody(
                    body -> {
                      body.stringValue("status", "READY_FOR_FINAL_VALIDATION");
                      body.stringValue("office_account_number", OFFICE_CODE);
                      body.stringValue("created_by_user_id", APPROVING_USER);
                      // The generated patch model also serializes its unset fields.
                      body.nullValue("submission_id");
                      body.nullValue("bulk_submission_id");
                      body.nullValue("submission_period");
                      body.nullValue("area_of_law");
                      body.nullValue("provider_user_id");
                      body.nullValue("crime_lower_schedule_number");
                      body.nullValue("legal_help_submission_reference");
                      body.nullValue("mediation_submission_reference");
                      body.nullValue("previous_submission_id");
                      body.nullValue("is_nil_submission");
                      body.nullValue("number_of_claims");
                      body.nullValue("calculated_total_amount");
                      body.nullValue("assessed_total_amount");
                      body.nullValue("submitted");
                      body.nullValue("approved_on");
                      body.nullValue("approved_by_user_id");
                      body.nullValue("error_messages");
                      body.array("validation_messages", messages -> {});
                    })
                .build())
        .willRespondWith()
        .status(204)
        .toPact();
  }

  @Test
  @DisplayName("Verify approval completes with a 204 response")
  @PactTestFor(pactMethod = "patchSubmission204")
  void verify204Response() {
    var patch =
        new SubmissionPatch()
            .status(SubmissionStatus.READY_FOR_FINAL_VALIDATION)
            .officeAccountNumber(OFFICE_CODE)
            .createdByUserId(APPROVING_USER);

    assertDoesNotThrow(() -> dataClaimsRestClient.patchSubmission(SUBMISSION_ID, patch).block());
  }
}
