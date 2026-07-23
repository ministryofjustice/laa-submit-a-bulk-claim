package uk.gov.justice.laa.bulkclaim.controller.nilsubmission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;

class NilSubmissionAreaOfLawControllerTest {

  @Mock private FeatureFlagsConfig featureFlagsConfig;
  @Mock private Model model;

  @InjectMocks private NilSubmissionAreaOfLawController controller;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void whenFeatureFlagDisabled_allMappings_returnsErrorView() {
    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "isNilSubmissionEnabled is false"))
        .when(featureFlagsConfig)
        .checkNilSubmissionEnabled();

    NilSubmissionForm form = new NilSubmissionForm();
    assertThrows(ResponseStatusException.class, () -> controller.getAreasOfLaw(form, model));
    verify(model, never()).addAttribute(eq("areasOfLaw"), any());

    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(form, "nilSubmissionForm");
    assertThrows(
        ResponseStatusException.class, () -> controller.postAreaOfLaw(form, bindingResult, model));
    assertNull(form.getAreaOfLaw());
  }

  @Test
  void whenFeatureFlagEnabled_getAreasOfLaw_addsAreasAndReturnsView() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);

    NilSubmissionForm form = new NilSubmissionForm();
    String view = controller.getAreasOfLaw(form, model);

    assertEquals("pages/nil-submission/areaoflaw", view);
    verify(model).addAttribute(eq("areasOfLaw"), anyMap());
  }

  @Test
  void postAreaOfLaw_setsFormAndRedirects() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    form.setAreaOfLaw(AreaOfLaw.CRIME_LOWER);

    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(form, "nilSubmissionForm");

    String view = controller.postAreaOfLaw(form, bindingResult, model);

    assertEquals("redirect:/nil-submission/period", view);
    assertEquals(AreaOfLaw.CRIME_LOWER, form.getAreaOfLaw());
  }

  @Test
  void postAreaOfLaw_whenBindingFails_returnsPageAndClearsSelection() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    form.setAreaOfLaw(AreaOfLaw.CRIME_LOWER);

    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(form, "nilSubmissionForm");
    bindingResult.rejectValue("areaOfLaw", "typeMismatch.nilSubmissionForm.areaOfLaw");

    String view = controller.postAreaOfLaw(form, bindingResult, model);

    assertEquals("pages/nil-submission/areaoflaw", view);
    assertNull(form.getAreaOfLaw());
    verify(model).addAttribute(eq("areasOfLaw"), anyMap());
    assertTrue(bindingResult.hasFieldErrors("areaOfLaw"));
    assertEquals(1, bindingResult.getFieldErrors("areaOfLaw").size());
  }

  @Test
  void postAreaOfLaw_whenAreaOfLawNotSelected_returnsPageAndAddsRequiredError() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");

    BeanPropertyBindingResult bindingResult =
        new BeanPropertyBindingResult(form, "nilSubmissionForm");

    String view = controller.postAreaOfLaw(form, bindingResult, model);

    assertEquals("pages/nil-submission/areaoflaw", view);
    assertNull(form.getAreaOfLaw());
    verify(model).addAttribute(eq("areasOfLaw"), anyMap());
    assertEquals(
        "nilSubmission.areaOfLaw.heading", bindingResult.getFieldError("areaOfLaw").getCode());
  }

  @Test
  void getAreaOfLaw_session_management_cleansing() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);

    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    form.setAreaOfLaw(AreaOfLaw.CRIME_LOWER);
    form.setSubmissionPeriod("submissionPeriod1");
    form.setScheduleReference("scheduleReference1");

    controller.getAreasOfLaw(form, model);
    assertNotNull(form.getOffice());
    assertNull(form.getAreaOfLaw());
    assertNull(form.getSubmissionPeriod());
    assertNull(form.getScheduleReference());
  }
}
