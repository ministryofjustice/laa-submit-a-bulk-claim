package uk.gov.justice.laa.bulkclaim.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.response.SubmissionSearchResponseDto;
import uk.gov.justice.laa.claims.model.CreateBulkSubmission201Response;

/**
 * REST Service interface for interacting with the Claims API.
 *
 * @author Jamie Briggs
 */
@HttpExchange("/api/v0")
public interface ClaimsRestService {

  /**
   * Uploads a bulk claim submission file to the Claims API.
   *
   * @param file a bulk claim submission file.
   * @return a mono containing the response from the Claims API.
   */
  @PostExchange(url = "/bulk-submissions", contentType = MediaType.MULTIPART_FORM_DATA_VALUE)
  Mono<CreateBulkSubmission201Response> upload(@RequestPart("file") MultipartFile file);

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
  Mono<SubmissionSearchResponseDto> search(
      @RequestParam(required = true) List<String> offices,
      @RequestParam String submissionId,
      @RequestParam LocalDate dateFrom,
      @RequestParam LocalDate dateTo);
}
