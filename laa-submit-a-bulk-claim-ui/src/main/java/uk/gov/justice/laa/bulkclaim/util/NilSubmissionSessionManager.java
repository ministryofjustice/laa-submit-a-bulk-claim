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

  public static NilSubmissionForm nilSubmissionCleanseSession(
      NilSubmissionForm nilSubmissionForm, NilSubmissionPage page) {

    switch (page) {
      case OTHER -> nilSubmissionForm = null;
      case OFFICE -> cleanseOffice(nilSubmissionForm);
      case AREA_OF_LAW -> cleanseAreaOfLaw(nilSubmissionForm);
      case SUBMISSION_PERIOD -> cleanseSubmissionPeriod(nilSubmissionForm);
    }

    return nilSubmissionForm;
  }

  static void cleanseOffice(NilSubmissionForm form) {
    form.setAreaOfLaw(null);
    form.setSubmissionPeriod(null);
    form.setScheduleReference(null);
  }

  static void cleanseAreaOfLaw(NilSubmissionForm form) {
    form.setSubmissionPeriod(null);
    form.setScheduleReference(null);
  }

  static void cleanseSubmissionPeriod(NilSubmissionForm form) {
    form.setScheduleReference(null);
  }
}
