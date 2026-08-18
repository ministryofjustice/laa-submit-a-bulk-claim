package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.BULK_SUBMISSION_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.BulkSubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.GetBulkSubmissionStatusById200Response;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BulkUploadBeingCheckedController {

  private final DataClaimsRestClient dataClaimsRestClient;
  private final OidcAttributeUtils oidcAttributeUtils;

  private final List<BulkSubmissionStatus> completedStatuses =
      List.of(
          BulkSubmissionStatus.VALIDATION_SUCCEEDED,
          BulkSubmissionStatus.VALIDATION_FAILED,
          BulkSubmissionStatus.READY_FOR_SUBMISSION);

  private final List<BulkSubmissionStatus> pendingStatuses =
      List.of(BulkSubmissionStatus.READY_FOR_PARSING, BulkSubmissionStatus.PARSING_COMPLETED);

  /**
   * Shows the import in progress page, and refreshes every 5 seconds. Redirects if the submission
   * is ready.
   */
  @GetMapping("/upload-is-being-checked")
  public String uploadBeingChecked(
      Model model,
      @SessionAttribute(name = SUBMISSION_ID, required = false) UUID submissionId,
      @SessionAttribute(name = BULK_SUBMISSION_ID, required = false) UUID bulkSubmissionId) {

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
      if (completedStatuses.contains(bulkSubmissionStatus)) {
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
    } catch (ResponseStatusException e) {
      throw e;
    }
  }

  @GetMapping("/submission/{submissionId}/status")
  public ResponseEntity<Boolean> isSubmissionDone(
      @PathVariable UUID submissionId, @AuthenticationPrincipal OidcUser oidcUser) {
    try {
      Optional<SubmissionResponse> submissionResponse =
          dataClaimsRestClient.getSubmission(submissionId).blockOptional();

      // This specifically does not use the SubmissionControllerAdvice. This is due to the advice
      //  expecting a submission to be found 100% of the time, however this may not be the case
      //  initially for a submission that has not been created yet so doing the office code check
      //  here instead so handle a 404 submission.
      if (submissionResponse.isPresent()) {
        var offices = oidcAttributeUtils.getUserOffices(oidcUser);
        if (!offices.contains(submissionResponse.get().getOfficeAccountNumber())) {
          throw new ResponseStatusException(
              HttpStatus.FORBIDDEN,
              "User does not have access to office %s"
                  .formatted(submissionResponse.get().getOfficeAccountNumber()));
        }
      }

      SubmissionStatus submissionStatus =
          submissionResponse.map(SubmissionResponse::getStatus).orElse(SubmissionStatus.CREATED);
      log.debug("Submission status: {}", submissionStatus);
      return ResponseEntity.ok(
          List.of(
                  SubmissionStatus.VALIDATION_SUCCEEDED,
                  SubmissionStatus.VALIDATION_FAILED,
                  SubmissionStatus.READY_FOR_SUBMISSION)
              .contains(submissionStatus));
    } catch (WebClientResponseException e) {
      return new ResponseEntity<>(false, e.getStatusCode());
    } catch (ResponseStatusException e) {
      throw e;
    } catch (Exception e) {
      log.error("Unexpected error occurred while checking submission status", e);
      return new ResponseEntity<>(false, HttpStatusCode.valueOf(404));
    }
  }
}
