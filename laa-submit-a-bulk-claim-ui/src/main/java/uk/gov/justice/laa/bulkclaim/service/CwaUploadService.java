package uk.gov.justice.laa.bulkclaim.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.justice.laa.bulkclaim.response.CwaSubmissionResponseDto;
import uk.gov.justice.laa.bulkclaim.response.CwaUploadErrorResponseDto;
import uk.gov.justice.laa.bulkclaim.response.CwaUploadResponseDto;
import uk.gov.justice.laa.bulkclaim.response.CwaUploadSummaryResponseDto;
import uk.gov.justice.laa.bulkclaim.response.CwaVendorDto;

/**
 * This service is deprecated as all submission related functionality is now handled by the Claims
 * API. Leaving this class in place for now to keep the existing logic here until Claims API is
 * fully integrated.
 *
 * @author Jamie Briggs
 */
@Deprecated(forRemoval = true)
@RequiredArgsConstructor
public class CwaUploadService {

  private final RestClient restClient;

  @Value("${cwa-api.url}")
  private String cwaApiUrl;

  /**
   * Uploads a file to CWA.
   *
   * @param file the file to be uploaded.
   * @param provider the provider for which the file is being uploaded.
   * @param username the user who is uploading the file.
   * @return CwaUploadResponseDto containing upload response details.
   */
  public CwaUploadResponseDto uploadFile(MultipartFile file, String provider, String username) {
    if (file == null) {
      throw new IllegalArgumentException("file cannot be null");
    }
    if (username == null) {
      throw new IllegalArgumentException("username cannot be null");
    }
    if (provider == null) {
      throw new IllegalArgumentException("provider cannot be null");
    }

    MultipartBodyBuilder builder = new MultipartBodyBuilder();
    builder.part("file", file.getResource()).header("Content-Type", file.getContentType());
    builder.part("username", username);
    builder.part("vendor_id", provider);

    return restClient
        .post()
        .uri(cwaApiUrl + "/api/upload")
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .body(builder.build())
        .retrieve()
        .body(CwaUploadResponseDto.class);
  }

  /**
   * Retrieves the list of providers from CWA.
   *
   * @param username for which providers are to be fetched
   * @return List of CwaVendorDto
   */
  public List<CwaVendorDto> getProviders(String username) {
    return restClient
        .get()
        .uri(
            cwaApiUrl + "/api/validate_user",
            uriBuilder -> uriBuilder.queryParam("username", username).build())
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }

  /**
   * Validates the file in CWA.
   *
   * @param fileId the ID of the file to be validated.
   * @param username the user who is validating the file.
   * @param provider the provider for which validation is to be done.
   * @return CwaSubmissionResponseDto containing validation results.
   */
  public CwaSubmissionResponseDto processSubmission(
      String fileId, String username, String provider) {
    if (fileId == null) {
      throw new IllegalArgumentException("fileId cannot be null");
    }
    if (username == null) {
      throw new IllegalArgumentException("username cannot be null");
    }
    if (provider == null) {
      throw new IllegalArgumentException("provider cannot be null");
    }
    return restClient
        .post()
        .uri(
            cwaApiUrl + "/api/process_submission",
            uriBuilder ->
                uriBuilder
                    .queryParam("username", username)
                    .queryParam("am_bulk_file_id", fileId)
                    .queryParam("vendor_id", provider)
                    .build())
        .retrieve()
        .body(CwaSubmissionResponseDto.class);
  }

  /**
   * Retrieves the upload summary from CWA.
   *
   * @param fileId the ID of the file for which summary is to be fetched.
   * @param username the user who is fetching the summary.
   * @param provider the provider for which summary is to be fetched.
   * @return List of CwaUploadSummaryResponseDto containing the upload summary.
   */
  public List<CwaUploadSummaryResponseDto> getUploadSummary(
      String fileId, String username, String provider) {
    return restClient
        .get()
        .uri(
            cwaApiUrl + "/api/get_bulkload_summary",
            uriBuilder ->
                uriBuilder
                    .queryParam("username", username)
                    .queryParam("am_bulk_file_id", fileId)
                    .queryParam("vendor_id", provider)
                    .build())
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }

  /**
   * Retrieves the upload errors from CWA.
   *
   * @param fileId the ID of the file for which errors are to be fetched.
   * @param username the user who is fetching the errors.
   * @param provider the provider for which errors are to be fetched.
   * @return List of CwaUploadErrorResponseDto containing the upload errors.
   */
  public List<CwaUploadErrorResponseDto> getUploadErrors(
      String fileId, String username, String provider) {
    return restClient
        .get()
        .uri(
            cwaApiUrl + "/api/get_bulkload_errors",
            uriBuilder ->
                uriBuilder
                    .queryParam("username", username)
                    .queryParam("am_bulk_file_id", fileId)
                    .queryParam("vendor_id", provider)
                    .build())
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});
  }
}
