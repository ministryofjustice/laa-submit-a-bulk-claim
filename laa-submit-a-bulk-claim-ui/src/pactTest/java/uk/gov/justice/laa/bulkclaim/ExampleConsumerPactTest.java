package uk.gov.justice.laa.bulkclaim;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit.MockServerConfig;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.ClaimsApiPactTestConfig;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"claims-api.url=http://localhost:1234"})
@PactConsumerTest
@PactTestFor(providerName = "laa-data-claim-api")
@MockServerConfig(port = "1234") // Same as Claims API URL port
@Import(ClaimsApiPactTestConfig.class)
public class ExampleConsumerPactTest {

  final UUID SUBMISSION_ID = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");

  @Autowired
  DataClaimsRestClient dataClaimsRestClient;

  @MockitoBean
  OAuth2AuthorizedClientManager authorizedClientManager;

  @SneakyThrows
  @Pact(consumer = "submission-consumer")
  public RequestResponsePact getSubmission(PactDslWithProvider builder) {
    String submissionResponse = readJsonFromFile("get-submission-200.json");
    return builder
        .given("a submission exists")
        .uponReceiving("a request for a submission")
        .path("/api/v0/submissions/" + SUBMISSION_ID)
        .method("GET")
        .willRespondWith()
        .status(200)
        .headers(Map.of("Content-Type", "application/json"))
        .body(submissionResponse)
        .toPact();
  }


  @Test
  @DisplayName("Verify get submission")
  void testGetSubmission() {
    SubmissionResponse submission = dataClaimsRestClient.getSubmission(SUBMISSION_ID).block();

    assertThat(submission.getSubmissionId()).isEqualTo(SUBMISSION_ID);
  }


  protected static String readJsonFromFile(final String fileName) throws Exception {
    Path path = Paths.get("src/pactTest/resources/responses/", fileName);
    return Files.readString(path);
  }
}
