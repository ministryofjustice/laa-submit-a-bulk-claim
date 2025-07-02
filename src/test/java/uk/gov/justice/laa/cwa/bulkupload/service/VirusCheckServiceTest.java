package uk.gov.justice.laa.cwa.bulkupload.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import uk.gov.justice.laa.cwa.bulkupload.exception.VirusCheckException;
import uk.gov.justice.laa.cwa.bulkupload.response.SdsVirusCheckResponseDto;

@ExtendWith(MockitoExtension.class)
class VirusCheckServiceTest {

  @Mock private RestClient restClient;

  @Mock private TokenService tokenService;

  private VirusCheckService virusCheckService;

  @BeforeEach
  void setUp() {
    virusCheckService = new VirusCheckService(restClient, tokenService);
  }

  @Test
  void shouldSuccessfullyCheckVirusInFile() {
    // Given
    String mockToken = "mock-token";
    SdsVirusCheckResponseDto expectedResponse = new SdsVirusCheckResponseDto();
    expectedResponse.setSuccess("success");

    RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
    RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
    RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

    when(tokenService.getSdsAccessToken()).thenReturn(mockToken);
    when(restClient.put()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(endsWith("/virus_check_file"))).thenReturn(requestBodySpec);
    when(requestBodySpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(requestBodySpec);
    when(requestBodySpec.header("Authorization", "Bearer " + mockToken))
        .thenReturn(requestBodySpec);
    when(requestBodySpec.body(any(MultiValueMap.class))).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(SdsVirusCheckResponseDto.class)).thenReturn(expectedResponse);

    MockMultipartFile file =
        new MockMultipartFile(
            "file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

    // When
    virusCheckService.checkVirus(file);

    // Then
    verify(requestBodySpec).contentType(MediaType.MULTIPART_FORM_DATA);
    verify(requestBodySpec).header("Authorization", "Bearer " + mockToken);
  }

  @Test
  void shouldHandleNullFile() {

    // When/Then
    assertThatThrownBy(() -> virusCheckService.checkVirus(null))
        .isInstanceOf(VirusCheckException.class)
        .hasMessage("File cannot be null");
  }

  @Test
  void shouldHandleRestClientException() {
    // Given
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());

    RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
    RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);

    when(tokenService.getSdsAccessToken()).thenReturn("mock-token");
    when(restClient.put()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(endsWith("/virus_check_file"))).thenReturn(requestBodySpec);
    when(requestBodySpec.contentType(any())).thenReturn(requestBodySpec);
    when(requestBodySpec.header(any(), any())).thenReturn(requestBodySpec);
    when(requestBodySpec.body(any(MultiValueMap.class))).thenReturn(requestBodySpec);
    when(requestBodySpec.retrieve())
        .thenThrow(new RestClientException("Failed to connect to server"));

    // When/Then
    assertThatThrownBy(() -> virusCheckService.checkVirus(file))
        .isInstanceOf(RestClientException.class)
        .hasMessage("Failed to connect to server");
  }
}
