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

@Slf4j
@Getter
@Component
public class BulkClaimMetricService {

  private final Histogram fileUploadSizeHistogram;

  public BulkClaimMetricService(PrometheusRegistry prometheusRegistry) {
    this.fileUploadSizeHistogram =
        Histogram.builder()
            .name("submit_a_bulk_claim_file_size_bytes")
            .help("Size of uploaded bulk claim file in bytes which was submitted by the user")
            .labelNames("has_errors", "failed_reason")
            .register(prometheusRegistry);
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
