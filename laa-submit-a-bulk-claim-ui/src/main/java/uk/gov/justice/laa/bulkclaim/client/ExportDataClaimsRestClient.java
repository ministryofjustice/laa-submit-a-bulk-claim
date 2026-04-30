package uk.gov.justice.laa.bulkclaim.client;

import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

@HttpExchange("/exports")
public interface ExportDataClaimsRestClient {

  @GetExchange(value = "/submission-claims-{area-of-law}")
  Mono<ResponseEntity<byte[]>> getSubmissionExport(
      @PathVariable("area-of-law") String areaOfLaw,
      @RequestParam("submission-id") UUID submissionId,
      @RequestParam("office") String office);
}
