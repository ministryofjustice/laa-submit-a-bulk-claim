package uk.gov.justice.laa.payments.submit.metrics;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.validation.Errors;
import org.springframework.validation.SimpleErrors;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.BulkSubmissionStatus;
import uk.gov.justice.laa.payments.submit.dto.FileUploadForm;
import uk.gov.justice.laa.payments.submit.util.BulkLoadSpreadsheetFilenameUtil;

@ExtendWith(MockitoExtension.class)
@DisplayName("Bulk Claim Metric Service Test")
class BulkClaimMetricServiceTest {

  @Mock PrometheusRegistry prometheusRegistry;

  BulkClaimMetricService bulkClaimMetricService;

  @BeforeEach
  void beforeEach() {
    bulkClaimMetricService =
        new BulkClaimMetricService(prometheusRegistry, new BulkLoadSpreadsheetFilenameUtil());
  }

  @Test
  @DisplayName("Verify metrics initialized")
  void verifyMetricsInitialized() {
    // Then
    verify(prometheusRegistry, times(1))
        .register(bulkClaimMetricService.getFileUploadSizeHistogram());
    verify(prometheusRegistry, times(1)).register(bulkClaimMetricService.getFileExtensionCounter());
    verify(prometheusRegistry, times(1))
        .register(bulkClaimMetricService.getFileNamePatternCounter());
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

  @Nested
  @DisplayName("recordFileExtension")
  class RecordFileExtension {

    @ParameterizedTest
    @CsvSource({
      "claims.csv, csv",
      "claims.CSV, csv",
      "claims.txt, txt",
      "claims.TXT, txt",
      "claims.xml, xml",
      "claims.XML, xml"
    })
    @DisplayName("Should record the lower-cased extension with no leading dot")
    void shouldRecordExtension(String filename, String expectedExtension) {
      // When
      bulkClaimMetricService.recordFileExtension(
          filename, BulkSubmissionStatus.VALIDATION_SUCCEEDED);
      // Then
      assertThat(
              bulkClaimMetricService
                  .getFileExtensionCounter()
                  .collect()
                  .getDataPoints()
                  .getFirst()
                  .getValue())
          .isEqualTo(1);
      assertThat(
              bulkClaimMetricService
                  .getFileExtensionCounter()
                  .collect()
                  .getDataPoints()
                  .getFirst()
                  .getLabels()
                  .get("extension"))
          .isEqualTo(expectedExtension);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"noextension", "trailingdot."})
    @DisplayName("Should record \"N/A\" extension for a null, blank or extensionless filename")
    void shouldRecordNotApplicableExtension(String filename) {
      // When
      bulkClaimMetricService.recordFileExtension(
          filename, BulkSubmissionStatus.VALIDATION_SUCCEEDED);
      // Then
      assertThat(
              bulkClaimMetricService
                  .getFileExtensionCounter()
                  .collect()
                  .getDataPoints()
                  .getFirst()
                  .getLabels()
                  .get("extension"))
          .isEqualTo("N/A");
    }

    @ParameterizedTest
    @EnumSource(
        value = BulkSubmissionStatus.class,
        names = {"VALIDATION_SUCCEEDED", "VALIDATION_FAILED"})
    @DisplayName("Should record the validation outcome label")
    void shouldRecordValidationOutcome(BulkSubmissionStatus status) {
      // Given
      String expectedOutcome =
          status == BulkSubmissionStatus.VALIDATION_SUCCEEDED ? "succeeded" : "failed";
      // When
      bulkClaimMetricService.recordFileExtension("claims.csv", status);
      // Then
      assertThat(
              bulkClaimMetricService
                  .getFileExtensionCounter()
                  .collect()
                  .getDataPoints()
                  .getFirst()
                  .getLabels()
                  .get("validation_outcome"))
          .isEqualTo(expectedOutcome);
    }

    @Test
    @DisplayName("Should reject unsupported statuses")
    void shouldRejectUnsupportedStatuses() {
      assertThatThrownBy(
              () ->
                  bulkClaimMetricService.recordFileExtension(
                      "claims.csv", BulkSubmissionStatus.PARSING_FAILED))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("recordFileNamePattern")
  class RecordFileNamePattern {

    @Test
    @DisplayName("Should record a matched standard filename")
    void shouldRecordMatchedStandardFilename() {
      // When
      bulkClaimMetricService.recordFileNamePattern(
          "claims-v2.00.csv", BulkSubmissionStatus.VALIDATION_SUCCEEDED);
      // Then
      var labels =
          bulkClaimMetricService
              .getFileNamePatternCounter()
              .collect()
              .getDataPoints()
              .getFirst()
              .getLabels();
      assertThat(labels.get("matched")).isEqualTo("true");
      assertThat(labels.get("is_mac")).isEqualTo("false");
      assertThat(labels.get("version")).isEqualTo("2.00");
    }

    @Test
    @DisplayName("Should record a matched Mac-variant filename")
    void shouldRecordMatchedMacFilename() {
      // When
      bulkClaimMetricService.recordFileNamePattern(
          "claims-mac-v1.42.csv", BulkSubmissionStatus.VALIDATION_SUCCEEDED);
      // Then
      var labels =
          bulkClaimMetricService
              .getFileNamePatternCounter()
              .collect()
              .getDataPoints()
              .getFirst()
              .getLabels();
      assertThat(labels.get("matched")).isEqualTo("true");
      assertThat(labels.get("is_mac")).isEqualTo("true");
      assertThat(labels.get("version")).isEqualTo("1.42");
    }

    @Test
    @DisplayName("Should record a non-matching filename")
    void shouldRecordNonMatchingFilename() {
      // When
      bulkClaimMetricService.recordFileNamePattern(
          "my-own-claims.csv", BulkSubmissionStatus.VALIDATION_SUCCEEDED);
      // Then
      var labels =
          bulkClaimMetricService
              .getFileNamePatternCounter()
              .collect()
              .getDataPoints()
              .getFirst()
              .getLabels();
      assertThat(labels.get("matched")).isEqualTo("false");
      assertThat(labels.get("is_mac")).isEqualTo("false");
      assertThat(labels.get("version")).isEqualTo("N/A");
    }

    @ParameterizedTest
    @EnumSource(
        value = BulkSubmissionStatus.class,
        names = {"VALIDATION_SUCCEEDED", "VALIDATION_FAILED"})
    @DisplayName("Should record the validation outcome label")
    void shouldRecordValidationOutcome(BulkSubmissionStatus status) {
      // Given
      String expectedOutcome =
          status == BulkSubmissionStatus.VALIDATION_SUCCEEDED ? "succeeded" : "failed";
      // When
      bulkClaimMetricService.recordFileNamePattern("claims-v2.00.csv", status);
      // Then
      assertThat(
              bulkClaimMetricService
                  .getFileNamePatternCounter()
                  .collect()
                  .getDataPoints()
                  .getFirst()
                  .getLabels()
                  .get("validation_outcome"))
          .isEqualTo(expectedOutcome);
    }

    @Test
    @DisplayName("Should reject unsupported statuses")
    void shouldRejectUnsupportedStatuses() {
      assertThatThrownBy(
              () ->
                  bulkClaimMetricService.recordFileNamePattern(
                      "claims-v2.00.csv", BulkSubmissionStatus.PARSING_FAILED))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }
}
