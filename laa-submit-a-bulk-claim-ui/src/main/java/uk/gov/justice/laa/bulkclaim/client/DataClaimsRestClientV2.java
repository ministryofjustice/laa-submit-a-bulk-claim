package uk.gov.justice.laa.bulkclaim.client;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResultSetV2;

/**
 * REST Service interface for interacting with version 2 of the Claims API. Provides functionality
 * to manage claims, submissions, and related resources. This interface includes updated API
 * endpoints aligned with version 2 specifications.
 */
@HttpExchange("/api/v2")
public interface DataClaimsRestClientV2 {

  @GetExchange("/claims")
  ResponseEntity<ClaimResultSetV2> getClaims(
      @RequestParam(value = "office_code") String officeCode,
      @RequestParam(value = "submission_id") UUID submissionId,
      @RequestParam(value = "page") Integer page,
      @RequestParam(value = "size") Integer size,
      @RequestParam(value = "sort", required = false) String sort);
}
