package uk.gov.justice.laa.bulkclaim.client;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.CreateBulkSubmission201Response;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.MatterStartGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.MatterStartResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessagesResponse;

/**
 * REST Service interface for interacting with the Claims API.
 *
 * @author Jamie Briggs
 */
@HttpExchange("/api/v0")
public interface DataClaimsRestClient {

  /**
   * Uploads a bulk claim submission file to the Claims API.
   *
   * @param file a bulk claim submission file.
   * @return a mono containing the response from the Claims API.
   * @throws WebClientResponseException if status other than 2xx is returned
   */
  @PostExchange(value = "/bulk-submissions", contentType = MediaType.MULTIPART_FORM_DATA_VALUE)
  Mono<ResponseEntity<CreateBulkSubmission201Response>> upload(
      @RequestPart("file") MultipartFile file,
      @RequestParam(required = true) String userId,
      @RequestParam(required = true) List<String> offices)
      throws WebClientResponseException;

  /**
   * Searches submissions using JSON criteria sent in the GET request body.
   *
   * @param offices array of authenticated user silas provider offices.
   * @param submissionId submission id
   * @param dateFrom date range date from
   * @param dateTo date range date to
   * @return SubmissionSearchResponseDto
   */
  @GetExchange(url = "/submissions", accept = MediaType.APPLICATION_JSON_VALUE)
  Mono<SubmissionsResultSet> search(
      @RequestParam(value = "offices", required = true) List<String> offices,
      @RequestParam(value = "submission_id", required = false) String submissionId,
      @RequestParam(value = "date_from", required = false) LocalDate dateFrom,
      @RequestParam(value = "date_to", required = false) LocalDate dateTo,
      @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10") Integer size);

  /**
   * Gets a submission by its ID.
   *
   * @param submissionId the submission ID
   * @return a mono containing the response from the Claims API.
   * @throws WebClientResponseException if status other than 2xx is returned
   */
  @GetExchange(value = "/submissions/{submissionId}")
  Mono<SubmissionResponse> getSubmission(@PathVariable("submissionId") UUID submissionId)
      throws WebClientResponseException;

  /**
   * Gets a submission claim by submission ID and claim ID.
   *
   * @param submissionId the submission ID
   * @param claimId the claim ID
   * @return a mono containing the response from the Claims API.
   * @throws WebClientResponseException if status other than 2xx is returned
   */
  @GetExchange(value = "/submissions/{submission-id}/claims/{claim-id}")
  Mono<ClaimResponse> getSubmissionClaim(
      @PathVariable("submission-id") UUID submissionId, @PathVariable("claim-id") UUID claimId);

  @GetExchange(value = "/submissions/{submission-id}/matter-starts/{matter-starts-id}")
  Mono<MatterStartGet> getSubmissionMatterStarts(
      @PathVariable("submission-id") UUID submissionId,
      @PathVariable("matter-starts-id") UUID claimId);

  /**
   * Gets validation errors for a submission.
   *
   * @param submissionId the submission ID
   * @return a Mono containing a list of validation errors for a submission.
   */
  @GetExchange(value = "/validation-messages")
  Mono<ValidationMessagesResponse> getValidationMessages(
      @RequestParam("submission-id") UUID submissionId,
      @RequestParam(value = "claim-id", required = false) UUID claimId,
      @RequestParam(value = "type", required = false) String type,
      @RequestParam(value = "source", required = false) String source,
      @RequestParam(value = "page", required = false, defaultValue = "0") Integer page);

  @GetExchange(value = "/submissions/{id}/matter-starts")
  Mono<MatterStartResultSet> getAllMatterStartsForSubmission(@PathVariable("id") UUID submissionId);
}
