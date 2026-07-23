package uk.gov.justice.laa.bulkclaim.controller.nilsubmission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.MEDIATION;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;

public class NilSubmissionScheduleReferenceControllerTest {
  @Mock private FeatureFlagsConfig featureFlagsConfig;
  @Mock private Model model;

  @InjectMocks private NilSubmissionScheduleReferenceController controller;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void whenFeatureFlagDisabled_all_mappings_returnsErrorView() {

    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "isNilSubmissionEnabled is false"))
        .when(featureFlagsConfig)
        .checkNilSubmissionEnabled();

    NilSubmissionForm form = new NilSubmissionForm();

    assertThrows(ResponseStatusException.class, () -> controller.getReference(form, model));

    verify(model, never()).addAttribute(eq("referenceLabel"), any());

    assertThrows(ResponseStatusException.class, () -> controller.postReference(form, "Reference"));
    assertNull(form.getScheduleReference());
  }

  @Test
  void whenFeatureFlagEnabled_getAreasOfLaw_addsAreasAndReturnsView() {

    NilSubmissionForm form = new NilSubmissionForm();
    form.setAreaOfLaw(MEDIATION);
    String view = controller.getReference(form, model);

    assertEquals("pages/nil-submission/reference", view);
  }

  @Test
  void postAreaOfLaw_setsFormAndRedirects() {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    form.setAreaOfLaw(MEDIATION);
    form.setSubmissionPeriod("OCT-2025");

    String view = controller.postReference(form, "reference");

    assertEquals("redirect:/nil-submission/summary-details", view);
    assertEquals("reference", form.getScheduleReference());
  }

  @Test
  void getScheduleReference_session_management_cleansing() {

    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    form.setAreaOfLaw(MEDIATION);
    form.setSubmissionPeriod("submissionPeriod1");
    form.setScheduleReference("scheduleReference1");

    controller.getReference(form, model);
    assertNotNull(form.getOffice());
    assertNotNull(form.getAreaOfLaw());
    assertNotNull(form.getSubmissionPeriod());
    assertNull(form.getScheduleReference());
  }
}
