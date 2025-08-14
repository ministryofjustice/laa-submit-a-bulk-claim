package uk.gov.justice.laa.bulkclaim.service.claims;

import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.claims.model.ClaimFields;
import uk.gov.justice.laa.claims.model.CreateBulkSubmission201Response;
import uk.gov.justice.laa.claims.model.GetSubmission200Response;

/**
 * REST Service interface for interacting with the Claims API.
 *
 * @author Jamie Briggs
 */
@HttpExchange("/api/v0")
public interface DataClaimsRestService {

  /**
   * Uploads a bulk claim submission file to the Claims API.
   *
   * @param file a bulk claim submission file.
   * @return a mono containing the response from the Claims API.
   * @throws WebClientResponseException if status other than 2xx is returned
   */
  @PostExchange(value = "/bulk-submissions", contentType = MediaType.MULTIPART_FORM_DATA_VALUE)
  Mono<ResponseEntity<CreateBulkSubmission201Response>> upload(
      @RequestPart("file") MultipartFile file) throws WebClientResponseException;

  /**
   * Gets a submission by its ID.
   *
   * @param submissionId the submission ID
   * @return a mono containing the response from the Claims API.
   * @throws WebClientResponseException if status other than 2xx is returned
   */
  @GetExchange(value = "/submissions/{submissionId}")
  Mono<GetSubmission200Response> getSubmission(@PathVariable("submissionId") UUID submissionId)
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
  Mono<ClaimFields> getSubmissionClaim(
      @PathVariable("submission-id") UUID submissionId, @PathVariable("claim-id") UUID claimId);
}
