package uk.gov.justice.laa.bulkclaim.metrics;

import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.util.Objects;
import java.util.StringJoiner;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

/**
 * Metric service which records Submit a Bulk Claim based metrics.
 *
 * @author Jamie Briggs
 */
@Slf4j
@Getter
@Component
public class BulkClaimMetricService {

  private final Histogram fileUploadSizeHistogram;

  /**
   * Constructor.
   *
   * @param prometheusRegistry the registered prometheus registry Spring bean.
   */
  public BulkClaimMetricService(PrometheusRegistry prometheusRegistry) {
    this.fileUploadSizeHistogram =
        Histogram.builder()
            .name("submit_a_bulk_claim_file_size_bytes")
            .help("Size of uploaded bulk claim file in bytes which was submitted by the user")
            .labelNames("has_errors", "failed_reason")
            .register(prometheusRegistry);
  }

  /**
   * Records the size of the successfully uploaded file.
   *
   * @param file the file to measure the size of
   */
  public void recordSuccessfulFileUploadSize(MultipartFile file) {
    fileUploadSizeHistogram.labelValues("false", "N/A").observe(file.getSize());
  }

  /**
   * Records the size of the failed uploaded file, and the reason why it failed.
   *
   * @param size the size of the uploaded file in bytes.
   * @param reason the reason why the file upload failed.
   */
  public void recordFailedFileUploadSize(long size, String reason) {
    fileUploadSizeHistogram.labelValues("true", reason).observe(size);
  }

  /**
   * Records the size of the failed uploaded file, and the reason why it failed using the passed
   * binding result.
   *
   * @param file the file to measure the size of
   * @param errors the binding result of the failed validation.
   */
  public void recordFailedFileUploadSize(MultipartFile file, Errors errors) {
    if (!Objects.isNull(file)) {
      StringJoiner stringJoiner = new StringJoiner(", ");
      errors.getAllErrors().forEach(error -> stringJoiner.add(error.getDefaultMessage()));
      recordFailedFileUploadSize(file.getSize(), stringJoiner.toString());
    }
  }

  /**
   * Records the size of the failed uploaded file, and the reason why it failed.
   *
   * @param exception the exception thrown when the file upload failed.
   */
  public void recordFailedFileUploadSize(MaxUploadSizeExceededException exception) {
    String message = exception.getCause().getMessage();
    long size = Long.parseLong(message.replaceAll(".*size \\((\\d+)\\).*", "$1"));
    recordFailedFileUploadSize(size, "File size exceeds maximum allowed");
  }
}
