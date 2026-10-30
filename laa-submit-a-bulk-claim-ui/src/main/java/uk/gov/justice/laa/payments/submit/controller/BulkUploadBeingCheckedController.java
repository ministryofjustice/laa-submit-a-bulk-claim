package uk.gov.justice.laa.payments.submit.controller;

import static java.lang.Boolean.TRUE;
import static uk.gov.justice.laa.payments.submit.constants.SessionConstants.BULK_SUBMISSION_ID;
import static uk.gov.justice.laa.payments.submit.constants.SessionConstants.ORIGINAL_FILENAME;
import static uk.gov.justice.laa.payments.submit.constants.SessionConstants.SUBMISSION_ID;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.BulkSubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.GetBulkSubmissionStatusById200Response;
import uk.gov.justice.laa.payments.submit.client.DataClaimsRestClient;
import uk.gov.justice.laa.payments.submit.config.FeatureFlagsConfig;
import uk.gov.justice.laa.payments.submit.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.payments.submit.metrics.BulkClaimMetricService;
import uk.gov.justice.laa.payments.submit.service.SubmissionService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BulkUploadBeingCheckedController {

  private final DataClaimsRestClient dataClaimsRestClient;
  private final BulkClaimMetricService bulkClaimMetricService;
  private final SubmissionService submissionService;
  private final FeatureFlagsConfig featureFlagsConfig;

  private final List<BulkSubmissionStatus> completedStatuses =
      List.of(BulkSubmissionStatus.VALIDATION_SUCCEEDED, BulkSubmissionStatus.VALIDATION_FAILED);

  private final List<BulkSubmissionStatus> pendingStatuses =
      List.of(BulkSubmissionStatus.READY_FOR_PARSING, BulkSubmissionStatus.PARSING_COMPLETED);

  /**
   * Shows the import in progress page, and refreshes every 5 seconds. Redirects if the submission
   * is ready.
   */
  @GetMapping("/upload-is-being-checked")
  public String uploadBeingChecked(
      Model model,
      @AuthenticationPrincipal OidcUser user,
      @SessionAttribute(name = SUBMISSION_ID, required = false) UUID submissionId,
      @SessionAttribute(name = BULK_SUBMISSION_ID, required = false) UUID bulkSubmissionId,
      @SessionAttribute(name = ORIGINAL_FILENAME, required = false) String originalFilename) {

    if (submissionId == null || bulkSubmissionId == null) {
      log.info("Submission IDs not found in session, redirecting to landing page");
      return "redirect:/";
    }

    try {
      GetBulkSubmissionStatusById200Response bulkSubmission =
          dataClaimsRestClient.getBulkSubmissionSummary(bulkSubmissionId).block();
      Assert.notNull(bulkSubmission, "Bulk submission summary cannot be null");

      BulkSubmissionStatus bulkSubmissionStatus = bulkSubmission.getStatus();
      if (bulkSubmissionStatus == BulkSubmissionStatus.PARSING_FAILED) {
        throw new SubmitBulkClaimException(
            "Bulk submission parsing failed for: " + bulkSubmissionId);
      }
      if (pendingStatuses.contains(bulkSubmissionStatus)) {
        model.addAttribute("shouldRefresh", true);
        return "pages/upload-being-checked";
      }
      if (completedStatuses.contains(bulkSubmissionStatus)
          || (TRUE.equals(featureFlagsConfig.getIsPreSubmissionEnabled())
              && bulkSubmissionStatus == BulkSubmissionStatus.VALIDATED_PENDING_APPROVAL)) {
        recordFilenameMetrics(originalFilename, submissionId, user, bulkSubmissionStatus);
        return "redirect:/submissions/%s".formatted(submissionId.toString());
      }
      throw new SubmitBulkClaimException(
          "Unexpected bulk submission status returned for: " + bulkSubmissionId);
    } catch (WebClientResponseException e) {
      if (e.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(404))) {
        log.debug(
            "No bulk submission found, will retry: %s".formatted(bulkSubmissionId.toString()));
        model.addAttribute("shouldRefresh", true);
        return "pages/upload-being-checked";
      }
      throw new SubmitBulkClaimException("Claims API returned an error", e);
    }
  }

  private void recordFilenameMetrics(
      String originalFilename, UUID submissionId, OidcUser user, BulkSubmissionStatus outcome) {
    if (originalFilename == null) {
      log.debug("No original filename stashed in session, skipping filename metrics");
      return;
    }
    bulkClaimMetricService.recordFileExtension(originalFilename, outcome);
    bulkClaimMetricService.recordFileNamePattern(
        originalFilename, lookupAreaOfLaw(submissionId, user), outcome);
  }

  private AreaOfLaw lookupAreaOfLaw(UUID submissionId, OidcUser user) {
    try {
      return submissionService.getSubmission(submissionId, user).getAreaOfLaw();
    } catch (Exception e) {
      log.warn("Unable to look up area of law for submission {} metrics", submissionId, e);
      return null;
    }
  }
}
