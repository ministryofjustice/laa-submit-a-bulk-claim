package uk.gov.justice.laa.cwa.bulkupload.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadErrorResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.response.CwaUploadSummaryResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.response.ValidateResponseDto;
import uk.gov.justice.laa.cwa.bulkupload.response.VendorDto;

import java.util.List;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CwaUploadServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private TokenService tokenService;

    private CwaUploadService cwaUploadService;

    @BeforeEach
    void setUp() {
        cwaUploadService = new CwaUploadService(restClient, tokenService);
        // Set the private cwaApiUrl field via reflection for test
        try {
            var field = CwaUploadService.class.getDeclaredField("cwaApiUrl");
            field.setAccessible(true);
            field.set(cwaUploadService, "http://localhost:8090");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldSuccessfullyUploadFile() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test".getBytes());
        String token = "mock-token";
        CwaUploadResponseDto expected = new CwaUploadResponseDto();

        RestClient.RequestBodyUriSpec uriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(tokenService.getSdsAccessToken()).thenReturn(token);
        when(restClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(endsWith("/upload"))).thenReturn(bodySpec);
        when(bodySpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(bodySpec);
        when(bodySpec.header("Authorization", "Bearer " + token)).thenReturn(bodySpec);
        when(bodySpec.body(any(MultiValueMap.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(CwaUploadResponseDto.class)).thenReturn(expected);

        CwaUploadResponseDto result = cwaUploadService.uploadFile(file, "provider", "user");
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldThrowIfFileIsNull() {
        assertThatThrownBy(() -> cwaUploadService.uploadFile(null, "provider", "user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("file cannot be null");
    }

    @Test
    void shouldPropagateRestClientExceptionOnUpload() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test".getBytes());
        RestClient.RequestBodyUriSpec uriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);

        when(tokenService.getSdsAccessToken()).thenReturn("token");
        when(restClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(endsWith("/upload"))).thenReturn(bodySpec);
        when(bodySpec.contentType(any())).thenReturn(bodySpec);
        when(bodySpec.header(any(), any())).thenReturn(bodySpec);
        when(bodySpec.body(any(MultiValueMap.class))).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenThrow(new RestClientException("fail"));

        assertThatThrownBy(() -> cwaUploadService.uploadFile(file, "provider", "user"))
                .isInstanceOf(RestClientException.class)
                .hasMessage("fail");
    }

    @Test
    void shouldValidateFileSuccessfully() {
        String fileId = "file-123";
        String userName = "test-user";
        String provider = "test-provider";
        ValidateResponseDto expected = new ValidateResponseDto();

        RestClient.RequestBodyUriSpec uriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(tokenService.getSdsAccessToken()).thenReturn("token");
        when(restClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(endsWith("/process_submission"), any(Function.class))).thenReturn(bodySpec);
        when(bodySpec.header(any(), any())).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(ValidateResponseDto.class)).thenReturn(expected);
        ValidateResponseDto result = cwaUploadService.processSubmission(fileId, userName, provider);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldPropagateRestClientExceptionOnValidate() {
        RestClient.RequestBodyUriSpec uriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);

        when(tokenService.getSdsAccessToken()).thenReturn("token");
        when(restClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(endsWith("/process_submission"), any(Function.class))).thenReturn(bodySpec);
        when(bodySpec.header(any(), any())).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenThrow(new RestClientException("fail"));

        assertThatThrownBy(() -> cwaUploadService.processSubmission("file", "user", "provider"))
                .isInstanceOf(RestClientException.class)
                .hasMessage("fail");
    }

    @Test
    void shouldGetProviders() {
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        VendorDto vendor = new VendorDto();
        List<VendorDto> expected = List.of(vendor);

        when(tokenService.getSdsAccessToken()).thenReturn("token");
        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(Function.class))).thenReturn(headersSpec);
        when(headersSpec.header(anyString(), any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(expected);

        List<VendorDto> result = cwaUploadService.getProviders("user");
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldPropagateRestClientExceptionOnGetProviders() {
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);

        when(tokenService.getSdsAccessToken()).thenReturn("token");
        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(Function.class))).thenReturn(headersSpec);
        when(headersSpec.header(anyString(), any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenThrow(new RestClientException("fail"));

        assertThatThrownBy(() -> cwaUploadService.getProviders("user"))
                .isInstanceOf(RestClientException.class)
                .hasMessage("fail");
    }

    @Test
    void shouldGetUploadSummary() {
        String fileId = "file-123";
        String userName = "test-user";
        String provider = "test-provider";

        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        CwaUploadSummaryResponseDto summary = new CwaUploadSummaryResponseDto();
        List<CwaUploadSummaryResponseDto> expected = List.of(summary);

        when(tokenService.getSdsAccessToken()).thenReturn("token");
        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(Function.class))).thenReturn(headersSpec);
        when(headersSpec.header(anyString(), any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(expected);
        List<CwaUploadSummaryResponseDto> result = cwaUploadService.getUploadSummary(fileId, userName, provider);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldGetUploadErrors() {
        String fileId = "file-123";
        String userName = "test-user";
        String provider = "test-provider";

        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        CwaUploadErrorResponseDto error = new CwaUploadErrorResponseDto();
        List<CwaUploadErrorResponseDto> expected = List.of(error);

        when(tokenService.getSdsAccessToken()).thenReturn("token");
        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(Function.class))).thenReturn(headersSpec);
        when(headersSpec.header(anyString(), any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(expected);

        List<CwaUploadErrorResponseDto> result = cwaUploadService.getUploadErrors(fileId, userName, provider);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldThrowIfProviderIsNullOnUploadFile() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test".getBytes());
        assertThatThrownBy(() -> cwaUploadService.uploadFile(file, null, "user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("provider cannot be null");
    }

    @Test
    void shouldThrowIfUserNameIsNullOnUploadFile() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test".getBytes());
        assertThatThrownBy(() -> cwaUploadService.uploadFile(file, "provider", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userName cannot be null");
    }

    @Test
    void shouldThrowIfFileIdIsNullOnProcessSubmission() {
        assertThatThrownBy(() -> cwaUploadService.processSubmission(null, "user", "provider"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fileId cannot be null");
    }

    @Test
    void shouldThrowIfUserNameIsNullOnProcessSubmission() {
        assertThatThrownBy(() -> cwaUploadService.processSubmission("fileId", null, "provider"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("userName cannot be null");
    }

    @Test
    void shouldThrowIfProviderIsNullOnProcessSubmission() {
        assertThatThrownBy(() -> cwaUploadService.processSubmission("fileId", "user", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("provider cannot be null");
    }

    @Test
    void shouldReturnEmptyListIfNoProviders() {
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(tokenService.getSdsAccessToken()).thenReturn("token");
        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString(), any(Function.class))).thenReturn(headersSpec);
        when(headersSpec.header(anyString(), any())).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(List.of());

        List<VendorDto> result = cwaUploadService.getProviders("user");
        assertThat(result).isEmpty();
    }
}