package uk.gov.justice.laa.cwa.bulkupload.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.justice.laa.cwa.bulkupload.response.UploadResponseDto;

/**
 * Service class for performing virus check.
 */
@Service
@RequiredArgsConstructor
public class VirusCheckService {
    private final RestClient restClient;
    private final TokenService tokenService;

    /**
     * Perform a virus check for the given file.
     *
     * @param file the file
     * @return the result
     */
    public UploadResponseDto checkVirus(MultipartFile file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", file.getResource());

        return restClient.put()
                .uri("/virus_check_file")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header("Authorization", "Bearer " + tokenService.getSdsAccessToken())
                .body(body)
                .retrieve()
                .body(UploadResponseDto.class);
    }
}