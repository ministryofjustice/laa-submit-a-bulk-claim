package uk.gov.justice.laa.bulkclaim.controller;

import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.ExportDataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.util.SubmissionPeriodUtil;

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
  private final SubmissionPeriodUtil submissionPeriodUtil;

  /**
   * Exports the submission details as a CSV file for a specified submission ID, office, and area of
   * law. The generated file is sent as a downloadable resource in the response.
   *
   * @param submissionId the unique identifier of the submission to be exported
   * @param officeCode the office code associated with the submission
   * @param areaOfLaw the area of law corresponding to the submission being exported
   * @param submissionPeriod the submission period corresponding to the submission being exported
   * @param oidcUser the authenticated OIDC user making the request
   * @return a {@code Mono} of {@code ResponseEntity} containing a downloadable {@code Resource} of
   *     the CSV export
   */
  @GetMapping("/submission/{submissionId}/export")
  public Mono<ResponseEntity<Resource>> exportSubmissionDetail(
      @PathVariable UUID submissionId,
      // TODO Temporary, use oidcUser once API switches to office array
      @RequestParam("office") String officeCode,
      @RequestParam("areaOfLaw") String areaOfLaw,
      @RequestParam("submissionPeriod") String submissionPeriod,
      @AuthenticationPrincipal OidcUser oidcUser) {
    // Setup response headers
    String fileName =
        "submission-%s-%s-%s".formatted(submissionPeriod, submissionId, LocalDate.now());
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName + ".csv");
    headers.add("Content-Type", "text/csv");
    headers.add("Cache-Control", "no-store");

    String areaOfLawPathVariable = areaOfLaw.toLowerCase().replace(" ", "_");
    return exportDataClaimsRestClient
        .getSubmissionExport(areaOfLawPathVariable, submissionId, officeCode)
        .map(bytes -> ResponseEntity.ok().headers(headers).body(new ByteArrayResource(bytes)));
  }
}
