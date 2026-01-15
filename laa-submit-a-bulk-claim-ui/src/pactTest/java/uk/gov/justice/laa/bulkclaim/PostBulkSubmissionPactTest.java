package uk.gov.justice.laa.bulkclaim;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import au.com.dius.pact.consumer.dsl.LambdaDsl;
import au.com.dius.pact.consumer.dsl.LambdaDslJsonArray;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.ClaimsApiPactTestConfig;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.CreateBulkSubmission201Response;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"claims-api.url=http://localhost:1236"})
@PactConsumerTest
@PactTestFor(providerName = AbstractPactTest.PROVIDER)
@MockServerConfig(port = "1236") // Same as Claims API URL port
@Import(ClaimsApiPactTestConfig.class)
@DisplayName("POST: /api/v0/bulk-submissions PACT tests")
public final class PostBulkSubmissionPactTest extends AbstractPactTest {

  @Autowired DataClaimsRestClient dataClaimsRestClient;

  @SneakyThrows
  @Pact(consumer = CONSUMER)
  public RequestResponsePact postBulkSubmission201(PactDslWithProvider builder) {
    // Defines expected 201 response for successfully submitting valid bulk submission
    return builder
        .given("the system is ready to process a valid bulk submission")
        .uponReceiving("a new bulk submission request")
        .path("/api/v0/bulk-submissions")
        .matchQuery("userId", ANY_FORMAT_REGEX)
        .matchQuery("offices", "([A-Z0-9]{6})")
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("POST")
        .withFileUpload("file", "test.txt", "text/plain", new byte[10])
        .willRespondWith()
        .status(201)
        .headers(Map.of("Content-Type", "application/json"))
        .body(
            LambdaDsl.newJsonBody(
                    body -> {
                      body.uuid(
                          "bulk_submission_id",
                          UUID.fromString("17bec50d-f3bb-4cee-95c4-68e0ce167ea5"));
                      body.array("submission_ids", LambdaDslJsonArray::uuid);
                    })
                .build())
        .toPact();
  }

  @SneakyThrows
  @Pact(consumer = CONSUMER)
  public RequestResponsePact postBulkSubmission400(PactDslWithProvider builder) {
    // Defines expected 400 response for uploading invalid bulk submission
    return builder
        .given("the submission file contains invalid data")
        .uponReceiving("a request to create a bulk submission with invalid data")
        .path("/api/v0/bulk-submissions")
        .matchQuery("userId", ANY_FORMAT_REGEX)
        .matchQuery("offices", "([A-Z0-9]{6})")
        .matchHeader(HttpHeaders.AUTHORIZATION, UUID_REGEX)
        .method("POST")
        .withFileUpload("file", "test.txt", "text/plain", new byte[10])
        .willRespondWith()
        .status(400)
        .headers(Map.of("Content-Type", "application/json"))
        .body(
            LambdaDsl.newJsonBody(
                    body -> {
                      body.uuid(
                          "bulk_submission_id",
                          UUID.fromString("17bec50d-f3bb-4cee-95c4-68e0ce167ea5"));
                      body.array("submission_ids", LambdaDslJsonArray::uuid);
                    })
                .build())
        .toPact();
  }

  @Test
  @DisplayName("Verify 201 response")
  @PactTestFor(pactMethod = "postBulkSubmission201")
  void verify201Response() {
    String userId = "test-user";
    List<String> offices = List.of("ABC123", "XYZ789");
    MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", new byte[10]);

    ResponseEntity<CreateBulkSubmission201Response> submission =
        dataClaimsRestClient.upload(file, userId, offices).block();
    assertThat(submission).isNotNull();
    assertThat(submission.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(submission.getBody().getBulkSubmissionId()).isNotNull();
  }

  @Test
  @DisplayName("Verify 400 response")
  @PactTestFor(pactMethod = "postBulkSubmission400")
  void verify400Response() {
    String userId = "test-user";
    List<String> offices = List.of("ABC123", "XYZ789");
    MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", new byte[10]);

    assertThrows(
        BadRequest.class, () -> dataClaimsRestClient.upload(file, userId, offices).block());
  }
}
