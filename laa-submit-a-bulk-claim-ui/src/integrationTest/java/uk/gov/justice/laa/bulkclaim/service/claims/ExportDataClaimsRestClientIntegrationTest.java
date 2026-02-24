package uk.gov.justice.laa.bulkclaim.service.claims;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import uk.gov.justice.laa.bulkclaim.client.ExportDataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.helper.MockServerIntegrationTest;

/**
 * Integration tests for the {@link ExportDataClaimsRestClient}.
 *
 * @author Jamie Briggs
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcTestConfig.class)
class ExportDataClaimsRestClientIntegrationTest extends MockServerIntegrationTest {

  protected ExportDataClaimsRestClient exportDataClaimsRestClient;

  @BeforeEach
  void setUp() {
    exportDataClaimsRestClient = createClient(ExportDataClaimsRestClient.class);
  }

  @Nested
  @DisplayName("GET: /api/v1/submissions/{submissionId}/export")
  class GetSubmissionExport {

    @Test
    @DisplayName("Should handle a 200 response")
    void shouldHandle200Response() {
      // Given
      byte[] csvContent = "one,two,three".getBytes(StandardCharsets.UTF_8);
      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod("GET")
                  .withPath("/exports/submission_claims_legal_help.csv"))
          .respond(
              HttpResponse.response()
                  .withStatusCode(200)
                  .withHeader("Content-Type", "text/csv")
                  .withBody(csvContent));
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      // When
      ResponseEntity<byte[]> result =
          exportDataClaimsRestClient.getSubmissionExport("legal_help", submissionId, "123").block();
      // Then
      assertThat(result).isNotNull();
      assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
      assertThat(result.getBody()).isEqualTo(csvContent);
    }
  }
}
