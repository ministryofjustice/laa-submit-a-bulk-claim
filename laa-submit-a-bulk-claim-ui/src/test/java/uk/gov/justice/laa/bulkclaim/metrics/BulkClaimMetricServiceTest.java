package uk.gov.justice.laa.bulkclaim.metrics;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.Errors;
import org.springframework.validation.SimpleErrors;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import uk.gov.justice.laa.bulkclaim.dto.FileUploadForm;

@ExtendWith(MockitoExtension.class)
@DisplayName("Bulk Claim Metric Service Test")
class BulkClaimMetricServiceTest {

  @Mock PrometheusRegistry prometheusRegistry;

  BulkClaimMetricService bulkClaimMetricService;

  @BeforeEach
  void beforeEach() {
    bulkClaimMetricService = new BulkClaimMetricService(prometheusRegistry);
  }

  @Test
  @DisplayName("Verify metrics initialized")
  void verifyMetricsInitialized() {
    // Then
    verify(prometheusRegistry, times(1))
        .register(bulkClaimMetricService.getFileUploadSizeHistogram());
  }

  @Test
  @DisplayName("Should record successful file upload")
  void shouldRecordSuccessfulFileUpload() {
    // Given
    MockMultipartFile file =
        new MockMultipartFile("fileUpload", "empty.txt", "text/plain", "text".getBytes());
    // When
    bulkClaimMetricService.recordSuccessfulFileUploadSize(file);
    // Then
    assertThat(
            bulkClaimMetricService
                .getFileUploadSizeHistogram()
                .collect()
                .getDataPoints()
                .getFirst()
                .getSum())
        .isEqualTo(4);
    assertThat(
            bulkClaimMetricService
                .getFileUploadSizeHistogram()
                .collect()
                .getDataPoints()
                .getFirst()
                .getLabels()
                .get("has_errors"))
        .isEqualTo("false");
    assertThat(
            bulkClaimMetricService
                .getFileUploadSizeHistogram()
                .collect()
                .getDataPoints()
                .getFirst()
                .getLabels()
                .get("failed_reason"))
        .isEqualTo("N/A");
  }

  @Test
  @DisplayName("Should record failed file upload size using primitive long")
  void shouldRecordFailedFileUploadSizeUsingPrimitiveLong() {
    // Given
    long size = 20L;
    String reason = "This reason";
    // When
    bulkClaimMetricService.recordFailedFileUploadSize(size, reason);
    // Then
    assertThat(
            bulkClaimMetricService
                .getFileUploadSizeHistogram()
                .collect()
                .getDataPoints()
                .getFirst()
                .getSum())
        .isEqualTo(20L);
    assertThat(
            bulkClaimMetricService
                .getFileUploadSizeHistogram()
                .collect()
                .getDataPoints()
                .getFirst()
                .getLabels()
                .get("has_errors"))
        .isEqualTo("true");
    assertThat(
            bulkClaimMetricService
                .getFileUploadSizeHistogram()
                .collect()
                .getDataPoints()
                .getFirst()
                .getLabels()
                .get("failed_reason"))
        .isEqualTo(reason);
  }

  @Test
  @DisplayName("Should record failed file upload size using MultipartFile and BindingResult")
  void shouldRecordFailedFileUploadSizeUsingMultipartFileAndBindingResult() {
    // Given
    MockMultipartFile file =
        new MockMultipartFile("fileUpload", "empty.txt", "text/plain", "12345".getBytes());
    Errors errors = new SimpleErrors(new FileUploadForm(file));
    errors.rejectValue("file", "bulkImport.validation.empty", "File is empty");
    errors.rejectValue("file", "bulkImport.validation.size", "File size is too large");
    // When
    bulkClaimMetricService.recordFailedFileUploadSize(file, errors);
    // Then
    assertThat(
            bulkClaimMetricService
                .getFileUploadSizeHistogram()
                .collect()
                .getDataPoints()
                .getFirst()
                .getSum())
        .isEqualTo(5);
    assertThat(
            bulkClaimMetricService
                .getFileUploadSizeHistogram()
                .collect()
                .getDataPoints()
                .getFirst()
                .getLabels()
                .get("has_errors"))
        .isEqualTo("true");
    assertThat(
            bulkClaimMetricService
                .getFileUploadSizeHistogram()
                .collect()
                .getDataPoints()
                .getFirst()
                .getLabels()
                .get("failed_reason"))
        .isEqualTo("File is empty, File size is too large");
  }

  @Test
  @DisplayName("Should not record failed file if file is null")
  void shouldNotRecordFailedFileIfFileIsNull() {
    // Given
    Errors errors = new SimpleErrors(new FileUploadForm(null));
    errors.rejectValue("file", "bulkImport.validation.empty", "File is empty");
    errors.rejectValue("file", "bulkImport.validation.size", "File size is too large");
    // When
    bulkClaimMetricService.recordFailedFileUploadSize(null, errors);
    // Then
    assertThat(
            bulkClaimMetricService.getFileUploadSizeHistogram().collect().getDataPoints().isEmpty())
        .isTrue();
  }

  @Test
  @DisplayName("Should record failed file upload size using MaxUploadSizeExceededException")
  void shouldRecordFailedFileUploadSizeUsingMaxUploadSizeExceededException() {
    // Given
    Throwable cause =
        new Throwable(
            "the request was rejected because its size (12345) exceeds the configured maximum (125)");
    MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(125, cause);
    // when
    bulkClaimMetricService.recordFailedFileUploadSize(exception);
    // Then
    assertThat(
            bulkClaimMetricService
                .getFileUploadSizeHistogram()
                .collect()
                .getDataPoints()
                .getFirst()
                .getSum())
        .isEqualTo(12345);
    assertThat(
            bulkClaimMetricService
                .getFileUploadSizeHistogram()
                .collect()
                .getDataPoints()
                .getFirst()
                .getLabels()
                .get("has_errors"))
        .isEqualTo("true");
    assertThat(
            bulkClaimMetricService
                .getFileUploadSizeHistogram()
                .collect()
                .getDataPoints()
                .getFirst()
                .getLabels()
                .get("failed_reason"))
        .isEqualTo("File size exceeds maximum allowed");
  }
}
