package uk.gov.justice.laa.bulkclaim.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.ExportDataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;

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

  private final ExportDataClaimsRestClient exportDataClaimsRestClient;
  private final OidcAttributeUtils oidcAttributeUtils;

  /**
   * Exports the submission details as a CSV file for a specified submission ID, office, and area of
   * law. The generated file is sent as a downloadable resource in the response.
   *
   * @param submissionId the unique identifier of the submission to be exported
   * @param office the office account number for the submission
   * @param areaOfLaw the area of law for the submission
   * @return a {@code Mono} of {@code ResponseEntity} containing a downloadable {@code Resource} of
   *     the CSV export
   */
  @GetMapping("/submission/{submissionId}/export")
  public Mono<ResponseEntity<Resource>> exportSubmissionDetail(
      @PathVariable UUID submissionId,
      @RequestParam String office,
      @RequestParam String areaOfLaw,
      @AuthenticationPrincipal OidcUser oidcUser) {
    var offices = oidcAttributeUtils.getUserOffices(oidcUser);
    if (!offices.contains(office)) {
      throw new SubmitBulkClaimException(
          "User (%s) does not have access to office: %s"
              .formatted(oidcUser.getPreferredUsername(), office));
    }

    String areaOfLawPathVariable = areaOfLaw.toLowerCase().replace(" ", "-");

    Mono<ResponseEntity<byte[]>> submissionExport =
        exportDataClaimsRestClient.getSubmissionExport(areaOfLawPathVariable, submissionId, office);

    return submissionExport.map(
        file ->
            ResponseEntity.ok()
                .contentType(file.getHeaders().getContentType())
                .contentLength(file.getBody().length)
                .headers(file.getHeaders())
                .body(new ByteArrayResource(file.getBody())));
  }
}
