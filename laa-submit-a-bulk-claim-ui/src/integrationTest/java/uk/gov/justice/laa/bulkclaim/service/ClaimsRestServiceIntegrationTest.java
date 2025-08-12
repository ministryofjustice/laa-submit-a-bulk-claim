package uk.gov.justice.laa.bulkclaim.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockserver.model.HttpResponse.response;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties.Http;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException.Forbidden;
import org.springframework.web.reactive.function.client.WebClientResponseException.InternalServerError;
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;
import org.springframework.web.reactive.function.client.WebClientResponseException.Unauthorized;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.helper.MockServerIntegrationTest;
import uk.gov.justice.laa.claims.model.CreateBulkSubmission201Response;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;

/**
 * Integration tests for the {@link ClaimsRestService}.
 *
 * @author Jamie Briggs
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcTestConfig.class)
class ClaimsRestServiceIntegrationTest extends MockServerIntegrationTest {

  protected ClaimsRestService claimsRestService;

  @BeforeEach
  void setUp() {
    claimsRestService = createClient(ClaimsRestService.class);
  }

  @Nested
  @DisplayName("GET: /api/v0/bulk-submissions")
  class PostBulkSubmission {

    @Test
    @DisplayName("Should handle a 201 response")
    void shouldHandle201Response() {
      // Given
      MockMultipartFile file =
          new MockMultipartFile("file", "test.txt", "text/plain", new byte[10 * 1024 * 1024]);
      String expectedBody =
          """
          {
            "bulk_submission_id": "f7ed1cda-692e-417a-bb55-5a5135006774",
            "submission_id": "aca8d879-3dd4-4fd1-97ee-03f0d0cfd5db"
          }
          """;
      mockServerClient
          .when(HttpRequest.request().withMethod("POST").withPath("/api/v0/bulk-submissions"))
          .respond(
              response()
                  .withStatusCode(201)
                  .withHeader("Content-Type", "application/json")
                  .withHeader("Location", "/api/v0/bulk-submissions/1234567890")
                  .withBody(expectedBody));

      // When
      Mono<ResponseEntity<CreateBulkSubmission201Response>> upload = claimsRestService.upload(file);
      ResponseEntity<CreateBulkSubmission201Response> block = upload.block();
      CreateBulkSubmission201Response result = block.getBody();
      String locationHeader = block.getHeaders().getFirst(HttpHeaders.LOCATION);

      // Then
      assertThat(result.getBulkSubmissionId())
          .isEqualTo(UUID.fromString("f7ed1cda-692e-417a-bb55-5a5135006774"));
      assertThat(result.getSubmissionId())
          .isEqualTo(UUID.fromString("aca8d879-3dd4-4fd1-97ee-03f0d0cfd5db"));
      assertThat(locationHeader).isEqualTo("/api/v0/bulk-submissions/1234567890");
    }

    @Test
    @DisplayName("Should handle a 400 response")
    void shouldHandle400Response() {
      // Given
      MockMultipartFile file =
          new MockMultipartFile("file", "test.txt", "text/plain", new byte[10 * 1024 * 1024]);
      mockServerClient
          .when(HttpRequest.request().withMethod("POST").withPath("/api/v0/bulk-submissions"))
          .respond(response().withStatusCode(400).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(BadRequest.class, () -> claimsRestService.upload(file).block());
    }

    @Test
    @DisplayName("Should handle a 401 response")
    void shouldHandle401Response() {
      // Given
      MockMultipartFile file =
          new MockMultipartFile("file", "test.txt", "text/plain", new byte[10 * 1024 * 1024]);
      mockServerClient
          .when(HttpRequest.request().withMethod("POST").withPath("/api/v0/bulk-submissions"))
          .respond(response().withStatusCode(401).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(Unauthorized.class, () -> claimsRestService.upload(file).block());
    }

    @Test
    @DisplayName("Should handle a 403 response")
    void shouldHandle403Response() {
      // Given
      MockMultipartFile file =
          new MockMultipartFile("file", "test.txt", "text/plain", new byte[10 * 1024 * 1024]);
      mockServerClient
          .when(HttpRequest.request().withMethod("POST").withPath("/api/v0/bulk-submissions"))
          .respond(response().withStatusCode(403).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(Forbidden.class, () -> claimsRestService.upload(file).block());
    }

    @Test
    @DisplayName("Should handle a 500 response")
    void shouldHandle500Response() {
      // Given
      MockMultipartFile file =
          new MockMultipartFile("file", "test.txt", "text/plain", new byte[10 * 1024 * 1024]);
      mockServerClient
          .when(HttpRequest.request().withMethod("POST").withPath("/api/v0/bulk-submissions"))
          .respond(response().withStatusCode(500).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(InternalServerError.class, () -> claimsRestService.upload(file).block());
    }
  }

  @Nested
  @DisplayName("GET: /api/v0/submission/{submissionId}")
  class GetSubmission{

    @Test
    @DisplayName("Should handle a 200 response")
    void shouldHandle200Response() throws Exception {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      String expectJson = readJsonFromFile("/GetSubmission200.json");
      mockServerClient.when(HttpRequest.request().withMethod("GET")
          .withPath("/api/v0/submissions/" + submissionId))
          .respond(response().withStatusCode(200).withHeader("Content-Type", "application/json")
              .withBody(expectJson));
      // Then
      GetSubmission200Response block = claimsRestService.getSubmission(submissionId).block();
      String result = objectMapper.writeValueAsString(block);
      assertThatJsonMatches(expectJson, result);
    }

    @Test
    @DisplayName("Should handle a 400 response")
    void shouldHandle400Response(){
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET")
              .withPath("/api/v0/submissions/" + submissionId))
          .respond(response().withStatusCode(400).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(BadRequest.class, () -> claimsRestService.getSubmission(submissionId).block());
    }

    @Test
    @DisplayName("Should handle a 401 response")
    void shouldHandle401Response(){
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET")
              .withPath("/api/v0/submissions/" + submissionId))
          .respond(response().withStatusCode(401).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(Unauthorized.class, () -> claimsRestService.getSubmission(submissionId).block());
    }

    @Test
    @DisplayName("Should handle a 403 response")
    void shouldHandle403Response(){
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET")
              .withPath("/api/v0/submissions/" + submissionId))
          .respond(response().withStatusCode(403).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(Forbidden.class, () -> claimsRestService.getSubmission(submissionId).block());
    }

    @Test
    @DisplayName("Should handle a 404 response")
    void shouldHandle404Response(){
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET")
              .withPath("/api/v0/submissions/" + submissionId))
          .respond(response().withStatusCode(404).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(NotFound.class, () -> claimsRestService.getSubmission(submissionId).block());
    }

    @Test
    @DisplayName("Should handle a 500 response")
    void shouldHandle500Response(){
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET")
              .withPath("/api/v0/submissions/" + submissionId))
          .respond(response().withStatusCode(500).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(InternalServerError.class, () -> claimsRestService.getSubmission(submissionId).block());
    }
  }
}
