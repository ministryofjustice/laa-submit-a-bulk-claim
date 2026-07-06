package uk.gov.justice.laa.bulkclaim.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;

class NilSubmissionAreaOfLawControllerTest {

  @Mock private FeatureFlagsConfig featureFlagsConfig;
  @Mock private Model model;

  @InjectMocks private NilSubmissionAreaOfLawController controller;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void whenFeatureFlagDisabled_all_mappings_returnsErrorView() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(false);

    NilSubmissionForm form = new NilSubmissionForm();

    assertEquals("error", controller.getAreasOfLaw(form, model));
    verify(model, never()).addAttribute(eq("areasOfLaw"), any());

    assertEquals("error", controller.postAreaOfLaw(form, "SOME_AREA"));
    assertNull(form.getAreaOfLaw());
  }

  @Test
  void whenFeatureFlagEnabled_getAreasOfLaw_addsAreasAndReturnsView() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);

    NilSubmissionForm form = new NilSubmissionForm();
    String view = controller.getAreasOfLaw(form, model);

    assertEquals("pages/nil-submission-areaoflaw", view);
    verify(model).addAttribute(eq("areasOfLaw"), anyMap());
  }

  @Test
  void postAreaOfLaw_setsFormAndRedirects() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");

    String view = controller.postAreaOfLaw(form, "SOME_AREA");

    assertEquals("redirect:/nil-submission-period", view);
    assertEquals("SOME_AREA", form.getAreaOfLaw());
  }

  @Test
  void postAreaOfLaw_whenAreaOfLawIsInvalid_returnsErrorView() {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");

    String view_on_null = controller.postAreaOfLaw(form, null);
    assertEquals("error", view_on_null);
    assertNull(form.getAreaOfLaw());

    String view_on_invalid = controller.postAreaOfLaw(form, "NOT_A_AREA_OF_LAW");
    assertEquals("error", view_on_invalid);
    assertNull(form.getAreaOfLaw());
  }

  @Test
  void getAreaOfLaw_session_management_cleansing() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);

    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    form.setAreaOfLaw("areaOfLaw1");
    form.setSubmissionPeriod("submissionPeriod1");
    form.setScheduleReference("scheduleReference1");

    controller.getAreasOfLaw(form, model);
    assertNotNull(form.getOffice());
    assertNull(form.getAreaOfLaw());
    assertNull(form.getSubmissionPeriod());
    assertNull(form.getScheduleReference());
  }
}
