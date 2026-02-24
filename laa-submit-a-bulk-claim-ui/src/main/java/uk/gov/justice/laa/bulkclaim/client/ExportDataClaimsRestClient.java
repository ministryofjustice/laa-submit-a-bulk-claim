package uk.gov.justice.laa.bulkclaim.client;

import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

/**
 * REST Service interface for interacting with the Claims API's exports endpoint.
 *
 * @author Jamie Briggs
 */
@HttpExchange("/exports")
public interface ExportDataClaimsRestClient {

  /**
   * Retrieves a CSV export of submission claims for a specified area of law.
   *
   * @param areaOfLaw the area of law associated with the submission claims to be exported
   * @param submissionId the unique identifier of the submission to export
   * @param office the office associated with the submission claims
   * @return a {@code Mono<byte[]>} containing the CSV file data
   */
  @GetExchange(value = "/submission_claims_{area-of-law}.csv")
  Mono<byte[]> getSubmissionExport(
      @PathVariable("area-of-law") String areaOfLaw,
      @RequestParam("submissionId") UUID submissionId,
      @RequestParam("office") String office);
}
