package uk.gov.justice.laa.cwa.bulkupload.service;

import java.util.Optional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.service.annotation.HttpExchange;
import uk.gov.justice.laa.claims.model.UploadResponse;

@HttpExchange(value = "/api/v1/claims")
public interface ClaimsRestService {

  Optional<UploadResponse> upload(@RequestBody MultipartFile file);
}
