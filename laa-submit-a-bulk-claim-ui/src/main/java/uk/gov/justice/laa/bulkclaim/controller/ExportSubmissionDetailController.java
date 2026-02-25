package uk.gov.justice.laa.bulkclaim.controller;

import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.client.ExportDataClaimsRestClient;

/**
 * Controller responsible for exporting submission details as a CSV file. This class provides an
 * endpoint to trigger the export of submission data for a specified area of law and office.
 *
 * @author Jamie Briggs
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ExportSubmissionDetailController {

  private final DataClaimsRestClient dataClaimsRestClient;
  private final ExportDataClaimsRestClient exportDataClaimsRestClient;

  /**
   * Exports the submission details as a CSV file for a specified submission ID, office, and area of
   * law. The generated file is sent as a downloadable resource in the response.
   *
   * @param submissionId the unique identifier of the submission to be exported
   * @param oidcUser the authenticated OIDC user making the request
   * @return a {@code Mono} of {@code ResponseEntity} containing a downloadable {@code Resource} of
   *     the CSV export
   */
  @GetMapping("/submission/{submissionId}/export")
  public Mono<ResponseEntity<Resource>> exportSubmissionDetail(
      @PathVariable UUID submissionId, @AuthenticationPrincipal OidcUser oidcUser) {

    return dataClaimsRestClient
        .getSubmission(submissionId)
        .flatMap(
            submission -> {
              // Setup response headers
              String rawFileName =
                  "submission-%s-%s-%s"
                      .formatted(submission.getSubmissionPeriod(), submissionId, LocalDate.now());

              HttpHeaders headers = getHttpHeaders(rawFileName);

              String areaOfLawPathVariable =
                  submission.getAreaOfLaw().getValue().toLowerCase().replace(" ", "-");

              Mono<ResponseEntity<byte[]>> submissionExport =
                  exportDataClaimsRestClient.getSubmissionExport(
                      areaOfLawPathVariable, submissionId, submission.getOfficeAccountNumber());

              return submissionExport.map(
                  file ->
                      ResponseEntity.ok()
                          .headers(headers)
                          .body(new ByteArrayResource(file.getBody())));
            });
  }

  private static @NonNull HttpHeaders getHttpHeaders(String rawFileName) {
    String safeFileNameBase =
        rawFileName
            .replaceAll("[\\r\\n]", "") // prevent header injection
            .replaceAll("[^A-Za-z0-9._-]", "_"); // conservative filename chars

    HttpHeaders headers = new HttpHeaders();
    headers.setContentDisposition(
        ContentDisposition.attachment().filename(safeFileNameBase + ".csv").build());
    headers.add("Content-Type", "text/csv");
    headers.add("Cache-Control", "no-store");
    return headers;
  }
}
