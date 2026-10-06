package uk.gov.justice.laa.payments.submit.metrics;

import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.BulkSubmissionStatus;
import uk.gov.justice.laa.payments.submit.util.BulkLoadSpreadsheetFilenameMatch;
import uk.gov.justice.laa.payments.submit.util.BulkLoadSpreadsheetFilenameUtil;

@Slf4j
@Getter
@Component
public class BulkClaimMetricService {

  private static final String NOT_APPLICABLE = "N/A";

  private final BulkLoadSpreadsheetFilenameUtil bulkLoadSpreadsheetFilenameUtil;
  private final Histogram fileUploadSizeHistogram;
  private final Counter fileExtensionCounter;
  private final Counter fileNamePatternCounter;

  public BulkClaimMetricService(
      PrometheusRegistry prometheusRegistry,
      BulkLoadSpreadsheetFilenameUtil bulkLoadSpreadsheetFilenameUtil) {
    this.bulkLoadSpreadsheetFilenameUtil = bulkLoadSpreadsheetFilenameUtil;
    this.fileUploadSizeHistogram =
        Histogram.builder()
            .name("submit_a_bulk_claim_file_size_bytes")
            .help("Size of uploaded bulk claim file in bytes which was submitted by the user")
            .labelNames("has_errors", "failed_reason")
            .register(prometheusRegistry);
    this.fileExtensionCounter =
        Counter.builder()
            .name("submit_a_bulk_claim_file_extension_total")
            .help("Count of uploaded bulk claim file extensions, by validation outcome")
            .labelNames("extension", "validation_outcome")
            .register(prometheusRegistry);
    this.fileNamePatternCounter =
        Counter.builder()
            .name("submit_a_bulk_claim_file_name_pattern_total")
            .help(
                "Count of uploaded filenames matching the LAA Bulk Load Spreadsheet naming"
                    + " convention, by validation outcome")
            .labelNames("matched", "is_mac", "version", "validation_outcome")
            .register(prometheusRegistry);
  }

  public void recordFileExtension(String originalFilename, BulkSubmissionStatus outcome) {
    fileExtensionCounter
        .labelValues(extractExtension(originalFilename), mapValidationOutcome(outcome))
        .inc();
  }

  public void recordFileNamePattern(String originalFilename, BulkSubmissionStatus outcome) {
    BulkLoadSpreadsheetFilenameMatch match =
        bulkLoadSpreadsheetFilenameUtil.detect(originalFilename);
    fileNamePatternCounter
        .labelValues(
            String.valueOf(match.matched()),
            String.valueOf(match.isMac()),
            match.matched() ? match.version() : NOT_APPLICABLE,
            mapValidationOutcome(outcome))
        .inc();
  }

  private String extractExtension(String originalFilename) {
    if (originalFilename == null || originalFilename.isBlank()) {
      return NOT_APPLICABLE;
    }
    int lastDot = originalFilename.lastIndexOf('.');
    if (lastDot < 0 || lastDot == originalFilename.length() - 1) {
      return NOT_APPLICABLE;
    }
    return originalFilename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
  }

  private String mapValidationOutcome(BulkSubmissionStatus outcome) {
    return switch (outcome) {
      case VALIDATION_SUCCEEDED -> "succeeded";
      case VALIDATION_FAILED -> "failed";
      default ->
          throw new IllegalArgumentException(
              "Unsupported BulkSubmissionStatus for metric recording: " + outcome);
    };
  }

  public void recordSuccessfulFileUploadSize(MultipartFile file) {
    fileUploadSizeHistogram.labelValues("false", "N/A").observe(file.getSize());
  }

  public void recordFailedFileUploadSize(long size, String reason) {
    fileUploadSizeHistogram.labelValues("true", reason).observe(size);
  }

  public void recordFailedFileUploadSize(MultipartFile file, Errors errors) {
    if (!Objects.isNull(file)) {
      StringJoiner stringJoiner = new StringJoiner(", ");
      errors.getAllErrors().forEach(error -> stringJoiner.add(error.getDefaultMessage()));
      recordFailedFileUploadSize(file.getSize(), stringJoiner.toString());
    }
  }

  public void recordFailedFileUploadSize(MaxUploadSizeExceededException exception) {
    String message = exception.getCause().getMessage();
    long size = Long.parseLong(message.replaceAll(".*size \\((\\d+)\\).*", "$1"));
    recordFailedFileUploadSize(size, "File size exceeds maximum allowed");
  }
}
