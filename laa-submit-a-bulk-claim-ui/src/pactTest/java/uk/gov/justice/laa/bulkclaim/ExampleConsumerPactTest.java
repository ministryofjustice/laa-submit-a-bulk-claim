package uk.gov.justice.laa.bulkclaim;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import au.com.dius.pact.consumer.dsl.PactBuilder;
import au.com.dius.pact.consumer.junit5.PactConsumerTest;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.ClaimsApiPactTestConfig;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PactConsumerTest
@PactTestFor(providerName = "laa-data-claim-api")
@Import(ClaimsApiPactTestConfig.class)
public class ExampleConsumerPactTest {

  final UUID SUBMISSION_ID = UUID.fromString("12345678-1234-1234-1234-123456789012");

  @Autowired
  DataClaimsRestClient dataClaimsRestClient;

  @MockitoBean
  OAuth2AuthorizedClientManager authorizedClientManager;

  @SneakyThrows
  @Pact(consumer = "submission-consumer")
  public V4Pact getSubmission(PactBuilder builder) {
    String submissionResponse = readJsonFromFile("get-submission-200.json");

    return builder
        .comment("V4 Pact using expectsToReceiveHttpInteraction")
        .given("A submission exists")
        .expectsToReceiveHttpInteraction(
            "a request for a submission", http -> http
                .withRequest(request -> request
                    .path("/api/v0/submissions/" + SUBMISSION_ID)
                    .method("GET"))
                .willRespondWith(response -> response
                    .status(200)
                    .body(submissionResponse))
        )
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
