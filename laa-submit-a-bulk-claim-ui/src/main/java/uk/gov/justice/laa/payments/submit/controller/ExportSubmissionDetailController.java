package uk.gov.justice.laa.payments.submit.controller;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.payments.submit.client.ExportDataClaimsRestClient;
import uk.gov.justice.laa.payments.submit.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.payments.submit.util.OidcAttributeUtils;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ExportSubmissionDetailController {

  private final ExportDataClaimsRestClient exportDataClaimsRestClient;
  private final OidcAttributeUtils oidcAttributeUtils;

  @GetMapping({"/submission/{submissionId}/export", "/submissions/{submissionId}/export"})
  public Mono<ResponseEntity<Resource>> exportSubmissionDetail(
      @PathVariable UUID submissionId,
      @RequestParam String office,
      @RequestParam String areaOfLaw,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate submissionPeriod,
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
        file -> {
          // Only add headers we need (Spring automatically adds some headers so don't want
          // to duplicate this)
            HttpHeaders safeHeaders = new HttpHeaders();
            safeHeaders.setContentType(file.getHeaders().getContentType());
            safeHeaders.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename(buildExportFilename(office, areaOfLawPathVariable, submissionPeriod))
                            .build());

            return ResponseEntity.ok()
                    .headers(safeHeaders)
                    .body(new ByteArrayResource(file.getBody()));
        });
  }

    private String buildExportFilename(
            String office, String areaOfLawPathVariable, LocalDate submissionPeriod) {
        String month =
                submissionPeriod
                        .getMonth()
                        .getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                        .toLowerCase(Locale.ENGLISH);
        return "%s-%s-%d-%s-bulk-claim-summary.csv"
                .formatted(office, areaOfLawPathVariable, submissionPeriod.getYear(), month);
    }
}
