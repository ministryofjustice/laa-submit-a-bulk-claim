package uk.gov.justice.laa.cwa.bulkupload.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.justice.laa.cwa.bulkupload.exception.VirusCheckException;
import uk.gov.justice.laa.cwa.bulkupload.response.SdsVirusCheckResponseDto;

/** Service class for performing virus check. */
@Service
@RequiredArgsConstructor
public class VirusCheckService {

  private final RestClient restClient;
  private final TokenService tokenService;

  @Value("${sds-api.url}")
  private String sdsApiUrl;

  /**
   * Perform a virus check for the given file.
   *
   * @param file the file
   */
  public void checkVirus(MultipartFile file) {
    if (file == null) {
      throw new VirusCheckException("File cannot be null");
    }

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", file.getResource());
    SdsVirusCheckResponseDto sdsVirusCheckResponseDto =
        restClient
            .put()
            .uri(sdsApiUrl + "/virus_check_file")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .header("Authorization", "Bearer " + tokenService.getSdsAccessToken())
            .body(body)
            .retrieve()
            .body(SdsVirusCheckResponseDto.class);
    if (sdsVirusCheckResponseDto == null
        || !StringUtils.hasText(sdsVirusCheckResponseDto.getSuccess())) {
      throw new VirusCheckException("Virus check failed");
    }
  }
}
