package uk.gov.justice.laa.bulkclaim.dto.submission;

import java.io.Serializable;
import lombok.Data;

@Data
public class NilSubmissionForm implements Serializable {

  private static final long serialVersionUID = 1L;

  private String office;
  private String areaOfLaw;
  private String submissionPeriod;
  private String scheduleReference;
}
