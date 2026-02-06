package uk.gov.justice.laa.bulkclaim.client;

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
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.CreateBulkSubmission201Response;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.MatterStartGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.MatterStartResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessagesResponse;

/**
 * REST Service interface for interacting with the Claims API.
 *
 * @author Jamie Briggs
 */
@HttpExchange("/api/v1")
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
      @RequestParam String userId,
      @RequestParam List<String> offices)
      throws WebClientResponseException;

  /**
   * Searches submissions using JSON criteria sent in the GET request body.
   *
   * @param offices array of authenticated user silas provider offices.
   * @param submissionPeriod date range date from
   * @param areaOfLaw area of law
   * @param submissionStatus array of submission statuses
   * @param page page number
   * @param size page size
   * @param sort sort order
   * @return SubmissionSearchResponseDto
   */
  @GetExchange(url = "/submissions", accept = MediaType.APPLICATION_JSON_VALUE)
  Mono<SubmissionsResultSet> search(
      @RequestParam(value = "offices") List<String> offices,
      @RequestParam(value = "submission_period", required = false) String submissionPeriod,
      @RequestParam(value = "area_of_law", required = false) AreaOfLaw areaOfLaw,
      @RequestParam(value = "submission_statuses", required = false)
          List<SubmissionStatus> submissionStatus,
      @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
      @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
      @RequestParam(value = "sort", required = false) String sort);

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

  /**
   * Get claims in a submission, filtering by office code, and using page number and size. Orders by
   * line number by default
   *
   * @param officeCode the office code of the claims to be retrieved
   * @param submissionId the submission id of the claims to be retrieved
   * @param page the page number
   * @param size the page size
   * @return 200 OK with JSON body containing the list of matched claims
   */
  default ResponseEntity<ClaimResultSet> getClaims(
      @RequestParam(value = "office_code") String officeCode,
      @RequestParam(value = "submission_id") UUID submissionId,
      @RequestParam(value = "page") Integer page,
      @RequestParam(value = "size") Integer size) {
    return getClaims(officeCode, submissionId, page, size, "lineNumber,asc");
  }

  /**
   * Get claims in a submission, filtering by office code, and using page number and size.
   *
   * @param officeCode the office code of the claims to be retrieved
   * @param submissionId the submission id of the claims to be retrieved
   * @param page the page number
   * @param size the page size
   * @return 200 OK with JSON body containing the list of matched claims
   */
  @GetExchange("/claims")
  ResponseEntity<ClaimResultSet> getClaims(
      @RequestParam(value = "office_code") String officeCode,
      @RequestParam(value = "submission_id") UUID submissionId,
      @RequestParam(value = "page") Integer page,
      @RequestParam(value = "size") Integer size,
      @RequestParam(value = "sort", required = false) String sort);

  @GetExchange(value = "/submissions/{submission-id}/matter-starts/{matter-starts-id}")
  Mono<MatterStartGet> getSubmissionMatterStart(
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
      @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
      @RequestParam(value = "size", required = false) Integer size);

  @GetExchange(value = "/submissions/{id}/matter-starts")
  Mono<MatterStartResultSet> getAllMatterStartsForSubmission(@PathVariable("id") UUID submissionId);
}
