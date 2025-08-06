package uk.gov.justice.laa.cwa.bulkupload.service;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.claims.model.UploadResponse;

/**
 * REST Service interface for interacting with the Claims API.
 *
 * @author Jamie Briggs
 */
@HttpExchange
public interface ClaimsRestService {

  /**
   * Uploads a bulk claim submission file to the Claims API.
   *
   * @param file a bulk claim submission file.
   * @return a mono containing the response from the Claims API.
   */
  @PostMapping("/bulk-submissions")
  Mono<UploadResponse> upload(@RequestBody MultipartFile file, @RequestParam String userId);
}
