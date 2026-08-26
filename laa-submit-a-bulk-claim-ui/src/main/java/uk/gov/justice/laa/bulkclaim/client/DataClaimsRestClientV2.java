package uk.gov.justice.laa.bulkclaim.client;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponseV2;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResultSetV2;

@HttpExchange("/api/v2")
public interface DataClaimsRestClientV2 {

  @GetExchange("/claims")
  ResponseEntity<ClaimResultSetV2> getClaims(
      @RequestParam(value = "office_code") String officeCode,
      @RequestParam(value = "submission_id") UUID submissionId,
      @RequestParam(value = "page") Integer page,
      @RequestParam(value = "size") Integer size,
      @RequestParam(value = "sort", required = false) String sort);

  default ResponseEntity<ClaimResultSetV2> getClaims(
      @RequestParam(value = "office_code") String officeCode,
      @RequestParam(value = "submission_id") UUID submissionId,
      @RequestParam(value = "page") Integer page,
      @RequestParam(value = "size") Integer size) {
    return getClaims(officeCode, submissionId, page, size, "line_number,asc");
  }

  @GetExchange(value = "/submissions/{submission-id}/claims/{claim-id}")
  Mono<ClaimResponseV2> getSubmissionClaim(
      @PathVariable("submission-id") UUID submissionId, @PathVariable("claim-id") UUID claimId);
}
