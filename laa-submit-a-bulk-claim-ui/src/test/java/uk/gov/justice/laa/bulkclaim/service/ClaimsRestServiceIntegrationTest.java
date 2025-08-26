package uk.gov.justice.laa.bulkclaim.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockserver.model.HttpResponse.response;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException.Forbidden;
import org.springframework.web.reactive.function.client.WebClientResponseException.InternalServerError;
import org.springframework.web.reactive.function.client.WebClientResponseException.Unauthorized;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.helper.MockServerIntegrationTest;
import uk.gov.justice.laa.bulkclaim.response.SubmissionSearchResponseDto;
import uk.gov.justice.laa.claims.model.CreateBulkSubmission201Response;

/**
 * Integration tests for the {@link ClaimsRestService}.
 *
 * @author Jamie Briggs
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcTestConfig.class)
@Slf4j
public class ClaimsRestServiceIntegrationTest extends MockServerIntegrationTest {

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
                  .withBody(expectedBody));

      // When
      CreateBulkSubmission201Response result = claimsRestService.upload(file).block();
      // Then
      assertThat(result.getBulkSubmissionId())
          .isEqualTo(UUID.fromString("f7ed1cda-692e-417a-bb55-5a5135006774"));
      assertThat(result.getSubmissionId())
          .isEqualTo(UUID.fromString("aca8d879-3dd4-4fd1-97ee-03f0d0cfd5db"));
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
          claimsRestService.search(offices, submissionId, from, to).block(Duration.ofSeconds(2));
      log.info(response.toString());
      assertThat(response.toString()).isNotEmpty();
      assertThat(response.submissions().getFirst().submissionId())
          .isEqualTo("660e8400-e29b-41d4-a716-14618440000");
    }
  }
}
