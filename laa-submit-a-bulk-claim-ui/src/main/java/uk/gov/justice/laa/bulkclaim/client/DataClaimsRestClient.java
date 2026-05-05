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
import uk.gov.justice.laa.dstew.payments.claimsdata.model.GetBulkSubmissionStatusById200Response;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.MatterStartGet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.MatterStartResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ValidationMessagesResponse;

@HttpExchange("/api/v1")
public interface DataClaimsRestClient {

  @PostExchange(value = "/bulk-submissions", contentType = MediaType.MULTIPART_FORM_DATA_VALUE)
  Mono<ResponseEntity<CreateBulkSubmission201Response>> upload(
      @RequestPart("file") MultipartFile file,
      @RequestParam String userId,
      // Allows Claims API to read the file and tell the user what office they're missing. Users
      // shouldn't be in a position where they have no offices unless they've been set up wrong.
      @RequestParam(required = false) List<String> offices)
      throws WebClientResponseException;

  @GetExchange(value = "/bulk-submissions/{id}/summary")
  Mono<GetBulkSubmissionStatusById200Response> getBulkSubmissionSummary(@PathVariable UUID id);

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

  @GetExchange(value = "/submissions/{submissionId}")
  Mono<SubmissionResponse> getSubmission(@PathVariable UUID submissionId)
      throws WebClientResponseException;

  @GetExchange(value = "/submissions/{submission-id}/claims/{claim-id}")
  Mono<ClaimResponse> getSubmissionClaim(
      @PathVariable("submission-id") UUID submissionId, @PathVariable("claim-id") UUID claimId);

  default ResponseEntity<ClaimResultSet> getClaims(
      @RequestParam(value = "office_code") String officeCode,
      @RequestParam(value = "submission_id") UUID submissionId,
      @RequestParam(value = "page") Integer page,
      @RequestParam(value = "size") Integer size) {
    return getClaims(officeCode, submissionId, page, size, "lineNumber,asc");
  }

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
