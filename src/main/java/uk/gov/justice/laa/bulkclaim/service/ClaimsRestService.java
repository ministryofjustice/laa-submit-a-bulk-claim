package uk.gov.justice.laa.cwa.bulkupload.service;

import java.util.Optional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.annotation.HttpExchange;
import uk.gov.justice.laa.claims.model.UploadResponse;

@HttpExchange
public interface ClaimsRestService {

  @PostMapping("/bulk-submissions")
  Optional<UploadResponse> upload(@RequestBody MultipartFile file);
}
