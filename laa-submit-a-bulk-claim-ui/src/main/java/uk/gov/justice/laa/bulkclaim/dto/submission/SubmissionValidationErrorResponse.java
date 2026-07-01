package uk.gov.justice.laa.bulkclaim.dto.submission;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmissionValidationErrorResponse {

  private String detail;
  private String instance;
  private Integer status;
  private String title;
  private String type;
  private String message;
  private List<Issue> issues = List.of();

  @Data
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Issue {
    private String code;
    private String message;
    private String path;
    private String severity;
    private String technicalMessage;
  }
}
