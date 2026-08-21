package uk.gov.justice.laa.payments.submit.util;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.payments.submit.dto.submission.NilSubmissionForm;

@UtilityClass
public class NilSubmissionSessionManager {

  public static boolean isNilSubmissionSessionStateValid(
      NilSubmissionForm nilSubmissionForm, NilSubmissionPage page) {
    return switch (page) {
      case OFFICE -> sessionValidForOffice(nilSubmissionForm);
      case AREA_OF_LAW -> sessionValidForAreaOfLaw(nilSubmissionForm);
      case SUBMISSION_PERIOD -> sessionValidForSubmissionPeriod(nilSubmissionForm);
      case SUBMISSION_REFERENCE -> sessionValidForSubmissionReference(nilSubmissionForm);
      case SUMMARY_DETAILS -> sessionValidForSummaryDetails(nilSubmissionForm);
      case OTHER -> true;
    };
  }

  static boolean sessionValidForSubmissionReference(NilSubmissionForm nilSubmissionForm) {
    return StringUtils.hasText(nilSubmissionForm.getSubmissionPeriod())
        && sessionValidForSubmissionPeriod(nilSubmissionForm);
  }

  static boolean sessionValidForSummaryDetails(NilSubmissionForm nilSubmissionForm) {
    return StringUtils.hasText(nilSubmissionForm.getSubmissionReference())
        && sessionValidForSubmissionReference(nilSubmissionForm);
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

    return switch (page) {
      case OFFICE, OTHER -> cleanseAllNilSubmissionSessionValues(nilSubmissionForm);
      case AREA_OF_LAW -> cleanseSessionValuesPriorToAreaOfLawSelection(nilSubmissionForm);
      case SUBMISSION_PERIOD ->
          cleanseSessionValuesPriorToSubmissionPeriodSelection(nilSubmissionForm);
      case SUBMISSION_REFERENCE ->
          cleanseSessionValuesPriorToSubmissionReferenceEntry(nilSubmissionForm);
      case SUMMARY_DETAILS -> nilSubmissionForm;
    };
  }

  static NilSubmissionForm cleanseAllNilSubmissionSessionValues(NilSubmissionForm form) {
    form.setOffice(null);
    form.setAreaOfLaw(null);
    form.setSubmissionPeriod(null);
    form.setSubmissionReference(null);
    return form;
  }

  static NilSubmissionForm cleanseSessionValuesPriorToAreaOfLawSelection(NilSubmissionForm form) {
    form.setAreaOfLaw(null);
    form.setSubmissionPeriod(null);
    form.setSubmissionReference(null);
    return form;
  }

  static NilSubmissionForm cleanseSessionValuesPriorToSubmissionPeriodSelection(
      NilSubmissionForm form) {
    form.setSubmissionPeriod(null);
    form.setSubmissionReference(null);
    return form;
  }

  static NilSubmissionForm cleanseSessionValuesPriorToSubmissionReferenceEntry(
      NilSubmissionForm form) {
    form.setSubmissionReference(null);
    return form;
  }

  public static void validateSessionState(
      NilSubmissionForm nilSubmissionForm, NilSubmissionPage page) {
    if (!isNilSubmissionSessionStateValid(nilSubmissionForm, page)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid session state");
    }
  }
}
