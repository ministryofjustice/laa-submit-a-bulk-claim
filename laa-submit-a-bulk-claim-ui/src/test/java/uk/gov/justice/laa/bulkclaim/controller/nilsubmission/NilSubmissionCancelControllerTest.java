package uk.gov.justice.laa.bulkclaim.controller.nilsubmission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.CRIME_LOWER;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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

    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "isNilSubmissionEnabled is false"))
        .when(featureFlagsConfig)
        .checkNilSubmissionEnabled();

    assertThrows(
        ResponseStatusException.class,
        () -> nilSubmissionCancelController.getCancel("UPLOAD", form));
  }

  @Test
  void whenFeatureFlagEnabledAndDestinationUpload_shouldRedirectToUpload() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);
    NilSubmissionForm form = new NilSubmissionForm();

    form.setOffice("office1");
    form.setAreaOfLaw(CRIME_LOWER);
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
    form.setAreaOfLaw(CRIME_LOWER);
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
