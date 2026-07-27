package uk.gov.justice.laa.bulkclaim.util;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;

@UtilityClass
public class NilSubmissionSessionManager {

  public static boolean isNilSubmissionSessionStateValid(
      NilSubmissionForm nilSubmissionForm, NilSubmissionPage page) {
    return switch (page) {
      case OFFICE -> sessionValidForOffice(nilSubmissionForm);
      case AREA_OF_LAW -> sessionValidForAreaOfLaw(nilSubmissionForm);
      case SUBMISSION_PERIOD -> sessionValidForSubmissionPeriod(nilSubmissionForm);
      case SCHEDULE_REFERENCE -> sessionValidForScheduleReference(nilSubmissionForm);
      case SUMMARY_DETAILS -> sessionValidForSummaryDetails(nilSubmissionForm);
      case OTHER -> true;
    };
  }

  static boolean sessionValidForScheduleReference(NilSubmissionForm nilSubmissionForm) {
    return StringUtils.hasText(nilSubmissionForm.getSubmissionPeriod())
        && sessionValidForSubmissionPeriod(nilSubmissionForm);
  }

  static boolean sessionValidForSummaryDetails(NilSubmissionForm nilSubmissionForm) {
    return StringUtils.hasText(nilSubmissionForm.getScheduleReference())
        && sessionValidForScheduleReference(nilSubmissionForm);
  }

  static boolean sessionValidForSubmissionPeriod(NilSubmissionForm nilSubmissionForm) {
    return nilSubmissionForm.getAreaOfLaw() != null && sessionValidForAreaOfLaw(nilSubmissionForm);
  }

  static boolean sessionValidForAreaOfLaw(NilSubmissionForm nilSubmissionForm) {
    return StringUtils.hasText(nilSubmissionForm.getOffice())
        && sessionValidForOffice(nilSubmissionForm);
  }

  static boolean sessionValidForOffice(NilSubmissionForm nilSubmissionForm) {
    return true;
  }

  public static NilSubmissionForm cleanseSession(
      NilSubmissionForm nilSubmissionForm, NilSubmissionPage page) {

    var form =
        switch (page) {
          case OFFICE, OTHER -> cleanseAllNilSubmissionSessionValues(nilSubmissionForm);
          case AREA_OF_LAW -> cleanseSessionValuesPriorToAreaOfLawSelection(nilSubmissionForm);
          case SUBMISSION_PERIOD ->
              cleanseSessionValuesPriorToSubmissionPeriodSelection(nilSubmissionForm);
          case SCHEDULE_REFERENCE ->
              cleanseSessionValuesPriorToScheduleReferenceEntry(nilSubmissionForm);
          case SUMMARY_DETAILS -> nilSubmissionForm;
        };

    validateSessionState(form, page);

    return form;
  }

  static NilSubmissionForm cleanseAllNilSubmissionSessionValues(NilSubmissionForm form) {
    form.setOffice(null);
    form.setAreaOfLaw(null);
    form.setSubmissionPeriod(null);
    form.setScheduleReference(null);
    return form;
  }

  static NilSubmissionForm cleanseSessionValuesPriorToAreaOfLawSelection(NilSubmissionForm form) {
    form.setAreaOfLaw(null);
    form.setSubmissionPeriod(null);
    form.setScheduleReference(null);
    return form;
  }

  static NilSubmissionForm cleanseSessionValuesPriorToSubmissionPeriodSelection(
      NilSubmissionForm form) {
    form.setSubmissionPeriod(null);
    form.setScheduleReference(null);
    return form;
  }

  static NilSubmissionForm cleanseSessionValuesPriorToScheduleReferenceEntry(
      NilSubmissionForm form) {
    form.setScheduleReference(null);
    return form;
  }

  private static void validateSessionState(
      NilSubmissionForm nilSubmissionForm, NilSubmissionPage page) {
    if (!isNilSubmissionSessionStateValid(nilSubmissionForm, page)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid session state");
    }
  }
}
