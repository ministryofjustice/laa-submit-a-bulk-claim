package uk.gov.justice.laa.payments.submit.controller;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.payments.submit.client.ExportDataClaimsRestClient;
import uk.gov.justice.laa.payments.submit.service.SubmissionService;
import uk.gov.justice.laa.payments.submit.util.SubmissionPeriodUtil;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ExportSubmissionDetailController {

  private static final DateTimeFormatter FILENAME_PERIOD_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MMMM", Locale.ENGLISH);

  private final ExportDataClaimsRestClient exportDataClaimsRestClient;
  private final SubmissionService submissionService;

  @GetMapping({"/submission/{submissionId}/export", "/submissions/{submissionId}/export"})
  public Mono<ResponseEntity<Resource>> exportSubmissionDetail(
      @PathVariable UUID submissionId, @AuthenticationPrincipal OidcUser oidcUser) {
    var submission = submissionService.getSubmission(submissionId, oidcUser);
    System.out.println("STA: " + submission.getStatus());
    if (submission.getStatus() != SubmissionStatus.VALIDATION_SUCCEEDED) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Submission %s is not available for export".formatted(submissionId));
    }

    String office = submission.getOfficeAccountNumber();
    String areaOfLawPathVariable =
        submission.getAreaOfLaw().getValue().toLowerCase(Locale.ENGLISH).replace(" ", "-");

    YearMonth submissionPeriod =
        YearMonth.parse(submission.getSubmissionPeriod(), SubmissionPeriodUtil.ABBR_PERIOD_FMT);

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
                  .filename(
                      "%s-%s-%s-bulk-claim-summary.csv"
                          .formatted(
                              office,
                              areaOfLawPathVariable,
                              submissionPeriod
                                  .format(FILENAME_PERIOD_FORMATTER)
                                  .toLowerCase(Locale.ENGLISH)))
                  .build());

          return ResponseEntity.ok()
              .headers(safeHeaders)
              .body(new ByteArrayResource(file.getBody()));
        });
  }
}
