package uk.gov.justice.laa.bulkclaim;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.ClaimsApiPactTestConfig;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionPatch;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"app.claims-api.url=http://localhost:1241"})
@PactConsumerTest
@PactTestFor(providerName = AbstractPactTest.PROVIDER)
@MockServerConfig(port = "1241")
@Import(ClaimsApiPactTestConfig.class)
class PatchDraftConfirmationPactTest extends AbstractPactTest {

  @Autowired private DataClaimsRestClient dataClaimsRestClient;

  @Pact(consumer = CONSUMER)
  @SneakyThrows
  RequestResponsePact confirmDraft(PactDslWithProvider builder) {
    return builder
        .given("a draft submission can be confirmed")
        .uponReceiving("a request to confirm a valid draft submission")
        .path("/api/v1/submissions/" + SUBMISSION_ID)
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("PATCH")
        .headers(Map.of("Content-Type", "application/json"))
        .body(JsonMapper.builder().build().writeValueAsString(confirmationPatch()))
        .willRespondWith()
        .status(204)
        .toPact();
  }

  @Pact(consumer = CONSUMER)
  @SneakyThrows
  RequestResponsePact rejectDraftConfirmation(PactDslWithProvider builder) {
    return builder
        .given("a draft submission has confirmation errors")
        .uponReceiving("a request to confirm an invalid draft submission")
        .path("/api/v1/submissions/" + SUBMISSION_ID)
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("PATCH")
        .headers(Map.of("Content-Type", "application/json"))
        .body(JsonMapper.builder().build().writeValueAsString(confirmationPatch()))
        .willRespondWith()
        .status(400)
        .matchHeader("Content-Type", "application/(problem\\+)?json")
        .body(
            LambdaDsl.newJsonBody(
                    body ->
                        body.minArrayLike(
                            "claimReports",
                            1,
                            report -> {
                              report.uuid("claimId", CLAIM_ID);
                              report.minArrayLike(
                                  "validationMessages",
                                  1,
                                  message -> {
                                    message.stringType(
                                        "displayMessage", "Complete the inquest details");
                                    message.stringType(
                                        "technicalMessage", "Missing inquest fields");
                                    message.stringValue("type", "ERROR");
                                    message.stringType("source", "CLAIMS_API");
                                  });
                            }))
                .build())
        .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "confirmDraft")
  void confirmsDraft() {
    assertThat(
            dataClaimsRestClient
                .updateSubmission(SUBMISSION_ID, confirmationPatch())
                .getStatusCode())
        .isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  @PactTestFor(pactMethod = "rejectDraftConfirmation")
  void returnsClaimErrors() {
    assertThrows(
        BadRequest.class,
        () -> dataClaimsRestClient.updateSubmission(SUBMISSION_ID, confirmationPatch()));
  }

  private SubmissionPatch confirmationPatch() {
    return new SubmissionPatch()
        .submissionId(SUBMISSION_ID)
        .status(SubmissionStatus.VALIDATION_SUCCEEDED);
  }
}
