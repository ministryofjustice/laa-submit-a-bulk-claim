package uk.gov.justice.laa.bulkclaim.service.claims;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockserver.model.HttpResponse.response;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
import uk.gov.justice.laa.bulkclaim.response.SubmissionSearchResponseDto;
import uk.gov.justice.laa.claims.model.ClaimFields;
import uk.gov.justice.laa.claims.model.ClaimValidationError;
import uk.gov.justice.laa.claims.model.CreateBulkSubmission201Response;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;
import uk.gov.justice.laa.claims.model.MatterStartsGet;

/**
 * Integration tests for the {@link DataClaimsRestService}.
 *
 * @author Jamie Briggs
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcTestConfig.class)
class DataClaimsRestServiceIntegrationTest extends MockServerIntegrationTest {

  protected DataClaimsRestService dataClaimsRestService;

  @BeforeEach
  void setUp() {
    dataClaimsRestService = createClient(DataClaimsRestService.class);
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
                "submission_ids": [
                  "aca8d879-3dd4-4fd1-97ee-03f0d0cfd5db"
                ]
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
      Mono<ResponseEntity<CreateBulkSubmission201Response>> upload =
          dataClaimsRestService.upload(file);
      ResponseEntity<CreateBulkSubmission201Response> block = upload.block();
      CreateBulkSubmission201Response result = block.getBody();
      String locationHeader = block.getHeaders().getFirst(HttpHeaders.LOCATION);

      // Then
      assertThat(result.getBulkSubmissionId())
          .isEqualTo(UUID.fromString("f7ed1cda-692e-417a-bb55-5a5135006774"));
      assertThat(result.getSubmissionIds().get(0))
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
      assertThrows(BadRequest.class, () -> dataClaimsRestService.upload(file).block());
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
      assertThrows(Unauthorized.class, () -> dataClaimsRestService.upload(file).block());
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
      assertThrows(Forbidden.class, () -> dataClaimsRestService.upload(file).block());
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
      assertThrows(InternalServerError.class, () -> dataClaimsRestService.upload(file).block());
    }
  }

  @Nested
  @DisplayName("GET: /api/v0/submissions")
  class GetSubmissionsSearch {
    @Test
    @DisplayName("Should return 200 and collection of submissions result")
    void shouldReturn200WithSubmissionCollectionResults() {
      String submissionsBody =
          """
          {
            "submissionId": "660e8400-e29b-41d4-a716-14618440000",
            "officeAccountNumber": "9Z876X",
            "status": "PROCESSED",
            "areaOfLaw": "CIVIL",
            "submitted": "2025-08-21"
          },
          {
            "submissionId": "770e8400-e29b-41d4-a716-42892598452",
            "officeAccountNumber": "9Z876X",
            "status": "PROCESSED",
            "areaOfLaw": "CIVIL",
            "submitted": "2025-08-22"
          }
          """;
      mockServerClient
          .when(HttpRequest.request().withMethod("GET").withPath("/api/v0/submissions"))
          .respond(
              response()
                  .withStatusCode(200)
                  .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                  .withBody("{\"submissions\": [" + submissionsBody + "]}"));

      List<String> offices = List.of("1");
      String submissionId = "1234";
      LocalDate from = LocalDate.of(2025, 8, 1);
      LocalDate to = LocalDate.of(2025, 8, 31);

      SubmissionSearchResponseDto response =
          dataClaimsRestService.search(offices, submissionId, from, to).block(Duration.ofSeconds(2));
      assertThat(response.toString()).isNotEmpty();
      assertThat(response.submissions().getFirst().submissionId())
          .isEqualTo("660e8400-e29b-41d4-a716-14618440000");
    }
  }

  @Nested
  @DisplayName("GET: /api/v0/submission/{submissionId}")
  class GetSubmission {

    @Test
    @DisplayName("Should handle a 200 response")
    void shouldHandle200Response() throws Exception {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      String expectJson = readJsonFromFile("/GetSubmission200.json");
      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod("GET")
                  .withPath("/api/v0/submissions/" + submissionId))
          .respond(
              response()
                  .withStatusCode(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(expectJson));
      // Then
      GetSubmission200Response block = dataClaimsRestService.getSubmission(submissionId).block();
      String result = objectMapper.writeValueAsString(block);
      assertThatJsonMatches(expectJson, result);
    }

    @Test
    @DisplayName("Should handle a 400 response")
    void shouldHandle400Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod("GET")
                  .withPath("/api/v0/submissions/" + submissionId))
          .respond(response().withStatusCode(400).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          BadRequest.class, () -> dataClaimsRestService.getSubmission(submissionId).block());
    }

    @Test
    @DisplayName("Should handle a 401 response")
    void shouldHandle401Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod("GET")
                  .withPath("/api/v0/submissions/" + submissionId))
          .respond(response().withStatusCode(401).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          Unauthorized.class, () -> dataClaimsRestService.getSubmission(submissionId).block());
    }

    @Test
    @DisplayName("Should handle a 403 response")
    void shouldHandle403Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod("GET")
                  .withPath("/api/v0/submissions/" + submissionId))
          .respond(response().withStatusCode(403).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          Forbidden.class, () -> dataClaimsRestService.getSubmission(submissionId).block());
    }

    @Test
    @DisplayName("Should handle a 404 response")
    void shouldHandle404Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod("GET")
                  .withPath("/api/v0/submissions/" + submissionId))
          .respond(response().withStatusCode(404).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(NotFound.class, () -> dataClaimsRestService.getSubmission(submissionId).block());
    }

    @Test
    @DisplayName("Should handle a 500 response")
    void shouldHandle500Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod("GET")
                  .withPath("/api/v0/submissions/" + submissionId))
          .respond(response().withStatusCode(500).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          InternalServerError.class,
          () -> dataClaimsRestService.getSubmission(submissionId).block());
    }
  }

  @Nested
  @DisplayName("GET: /api/v0/submission/{submission-id}/claims/{claim-id}")
  class GetSubmissionClaim {

    @Test
    @DisplayName("Should handle a 200 response")
    void shouldHandle200Response() throws Exception {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      UUID claimId = UUID.fromString("f75578dc-add2-4fe1-80c4-4b9e8c3c523b");
      String expectJson = readJsonFromFile("/GetClaim200.json");
      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod("GET")
                  .withPath("/api/v0/submissions/" + submissionId + "/claims/" + claimId))
          .respond(
              response()
                  .withStatusCode(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(expectJson));
      // Then
      ClaimFields block = dataClaimsRestService.getSubmissionClaim(submissionId, claimId).block();
      String result = objectMapper.writeValueAsString(block);
      assertThatJsonMatches(expectJson, result);
    }

    @Test
    @DisplayName("Should handle a 400 response")
    void shouldHandle400Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      UUID claimId = UUID.fromString("f75578dc-add2-4fe1-80c4-4b9e8c3c523b");
      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod("GET")
                  .withPath("/api/v0/submissions/" + submissionId + "/claims/" + claimId))
          .respond(response().withStatusCode(400).withHeader("Content-Type", "application/json"));
      // When
      assertThrows(
          BadRequest.class,
          () -> dataClaimsRestService.getSubmissionClaim(submissionId, claimId).block());
    }

    @Test
    @DisplayName("Should handle a 401 response")
    void shouldHandle401Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      UUID claimId = UUID.fromString("f75578dc-add2-4fe1-80c4-4b9e8c3c523b");
      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod("GET")
                  .withPath("/api/v0/submissions/" + submissionId + "/claims/" + claimId))
          .respond(response().withStatusCode(401).withHeader("Content-Type", "application/json"));
      // When
      assertThrows(
          Unauthorized.class,
          () -> dataClaimsRestService.getSubmissionClaim(submissionId, claimId).block());
    }

    @Test
    @DisplayName("Should handle a 403 response")
    void shouldHandle403Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      UUID claimId = UUID.fromString("f75578dc-add2-4fe1-80c4-4b9e8c3c523b");
      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod("GET")
                  .withPath("/api/v0/submissions/" + submissionId + "/claims/" + claimId))
          .respond(response().withStatusCode(403).withHeader("Content-Type", "application/json"));
      // When
      assertThrows(
          Forbidden.class,
          () -> dataClaimsRestService.getSubmissionClaim(submissionId, claimId).block());
    }

    @Test
    @DisplayName("Should handle a 404 response")
    void shouldHandle404Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      UUID claimId = UUID.fromString("f75578dc-add2-4fe1-80c4-4b9e8c3c523b");
      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod("GET")
                  .withPath("/api/v0/submissions/" + submissionId + "/claims/" + claimId))
          .respond(response().withStatusCode(404).withHeader("Content-Type", "application/json"));
      // When
      assertThrows(
          NotFound.class,
          () -> dataClaimsRestService.getSubmissionClaim(submissionId, claimId).block());
    }

    @Test
    @DisplayName("Should handle a 500 response")
    void shouldHandle500Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      UUID claimId = UUID.fromString("f75578dc-add2-4fe1-80c4-4b9e8c3c523b");
      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod("GET")
                  .withPath("/api/v0/submissions/" + submissionId + "/claims/" + claimId))
          .respond(response().withStatusCode(500).withHeader("Content-Type", "application/json"));
      // When
      assertThrows(
          InternalServerError.class,
          () -> dataClaimsRestService.getSubmissionClaim(submissionId, claimId).block());
    }
  }

  @Nested
  @DisplayName("GET: /api/v0/submission/{submission-id}/matter-starts/{matter-starts-id}")
  class GetSubmissionMatterStarts {

    @Test
    @DisplayName("Should handle a 200 response")
    void shouldHandle200Response() throws Exception {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      UUID matterStartsId = UUID.fromString("f75578dc-add2-4fe1-80c4-4b9e8c3c523b");
      String expectJson = readJsonFromFile("/GetMatterStarts200.json");
      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod("GET")
                  .withPath(
                      "/api/v0/submissions/" + submissionId + "/matter-starts/" + matterStartsId))
          .respond(
              response()
                  .withStatusCode(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(expectJson));
      // Then
      MatterStartsGet block =
          dataClaimsRestService.getSubmissionMatterStarts(submissionId, matterStartsId).block();
      String result = objectMapper.writeValueAsString(block);
      assertThatJsonMatches(expectJson, result);
    }

    @Test
    @DisplayName("Should handle a 400 response")
    void shouldHandle400Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      UUID matterStartsId = UUID.fromString("f75578dc-add2-4fe1-80c4-4b9e8c3c523b");
      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod("GET")
                  .withPath(
                      "/api/v0/submissions/" + submissionId + "/matter-starts/" + matterStartsId))
          .respond(response().withStatusCode(400).withHeader("Content-Type", "application/json"));
      // When
      assertThrows(
          BadRequest.class,
          () ->
              dataClaimsRestService
                  .getSubmissionMatterStarts(submissionId, matterStartsId)
                  .block());
    }

    @Test
    @DisplayName("Should handle a 401 response")
    void shouldHandle401Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      UUID matterStartsId = UUID.fromString("f75578dc-add2-4fe1-80c4-4b9e8c3c523b");
      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod("GET")
                  .withPath("/api/v0/submissions/" + submissionId + "/claims/" + matterStartsId))
          .respond(response().withStatusCode(401).withHeader("Content-Type", "application/json"));
      // When
      assertThrows(
          Unauthorized.class,
          () -> dataClaimsRestService.getSubmissionClaim(submissionId, matterStartsId).block());
    }

    @Test
    @DisplayName("Should handle a 403 response")
    void shouldHandle403Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      UUID matterStartsId = UUID.fromString("f75578dc-add2-4fe1-80c4-4b9e8c3c523b");
      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod("GET")
                  .withPath(
                      "/api/v0/submissions/" + submissionId + "/matter-starts/" + matterStartsId))
          .respond(response().withStatusCode(403).withHeader("Content-Type", "application/json"));
      // When
      assertThrows(
          Forbidden.class,
          () ->
              dataClaimsRestService
                  .getSubmissionMatterStarts(submissionId, matterStartsId)
                  .block());
    }

    @Test
    @DisplayName("Should handle a 404 response")
    void shouldHandle404Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      UUID claimId = UUID.fromString("f75578dc-add2-4fe1-80c4-4b9e8c3c523b");
      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod("GET")
                  .withPath("/api/v0/submissions/" + submissionId + "/claims/" + claimId))
          .respond(response().withStatusCode(404).withHeader("Content-Type", "application/json"));
      // When
      assertThrows(
          NotFound.class,
          () -> dataClaimsRestService.getSubmissionClaim(submissionId, claimId).block());
    }

    @Test
    @DisplayName("Should handle a 500 response")
    void shouldHandle500Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      UUID matterStartsId = UUID.fromString("f75578dc-add2-4fe1-80c4-4b9e8c3c523b");
      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod("GET")
                  .withPath(
                      "/api/v0/submissions/" + submissionId + "/matter-starts/" + matterStartsId))
          .respond(response().withStatusCode(500).withHeader("Content-Type", "application/json"));
      // When
      assertThrows(
          InternalServerError.class,
          () ->
              dataClaimsRestService
                  .getSubmissionMatterStarts(submissionId, matterStartsId)
                  .block());
    }
  }

  @Nested
  @DisplayName("GET: /api/v0/validation-errors")
  class GetValidationErrors {

    @Test
    @DisplayName("Should handle 200 response with one error")
    void shouldHandle200ResponseWithOneError() throws Exception {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      String expectJson = readJsonFromFile("/GetValidationError200.json");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET").withPath("/api/v0/validation-errors"))
          .respond(
              response()
                  .withStatusCode(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(expectJson));
      // Then
      List<ClaimValidationError> block =
          dataClaimsRestService.getValidationErrors(submissionId).block();
      String result = objectMapper.writeValueAsString(block);
      assertThatJsonMatches(expectJson, result);
    }

    @Test
    @DisplayName("Should handle 200 response with multiple errors")
    void shouldHandle200ResponseWithMultipleErrors() throws Exception {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      String expectJson = readJsonFromFile("/GetValidationErrors200.json");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET").withPath("/api/v0/validation-errors"))
          .respond(
              response()
                  .withStatusCode(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(expectJson));
      // Then
      List<ClaimValidationError> block =
          dataClaimsRestService.getValidationErrors(submissionId).block();
      String result = objectMapper.writeValueAsString(block);
      assertThatJsonMatches(expectJson, result);
    }

    @Test
    @DisplayName("Should handle a 400 response")
    void shouldHandle400Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET").withPath("/api/v0/validation-errors"))
          .respond(response().withStatusCode(400).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          BadRequest.class, () -> dataClaimsRestService.getValidationErrors(submissionId).block());
    }

    @Test
    @DisplayName("Should handle a 401 response")
    void shouldHandle401Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET").withPath("/api/v0/validation-errors"))
          .respond(response().withStatusCode(401).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          Unauthorized.class,
          () -> dataClaimsRestService.getValidationErrors(submissionId).block());
    }

    @Test
    @DisplayName("Should handle a 403 response")
    void shouldHandle403Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET").withPath("/api/v0/validation-errors"))
          .respond(response().withStatusCode(403).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          Forbidden.class, () -> dataClaimsRestService.getValidationErrors(submissionId).block());
    }

    @Test
    @DisplayName("Should handle a 404 response")
    void shouldHandle404Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET").withPath("/api/v0/validation-errors"))
          .respond(response().withStatusCode(404).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          NotFound.class, () -> dataClaimsRestService.getValidationErrors(submissionId).block());
    }

    @Test
    @DisplayName("Should handle a 500 response")
    void shouldHandle500Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET").withPath("/api/v0/validation-errors"))
          .respond(response().withStatusCode(500).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          InternalServerError.class,
          () -> dataClaimsRestService.getValidationErrors(submissionId).block());
    }
  }
}
