package uk.gov.justice.laa.bulkclaim.service.claims;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockserver.model.HttpResponse.response;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException.Forbidden;
import org.springframework.web.reactive.function.client.WebClientResponseException.InternalServerError;
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;
import org.springframework.web.reactive.function.client.WebClientResponseException.Unauthorized;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.helper.MockServerIntegrationTest;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.*;

/**
 * Integration tests for the {@link uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient}.
 *
 * @author Jamie Briggs
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcTestConfig.class)
class DataClaimsRestClientIntegrationTest extends MockServerIntegrationTest {
  private static final String GET_ALL_MATTER_STARTS_URI = "/api/v1/submissions/%s/matter-starts";

  protected DataClaimsRestClient dataClaimsRestClient;

  @BeforeEach
  void setUp() {
    dataClaimsRestClient = createClient(DataClaimsRestClient.class);
  }

  @Nested
  @DisplayName("POST: /api/v1/bulk-submissions")
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
          .when(HttpRequest.request().withMethod("POST").withPath("/api/v1/bulk-submissions"))
          .respond(
              response()
                  .withStatusCode(201)
                  .withHeader("Content-Type", "application/json")
                  .withHeader("Location", "/api/v1/bulk-submissions/1234567890")
                  .withBody(expectedBody));

      // When
      Mono<ResponseEntity<CreateBulkSubmission201Response>> upload =
          dataClaimsRestClient.upload(file, "test-user", List.of("ABC123"));
      ResponseEntity<CreateBulkSubmission201Response> block = upload.block();
      CreateBulkSubmission201Response result = block.getBody();
      String locationHeader = block.getHeaders().getFirst(HttpHeaders.LOCATION);

      // Then
      assertThat(result.getBulkSubmissionId())
          .isEqualTo(UUID.fromString("f7ed1cda-692e-417a-bb55-5a5135006774"));
      assertThat(result.getSubmissionIds().get(0))
          .isEqualTo(UUID.fromString("aca8d879-3dd4-4fd1-97ee-03f0d0cfd5db"));
      assertThat(locationHeader).isEqualTo("/api/v1/bulk-submissions/1234567890");
    }

    @Test
    @DisplayName("Should handle a 400 response")
    void shouldHandle400Response() {
      // Given
      MockMultipartFile file =
          new MockMultipartFile("file", "test.txt", "text/plain", new byte[10 * 1024 * 1024]);
      mockServerClient
          .when(HttpRequest.request().withMethod("POST").withPath("/api/v1/bulk-submissions"))
          .respond(response().withStatusCode(400).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          BadRequest.class,
          () -> dataClaimsRestClient.upload(file, "test-user", List.of("ABC123")).block());
    }

    @Test
    @DisplayName("Should handle a 401 response")
    void shouldHandle401Response() {
      // Given
      MockMultipartFile file =
          new MockMultipartFile("file", "test.txt", "text/plain", new byte[10 * 1024 * 1024]);
      mockServerClient
          .when(HttpRequest.request().withMethod("POST").withPath("/api/v1/bulk-submissions"))
          .respond(response().withStatusCode(401).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          Unauthorized.class,
          () -> dataClaimsRestClient.upload(file, "test-user", List.of("ABC123")).block());
    }

    @Test
    @DisplayName("Should handle a 403 response")
    void shouldHandle403Response() {
      // Given
      MockMultipartFile file =
          new MockMultipartFile("file", "test.txt", "text/plain", new byte[10 * 1024 * 1024]);
      mockServerClient
          .when(HttpRequest.request().withMethod("POST").withPath("/api/v1/bulk-submissions"))
          .respond(response().withStatusCode(403).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          Forbidden.class,
          () -> dataClaimsRestClient.upload(file, "test-user", List.of("ABC123")).block());
    }

    @Test
    @DisplayName("Should handle a 500 response")
    void shouldHandle500Response() {
      // Given
      MockMultipartFile file =
          new MockMultipartFile("file", "test.txt", "text/plain", new byte[10 * 1024 * 1024]);
      mockServerClient
          .when(HttpRequest.request().withMethod("POST").withPath("/api/v1/bulk-submissions"))
          .respond(response().withStatusCode(500).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          InternalServerError.class,
          () -> dataClaimsRestClient.upload(file, "test-user", List.of("ABC123")).block());
    }
  }

  @Nested
  @DisplayName("GET: /api/v1/submissions")
  class GetSubmissionsSearch {
    @Test
    @DisplayName("Should return 200 and collection of submissions result")
    void shouldReturn200WithSubmissionCollectionResults() {
      String expectedSubmissionId = "660e8400-e29b-41d4-a716-2c963f66afa6";
      String submissionsBody =
          """
          {
            "submission_id": "660e8400-e29b-41d4-a716-2c963f66afa6",
            "office_account_number": "9Z876X",
            "status": "CREATED",
            "area_of_law": "LEGAL HELP",
            "submitted": "2019-08-24T14:15:22Z"
          },
          {
            "submission_id": "770e8400-e29b-41d4-a716-2c963f66afa6",
            "office_account_number": "9Z876X",
            "status": "VALIDATION_SUCCEEDED",
            "area_of_law": "LEGAL HELP",
            "submitted": "2019-08-25T15:17:22Z"
          }
          """;
      mockServerClient
          .when(HttpRequest.request().withMethod("GET").withPath("/api/v1/submissions"))
          .respond(
              response()
                  .withStatusCode(200)
                  .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                  .withBody("{\"content\": [" + submissionsBody + "]}"));

      List<String> offices = List.of("1");
      String submissionPeriod = "JAN-2025";
      AreaOfLaw areaOfLaw = AreaOfLaw.LEGAL_HELP;
      SubmissionStatus status = SubmissionStatus.VALIDATION_SUCCEEDED;

      SubmissionsResultSet response =
          dataClaimsRestClient
              .search(offices, submissionPeriod, areaOfLaw, Arrays.asList(status), 0, 10, null)
              .block(Duration.ofSeconds(2));
      assertThat(response.toString()).isNotEmpty();
      assertThat(response.getContent().getFirst().getSubmissionId().toString())
          .isEqualTo(expectedSubmissionId);
    }
  }

  @Nested
  @DisplayName("GET: /api/v1/submissions/{submissionId}")
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
                  .withPath("/api/v1/submissions/" + submissionId))
          .respond(
              response()
                  .withStatusCode(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(expectJson));
      // Then
      SubmissionResponse block = dataClaimsRestClient.getSubmission(submissionId).block();
      String result = objectMapper.writeValueAsString(block);
      assertThatJsonMatches(expectJson, result);
    }

    @Test
    @DisplayName("Should handle a 400 response")
    void shouldHandle400Response() {
      // Given
      UUID submissionsId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod("GET")
                  .withPath("/api/v1/submissions/" + submissionsId))
          .respond(response().withStatusCode(400).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          BadRequest.class, () -> dataClaimsRestClient.getSubmission(submissionsId).block());
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
                  .withPath("/api/v1/submissions/" + submissionId))
          .respond(response().withStatusCode(401).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          Unauthorized.class, () -> dataClaimsRestClient.getSubmission(submissionId).block());
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
                  .withPath("/api/v1/submissions/" + submissionId))
          .respond(response().withStatusCode(403).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(Forbidden.class, () -> dataClaimsRestClient.getSubmission(submissionId).block());
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
                  .withPath("/api/v1/submissions/" + submissionId))
          .respond(response().withStatusCode(404).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(NotFound.class, () -> dataClaimsRestClient.getSubmission(submissionId).block());
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
                  .withPath("/api/v1/submissions/" + submissionId))
          .respond(response().withStatusCode(500).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          InternalServerError.class,
          () -> dataClaimsRestClient.getSubmission(submissionId).block());
    }
  }

  @Nested
  @DisplayName("GET: /api/v1/claims")
  class GetClaims {

    @Test
    @DisplayName("Should handle a 200 response")
    void shouldHandle200Response() throws Exception {
      // Given
      UUID claimId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      String expectJson = readJsonFromFile("/GetClaims200.json");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET").withPath("/api/v1/claims"))
          .respond(
              response()
                  .withStatusCode(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(expectJson));
      // Then
      ClaimResultSet block = dataClaimsRestClient.getClaims("0P322F", claimId, 0, 1).getBody();
      String result = objectMapper.writeValueAsString(block);
      assertThatJsonMatches(expectJson, result);
    }

    @Test
    @DisplayName("Should handle a 200 response with sort")
    void shouldHandle200ResponseWithSort() throws Exception {
      // Given
      UUID claimId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      String expectJson = readJsonFromFile("/GetClaims200.json");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET").withPath("/api/v1/claims"))
          .respond(
              response()
                  .withStatusCode(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(expectJson));
      // Then
      ClaimResultSet block =
          dataClaimsRestClient.getClaims("0P322F", claimId, 0, 1, "lineNumber,asc").getBody();
      String result = objectMapper.writeValueAsString(block);
      assertThatJsonMatches(expectJson, result);
    }

    @Test
    @DisplayName("Should handle a 400 response")
    void shouldHandle400Response() {
      // Given
      UUID claimId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET").withPath("/api/v1/claims"))
          .respond(response().withStatusCode(400).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(BadRequest.class, () -> dataClaimsRestClient.getClaims("0P322F", claimId, 0, 1));
    }

    @Test
    @DisplayName("Should handle a 401 response")
    void shouldHandle401Response() {
      // Given
      UUID claimId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET").withPath("/api/v1/claims"))
          .respond(response().withStatusCode(401).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          Unauthorized.class, () -> dataClaimsRestClient.getClaims("0P322F", claimId, 0, 1));
    }

    @Test
    @DisplayName("Should handle a 403 response")
    void shouldHandle403Response() {
      // Given
      UUID claimId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET").withPath("/api/v1/claims"))
          .respond(response().withStatusCode(403).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(Forbidden.class, () -> dataClaimsRestClient.getClaims("0P322F", claimId, 0, 1));
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
                  .withPath("/api/v1/submissions/" + submissionId))
          .respond(response().withStatusCode(404).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(NotFound.class, () -> dataClaimsRestClient.getSubmission(submissionId).block());
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
                  .withPath("/api/v1/submissions/" + submissionId))
          .respond(response().withStatusCode(500).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          InternalServerError.class,
          () -> dataClaimsRestClient.getSubmission(submissionId).block());
    }
  }

  @Nested
  @DisplayName("GET: /api/v1/submission/{submission-id}/claims/{claim-id}")
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
                  .withPath("/api/v1/submissions/" + submissionId + "/claims/" + claimId))
          .respond(
              response()
                  .withStatusCode(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(expectJson));
      // Then
      ClaimResponse block = dataClaimsRestClient.getSubmissionClaim(submissionId, claimId).block();
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
                  .withPath("/api/v1/submissions/" + submissionId + "/claims/" + claimId))
          .respond(response().withStatusCode(400).withHeader("Content-Type", "application/json"));
      // When
      assertThrows(
          BadRequest.class,
          () -> dataClaimsRestClient.getSubmissionClaim(submissionId, claimId).block());
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
                  .withPath("/api/v1/submissions/" + submissionId + "/claims/" + claimId))
          .respond(response().withStatusCode(401).withHeader("Content-Type", "application/json"));
      // When
      assertThrows(
          Unauthorized.class,
          () -> dataClaimsRestClient.getSubmissionClaim(submissionId, claimId).block());
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
                  .withPath("/api/v1/submissions/" + submissionId + "/claims/" + claimId))
          .respond(response().withStatusCode(403).withHeader("Content-Type", "application/json"));
      // When
      assertThrows(
          Forbidden.class,
          () -> dataClaimsRestClient.getSubmissionClaim(submissionId, claimId).block());
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
                  .withPath("/api/v1/submissions/" + submissionId + "/claims/" + claimId))
          .respond(response().withStatusCode(404).withHeader("Content-Type", "application/json"));
      // When
      assertThrows(
          NotFound.class,
          () -> dataClaimsRestClient.getSubmissionClaim(submissionId, claimId).block());
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
                  .withPath("/api/v1/submissions/" + submissionId + "/claims/" + claimId))
          .respond(response().withStatusCode(500).withHeader("Content-Type", "application/json"));
      // When
      assertThrows(
          InternalServerError.class,
          () -> dataClaimsRestClient.getSubmissionClaim(submissionId, claimId).block());
    }
  }

  @Nested
  @DisplayName("GET: /api/v1/submission/{submission-id}/matter-starts/{matter-starts-id}")
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
                      "/api/v1/submissions/" + submissionId + "/matter-starts/" + matterStartsId))
          .respond(
              response()
                  .withStatusCode(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(expectJson));
      // Then
      MatterStartGet block =
          dataClaimsRestClient.getSubmissionMatterStart(submissionId, matterStartsId).block();
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
                      "/api/v1/submissions/" + submissionId + "/matter-starts/" + matterStartsId))
          .respond(response().withStatusCode(400).withHeader("Content-Type", "application/json"));
      // When
      assertThrows(
          BadRequest.class,
          () ->
              dataClaimsRestClient.getSubmissionMatterStart(submissionId, matterStartsId).block());
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
                  .withPath("/api/v1/submissions/" + submissionId + "/claims/" + matterStartsId))
          .respond(response().withStatusCode(401).withHeader("Content-Type", "application/json"));
      // When
      assertThrows(
          Unauthorized.class,
          () -> dataClaimsRestClient.getSubmissionClaim(submissionId, matterStartsId).block());
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
                      "/api/v1/submissions/" + submissionId + "/matter-starts/" + matterStartsId))
          .respond(response().withStatusCode(403).withHeader("Content-Type", "application/json"));
      // When
      assertThrows(
          Forbidden.class,
          () ->
              dataClaimsRestClient.getSubmissionMatterStart(submissionId, matterStartsId).block());
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
                  .withPath("/api/v1/submissions/" + submissionId + "/claims/" + claimId))
          .respond(response().withStatusCode(404).withHeader("Content-Type", "application/json"));
      // When
      assertThrows(
          NotFound.class,
          () -> dataClaimsRestClient.getSubmissionClaim(submissionId, claimId).block());
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
                      "/api/v1/submissions/" + submissionId + "/matter-starts/" + matterStartsId))
          .respond(response().withStatusCode(500).withHeader("Content-Type", "application/json"));
      // When
      assertThrows(
          InternalServerError.class,
          () ->
              dataClaimsRestClient.getSubmissionMatterStart(submissionId, matterStartsId).block());
    }
  }

  @Nested
  @DisplayName("GET: /api/v1/validation-messages")
  class GetValidationErrors {

    @Test
    @DisplayName("Should handle 200 response with one error")
    void shouldHandle200ResponseWithOneError() throws Exception {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      String expectJson = readJsonFromFile("/GetValidationMessage200.json");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET").withPath("/api/v1/validation-messages"))
          .respond(
              response()
                  .withStatusCode(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(expectJson));
      // Then
      ValidationMessagesResponse block =
          dataClaimsRestClient
              .getValidationMessages(submissionId, null, null, null, null, null)
              .block();
      String result = objectMapper.writeValueAsString(block);
      assertThatJsonMatches(expectJson, result);
    }

    @Test
    @DisplayName("Should handle 200 response with multiple errors")
    void shouldHandle200ResponseWithMultipleErrors() throws Exception {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      String expectJson = readJsonFromFile("/GetValidationMessages200.json");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET").withPath("/api/v1/validation-messages"))
          .respond(
              response()
                  .withStatusCode(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(expectJson));
      // Then
      ValidationMessagesResponse block =
          dataClaimsRestClient
              .getValidationMessages(submissionId, null, null, null, null, null)
              .block();
      String result = objectMapper.writeValueAsString(block);
      assertThatJsonMatches(expectJson, result);
    }

    @Test
    @DisplayName("Should handle a 400 response")
    void shouldHandle400Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET").withPath("/api/v1/validation-messages"))
          .respond(response().withStatusCode(400).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          BadRequest.class,
          () ->
              dataClaimsRestClient
                  .getValidationMessages(submissionId, null, null, null, null, null)
                  .block());
    }

    @Test
    @DisplayName("Should handle a 401 response")
    void shouldHandle401Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET").withPath("/api/v1/validation-messages"))
          .respond(response().withStatusCode(401).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          Unauthorized.class,
          () ->
              dataClaimsRestClient
                  .getValidationMessages(submissionId, null, null, null, null, null)
                  .block());
    }

    @Test
    @DisplayName("Should handle a 403 response")
    void shouldHandle403Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET").withPath("/api/v1/validation-messages"))
          .respond(response().withStatusCode(403).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          Forbidden.class,
          () ->
              dataClaimsRestClient
                  .getValidationMessages(submissionId, null, null, null, null, null)
                  .block());
    }

    @Test
    @DisplayName("Should handle a 404 response")
    void shouldHandle404Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET").withPath("/api/v1/validation-messages"))
          .respond(response().withStatusCode(404).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          NotFound.class,
          () ->
              dataClaimsRestClient
                  .getValidationMessages(submissionId, null, null, null, null, null)
                  .block());
    }

    @Test
    @DisplayName("Should handle a 500 response")
    void shouldHandle500Response() {
      // Given
      UUID submissionId = UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6");
      mockServerClient
          .when(HttpRequest.request().withMethod("GET").withPath("/api/v1/validation-messages"))
          .respond(response().withStatusCode(500).withHeader("Content-Type", "application/json"));

      // When
      assertThrows(
          InternalServerError.class,
          () ->
              dataClaimsRestClient
                  .getValidationMessages(submissionId, null, null, null, null, null)
                  .block());
    }

    @Test
    @DisplayName("Should handle a 200 response")
    void getAllMatterStartsForSubmission_shouldHandle200Response() throws Exception {

      mockServerClient
          .when(
              HttpRequest.request()
                  .withMethod(HttpMethod.GET.toString())
                  .withPath(
                      String.format(
                          GET_ALL_MATTER_STARTS_URI, "3fa85f64-5717-4562-b3fc-2c963f66afa6")))
          .respond(
              response()
                  .withStatusCode(200)
                  .withHeader("Content-Type", "application/json")
                  .withBody(readJsonFromFile("/GetAllMatterStarts200.json")));

      dataClaimsRestClient
          .getAllMatterStartsForSubmission(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"))
          .blockOptional()
          .ifPresent(
              matterStartResultSet -> {
                assertThat(matterStartResultSet.getSubmissionId())
                    .isEqualTo(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"));
              });
    }
  }
}
