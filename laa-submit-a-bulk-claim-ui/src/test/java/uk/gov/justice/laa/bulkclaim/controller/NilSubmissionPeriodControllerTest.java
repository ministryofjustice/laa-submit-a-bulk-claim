package uk.gov.justice.laa.bulkclaim.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.helper.SubmissionsResultSetTestHelper;
import uk.gov.justice.laa.bulkclaim.service.SubmissionPeriodService;
import uk.gov.justice.laa.bulkclaim.util.DateWrapperUtil;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

@AutoConfigureMockMvc(addFilters = false)
class NilSubmissionPeriodControllerTest extends BaseControllerTest {

  @Mock private Model model;
  @Mock private SubmissionPeriodService submissionPeriodService;
  @Mock private DateWrapperUtil dateWrapperUtil;

  @InjectMocks private NilSubmissionPeriodController nilSubmissionPeriodController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void whenFeatureFlagDisabled_all_mappings_returnsErrorView() {
    NilSubmissionForm form = new NilSubmissionForm();
    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "isNilSubmissionEnabled is false"))
        .when(featureFlagsConfig)
        .checkNilSubmissionEnabled();

    assertThrows(
        ResponseStatusException.class,
        () -> nilSubmissionPeriodController.getSubmissionPeriods(form, model));
    verify(model, never()).addAttribute(eq("submissionPeriods"), any());

    assertThrows(
        ResponseStatusException.class,
        () -> nilSubmissionPeriodController.postSubmissionPeriod(form, "JAN-2024"));
    assertNull(form.getSubmissionPeriod());
  }

  @Test
  void getNilSubmission_SuccessView() {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("officeA");
    form.setAreaOfLaw(AreaOfLaw.MEDIATION.getValue());

    when(submissionPeriodService.sortSubmissionPeriods(any()))
        .thenReturn(Map.of("JAN-2024", "January 2024"));
    when(dateWrapperUtil.nowYearMonth()).thenReturn(YearMonth.now());
    when(dateWrapperUtil.now()).thenReturn(LocalDate.now());

    String view = nilSubmissionPeriodController.getSubmissionPeriods(form, model);
    assertEquals("pages/nil-submission-period", view);
    verify(model, times(1)).addAttribute(eq("submissionPeriods"), any(Map.class));
  }

  @Test
  void postNilSubmission_SuccessView() {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("officeA");
    form.setAreaOfLaw(AreaOfLaw.MEDIATION.getValue());
    doReturn(true).when(featureFlagsConfig).getIsNilSubmissionEnabled();

    final SubmissionsResultSet response = SubmissionsResultSetTestHelper.getSubmissionsResultSet(0);

    when(submissionPeriodService.searchSubmissions(any())).thenReturn(response);

    String view = nilSubmissionPeriodController.postSubmissionPeriod(form, "JAN-2024");
    assertEquals("redirect:/nil-submission-reference", view);
    assertEquals("JAN-2024", form.getSubmissionPeriod());
  }

  @Test
  void postNilSubmission_InvalidPeriod_ReturnsErrorView() {
    // TODO
  }

  @Test
  void getNilSubmission_NoPeriods_ReturnsInfoMessageView() {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("officeA");
    form.setAreaOfLaw(AreaOfLaw.MEDIATION.getValue());
    doReturn(true).when(featureFlagsConfig).getIsNilSubmissionEnabled();

    final SubmissionsResultSet response =
        SubmissionsResultSetTestHelper.getSubmissionsResultSet(12);

    when(submissionPeriodService.searchSubmissions(any())).thenReturn(response);
    when(dateWrapperUtil.nowYearMonth()).thenReturn(YearMonth.now());
    when(dateWrapperUtil.now()).thenReturn(LocalDate.now());

    String view = nilSubmissionPeriodController.getSubmissionPeriods(form, model);
    assertEquals("pages/nil-submission-no-submission-periods", view);
    verify(model, times(0)).addAttribute(eq("submissionPeriods"), any(Map.class));
  }

  @Test
  void getSubmissionPeriod_session_management_cleansing() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);

    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    form.setAreaOfLaw("areaOfLaw1");
    form.setSubmissionPeriod("submissionPeriod1");
    form.setScheduleReference("scheduleReference1");

    nilSubmissionPeriodController.getSubmissionPeriods(form, model);
    assertNotNull(form.getOffice());
    assertNotNull(form.getAreaOfLaw());
    assertNull(form.getSubmissionPeriod());
    assertNull(form.getScheduleReference());
  }
}
