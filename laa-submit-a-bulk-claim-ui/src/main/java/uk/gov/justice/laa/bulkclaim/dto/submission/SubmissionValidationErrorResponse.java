package uk.gov.justice.laa.bulkclaim.dto.submission;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SubmissionValidationErrorResponse(
    String detail,
    String instance,
    Integer status,
    String title,
    String type,
    String message,
    List<Issue> issues) {
  public SubmissionValidationErrorResponse(
      String detail, String instance, Integer status, String title, String type, String message) {
    this(detail, instance, status, title, type, message, List.of());
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Issue(
      String code, String message, String path, String severity, String technicalMessage) {}
}
