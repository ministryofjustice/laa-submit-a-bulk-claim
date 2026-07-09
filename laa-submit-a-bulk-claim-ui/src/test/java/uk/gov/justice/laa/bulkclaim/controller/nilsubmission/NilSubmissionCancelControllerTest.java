package uk.gov.justice.laa.bulkclaim.controller.nilsubmission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;

class NilSubmissionCancelControllerTest {

  @Mock private FeatureFlagsConfig featureFlagsConfig;

  @InjectMocks private NilSubmissionCancelController nilSubmissionCancelController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void whenFeatureFlagDisabled_shouldReturnErrorView() {
    NilSubmissionForm form = new NilSubmissionForm();

    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(false);

    assertEquals("error", nilSubmissionCancelController.getCancel("UPLOAD", form));

    verify(featureFlagsConfig).getIsNilSubmissionEnabled();
    verifyNoMoreInteractions(featureFlagsConfig);
  }

  @Test
  void whenFeatureFlagEnabledAndDestinationUpload_shouldRedirectToUpload() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);
    NilSubmissionForm form = new NilSubmissionForm();

    form.setOffice("office1");
    form.setAreaOfLaw("areaOfLaw1");
    form.setSubmissionPeriod("submissionPeriod1");
    form.setScheduleReference("scheduleReference1");

    String result = nilSubmissionCancelController.getCancel("UPLOAD", form);

    assertEquals("redirect:/upload", result);
    assertNull(form.getOffice());
    assertNull(form.getAreaOfLaw());
    assertNull(form.getSubmissionPeriod());
    assertNull(form.getScheduleReference());
  }

  @Test
  void whenFeatureFlagEnabledAndDestinationSearch_shouldRedirectToUpload_SessionRetained() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);
    NilSubmissionForm form = new NilSubmissionForm();

    form.setOffice("office1");
    form.setAreaOfLaw("areaOfLaw1");
    form.setSubmissionPeriod("submissionPeriod1");
    form.setScheduleReference("scheduleReference1");

    String result = nilSubmissionCancelController.getCancel("SEARCH", form);

    assertEquals("redirect:/submissions/search", result);
    assertNotNull(form.getOffice());
    assertNotNull(form.getAreaOfLaw());
    assertNotNull(form.getSubmissionPeriod());
    assertNotNull(form.getScheduleReference());
  }
}
