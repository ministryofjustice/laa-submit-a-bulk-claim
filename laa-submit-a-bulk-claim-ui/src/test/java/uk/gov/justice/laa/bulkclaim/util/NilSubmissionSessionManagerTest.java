package uk.gov.justice.laa.bulkclaim.util;

import static org.junit.jupiter.api.Assertions.*;

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

    form.setOffice("ABC123");
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

    form.setAreaOfLaw("MEDIATION");
    assertFalse(
        NilSubmissionSessionManager.isNilSubmissionSessionStateValid(
            form, NilSubmissionPage.SUBMISSION_PERIOD));
    form.setOffice("ABC123");
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

    form.setAreaOfLaw("MEDIATION");
    assertFalse(
        NilSubmissionSessionManager.isNilSubmissionSessionStateValid(
            form, NilSubmissionPage.SUBMISSION_PERIOD));
    form.setOffice("ABC123");
    assertTrue(
        NilSubmissionSessionManager.isNilSubmissionSessionStateValid(
            form, NilSubmissionPage.SUBMISSION_PERIOD));
    form.setSubmissionPeriod("JAN-2026");
    assertTrue(
        NilSubmissionSessionManager.isNilSubmissionSessionStateValid(
            form, NilSubmissionPage.SUBMISSION_PERIOD));
  }

  @Test
  void shouldReturnNullWhenPageIsOther() {
    NilSubmissionForm form = createPopulatedForm();

    NilSubmissionForm result =
        NilSubmissionSessionManager.nilSubmissionCleanseSession(form, NilSubmissionPage.OTHER);

    assertNull(result);
  }

  @Test
  void shouldCleanseOfficeFields() {
    NilSubmissionForm form = createPopulatedForm();

    NilSubmissionForm result =
        NilSubmissionSessionManager.nilSubmissionCleanseSession(form, NilSubmissionPage.OFFICE);

    assertSame(form, result);
    assertNull(result.getAreaOfLaw());
    assertNull(result.getSubmissionPeriod());
    assertNull(result.getScheduleReference());

    assertNotNull(result.getOffice());
  }

  @Test
  void shouldCleanseAreaOfLawFields() {
    NilSubmissionForm form = createPopulatedForm();

    NilSubmissionForm result =
        NilSubmissionSessionManager.nilSubmissionCleanseSession(
            form, NilSubmissionPage.AREA_OF_LAW);

    assertSame(form, result);

    assertNull(result.getSubmissionPeriod());
    assertNull(result.getScheduleReference());

    assertNotNull(result.getAreaOfLaw());
    assertNotNull(result.getOffice());
  }

  @Test
  void shouldCleanseSubmissionPeriodFields() {
    NilSubmissionForm form = createPopulatedForm();

    NilSubmissionForm result =
        NilSubmissionSessionManager.nilSubmissionCleanseSession(
            form, NilSubmissionPage.SUBMISSION_PERIOD);

    assertSame(form, result);

    assertNotNull(result.getOffice());
    assertNotNull(result.getAreaOfLaw());
    assertNotNull(result.getSubmissionPeriod());

    assertNull(result.getScheduleReference());
  }

  private NilSubmissionForm createPopulatedForm() {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("A123BC");
    form.setAreaOfLaw("MEDIATION");
    form.setSubmissionPeriod("JAN-2026");
    form.setScheduleReference("REF123");
    return form;
  }
}
