package uk.gov.justice.laa.cwa.bulkupload.service;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.claims.model.CreateBulkSubmission201Response;

/**
 * REST Service interface for interacting with the Claims API.
 *
 * @author Jamie Briggs
 */
@HttpExchange("/api/v0/bulk-submissions")
public interface ClaimsRestService {

  /**
   * Uploads a bulk claim submission file to the Claims API.
   *
   * @param file a bulk claim submission file.
   * @return a mono containing the response from the Claims API.
   */
  @PostExchange(contentType = MediaType.MULTIPART_FORM_DATA_VALUE)
  Mono<CreateBulkSubmission201Response> upload(@RequestPart("file") MultipartFile file);
}
