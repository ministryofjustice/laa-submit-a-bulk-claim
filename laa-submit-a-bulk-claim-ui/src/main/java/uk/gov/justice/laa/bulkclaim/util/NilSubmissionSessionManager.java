package uk.gov.justice.laa.bulkclaim.util;

import org.springframework.util.StringUtils;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;

public class NilSubmissionSessionManager {

  public static boolean isNilSubmissionSessionStateValid(
      NilSubmissionForm nilSubmissionForm, NilSubmissionPage page) {
    return switch (page) {
      case OFFICE -> sessionValidForOffice(nilSubmissionForm);
      case AREA_OF_LAW -> sessionValidForAreaOfLaw(nilSubmissionForm);
      case SUBMISSION_PERIOD -> sessionValidForSubmissionPeriod(nilSubmissionForm);
      case SCHEDULE_REFERENCE -> sessionValidForScheduleReference(nilSubmissionForm);
      default -> false;
    };
  }

  static boolean sessionValidForScheduleReference(NilSubmissionForm nilSubmissionForm) {
    return StringUtils.hasText(nilSubmissionForm.getSubmissionPeriod())
        && sessionValidForSubmissionPeriod(nilSubmissionForm);
  }

  static boolean sessionValidForSubmissionPeriod(NilSubmissionForm nilSubmissionForm) {
    return StringUtils.hasText(nilSubmissionForm.getAreaOfLaw())
        && sessionValidForAreaOfLaw(nilSubmissionForm);
  }

  static boolean sessionValidForAreaOfLaw(NilSubmissionForm nilSubmissionForm) {
    return StringUtils.hasText(nilSubmissionForm.getOffice())
        && sessionValidForOffice(nilSubmissionForm);
  }

  static boolean sessionValidForOffice(NilSubmissionForm nilSubmissionForm) {
    return true;
  }

  public static void nilSubmissionCleanseSession(
      NilSubmissionForm nilSubmissionForm, NilSubmissionPage page) {}
}
