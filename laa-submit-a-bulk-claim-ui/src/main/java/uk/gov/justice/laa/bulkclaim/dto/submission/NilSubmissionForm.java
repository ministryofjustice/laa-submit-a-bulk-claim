package uk.gov.justice.laa.bulkclaim.dto.submission;

import java.io.Serializable;
import lombok.Data;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;

@Data
public class NilSubmissionForm implements Serializable {

  private static final long serialVersionUID = 1L;

  private String office;
  private AreaOfLaw areaOfLaw;
  private String submissionPeriod;
  private String scheduleReference;
  private int officeCount;
}
