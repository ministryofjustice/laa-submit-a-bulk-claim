package uk.gov.justice.laa.bulkclaim.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;

class NilSubmissionSessionManagerTest {

  @Test
  void officePage_isAlwaysValid() {
    NilSubmissionForm form = new NilSubmissionForm();

    assertTrue(
        NilSubmissionSessionManager.isNilSubmissionSessionStateValid(
            form, NilSubmissionPage.OFFICE));
  }

  @Test
  void areaOfLawPage_requiresOffice() {
    NilSubmissionForm form = new NilSubmissionForm();

    assertFalse(
        NilSubmissionSessionManager.isNilSubmissionSessionStateValid(
            form, NilSubmissionPage.AREA_OF_LAW));

    form.setOffice("officeA");
    assertTrue(
        NilSubmissionSessionManager.isNilSubmissionSessionStateValid(
            form, NilSubmissionPage.AREA_OF_LAW));
  }

  @Test
  void submissionPeriodPage_session_validation() {
    NilSubmissionForm form = new NilSubmissionForm();

    assertFalse(
        NilSubmissionSessionManager.isNilSubmissionSessionStateValid(
            form, NilSubmissionPage.SUBMISSION_PERIOD));

    form.setAreaOfLaw("SOME_AREA");
    assertFalse(
        NilSubmissionSessionManager.isNilSubmissionSessionStateValid(
            form, NilSubmissionPage.SUBMISSION_PERIOD));
    form.setOffice("SOME_OFFICE");
    assertTrue(
        NilSubmissionSessionManager.isNilSubmissionSessionStateValid(
            form, NilSubmissionPage.SUBMISSION_PERIOD));
  }

  @Test
  void scheduleReferencePage_requiresSubmissionPeriodAndScheduleReference() {
    NilSubmissionForm form = new NilSubmissionForm();
    assertFalse(
        NilSubmissionSessionManager.isNilSubmissionSessionStateValid(
            form, NilSubmissionPage.SUBMISSION_PERIOD));

    form.setAreaOfLaw("SOME_AREA");
    assertFalse(
        NilSubmissionSessionManager.isNilSubmissionSessionStateValid(
            form, NilSubmissionPage.SUBMISSION_PERIOD));
    form.setOffice("SOME_OFFICE");
    assertTrue(
        NilSubmissionSessionManager.isNilSubmissionSessionStateValid(
            form, NilSubmissionPage.SUBMISSION_PERIOD));
    form.setSubmissionPeriod("JAN-2026");
    assertTrue(
        NilSubmissionSessionManager.isNilSubmissionSessionStateValid(
            form, NilSubmissionPage.SUBMISSION_PERIOD));
  }
}
