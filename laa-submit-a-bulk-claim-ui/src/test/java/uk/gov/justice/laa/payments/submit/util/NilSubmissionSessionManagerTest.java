package uk.gov.justice.laa.payments.submit.util;

import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.MEDIATION;

import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.payments.submit.dto.submission.NilSubmissionForm;

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

    form.setAreaOfLaw(MEDIATION);
    assertFalse(
        NilSubmissionSessionManager.isNilSubmissionSessionStateValid(
            form, NilSubmissionPage.SUBMISSION_PERIOD));
    form.setOffice("ABC123");
    assertTrue(
        NilSubmissionSessionManager.isNilSubmissionSessionStateValid(
            form, NilSubmissionPage.SUBMISSION_PERIOD));
  }

  @Test
  void submissionReferencePage_requiresSubmissionPeriod() {
    NilSubmissionForm form = new NilSubmissionForm();
    assertFalse(
        NilSubmissionSessionManager.isNilSubmissionSessionStateValid(
            form, NilSubmissionPage.SUBMISSION_REFERENCE));

    form.setAreaOfLaw(MEDIATION);
    assertFalse(
        NilSubmissionSessionManager.isNilSubmissionSessionStateValid(
            form, NilSubmissionPage.SUBMISSION_REFERENCE));
    form.setOffice("ABC123");
    assertFalse(
        NilSubmissionSessionManager.isNilSubmissionSessionStateValid(
            form, NilSubmissionPage.SUBMISSION_REFERENCE));
    form.setSubmissionPeriod("JAN-2026");
    assertTrue(
        NilSubmissionSessionManager.isNilSubmissionSessionStateValid(
            form, NilSubmissionPage.SUBMISSION_REFERENCE));
  }

  @Test
  void shouldEmptySessionWhenPageIsOther() {
    NilSubmissionForm form = createPopulatedForm();

    NilSubmissionSessionManager.cleanseSession(form, NilSubmissionPage.OTHER);

    assertNull(form.getOffice());
    assertNull(form.getAreaOfLaw());
    assertNull(form.getSubmissionPeriod());
    assertNull(form.getSubmissionReference());
  }

  @Test
  void shouldCleanseOfficeFields() {
    NilSubmissionForm form = createPopulatedForm();

    NilSubmissionForm result =
        NilSubmissionSessionManager.cleanseSession(form, NilSubmissionPage.OFFICE);

    assertSame(form, result);
    assertNull(result.getAreaOfLaw());
    assertNull(result.getSubmissionPeriod());
    assertNull(result.getSubmissionReference());
    assertNull(result.getOffice());
  }

  @Test
  void shouldCleanseSessionValuesPriorToAreaOfLawSelectionFields() {
    NilSubmissionForm form = createPopulatedForm();

    NilSubmissionForm result =
        NilSubmissionSessionManager.cleanseSession(form, NilSubmissionPage.AREA_OF_LAW);

    assertSame(form, result);

    assertNull(result.getSubmissionPeriod());
    assertNull(result.getSubmissionReference());
    assertNull(result.getAreaOfLaw());

    assertNotNull(result.getOffice());
  }

  @Test
  void shouldCleanseSessionValuesPriorToSubmissionPeriodSelectionFields() {
    NilSubmissionForm form = createPopulatedForm();

    NilSubmissionForm result =
        NilSubmissionSessionManager.cleanseSession(form, NilSubmissionPage.SUBMISSION_PERIOD);

    assertSame(form, result);

    assertNotNull(result.getOffice());
    assertNotNull(result.getAreaOfLaw());

    assertNull(result.getSubmissionPeriod());
    assertNull(result.getSubmissionReference());
  }

  @Test
  void shouldCleanseSessionValuesPriorToSubmissionReferenceEntryFields() {
    NilSubmissionForm form = createPopulatedForm();

    NilSubmissionForm result =
        NilSubmissionSessionManager.cleanseSession(form, NilSubmissionPage.SUBMISSION_REFERENCE);

    assertSame(form, result);

    assertNotNull(result.getOffice());
    assertNotNull(result.getAreaOfLaw());
    assertNotNull(result.getSubmissionPeriod());

    assertNull(result.getSubmissionReference());
  }

  @Test
  void cleanseSession_doesNotValidateSessionState() {
    NilSubmissionForm form = new NilSubmissionForm();

    assertDoesNotThrow(
        () -> NilSubmissionSessionManager.cleanseSession(form, NilSubmissionPage.AREA_OF_LAW));
  }

  @Test
  void validateSessionState_throwsWhenSessionStateIsInvalid() {
    NilSubmissionForm form = new NilSubmissionForm();

    var exception =
        assertThrows(
            org.springframework.web.server.ResponseStatusException.class,
            () ->
                NilSubmissionSessionManager.validateSessionState(
                    form, NilSubmissionPage.AREA_OF_LAW));

    assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, exception.getStatusCode());
  }

  private NilSubmissionForm createPopulatedForm() {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("A123BC");
    form.setAreaOfLaw(MEDIATION);
    form.setSubmissionPeriod("JAN-2026");
    form.setSubmissionReference("REF123");
    return form;
  }
}
