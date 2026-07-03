package uk.gov.justice.laa.bulkclaim.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.service.SubmissionPeriodService;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionBase;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

@AutoConfigureMockMvc(addFilters = false)
class NilSubmissionPeriodControllerTest {

  @Mock private Model model;

  @Mock private FeatureFlagsConfig featureFlagsConfig;
  @Mock private MessageSource messageSource;
  @Mock private SubmissionPeriodService submissionPeriodService;
  @InjectMocks private NilSubmissionPeriodController nilSubmissionPeriodController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void whenFeatureFlagDisabled_all_mappings_returnsErrorView() {
    NilSubmissionForm form = new NilSubmissionForm();

    doReturn(false).when(featureFlagsConfig).getIsNilSubmissionEnabled();

    assertEquals("error", nilSubmissionPeriodController.getSubmissionPeriods(form, model));
    verify(model, times(0)).addAttribute(eq("submissionPeriods"), any(Map.class));

    assertEquals("error", nilSubmissionPeriodController.postSubmissionPeriod(form, "JAN-2024"));
    assertNull(form.getSubmissionPeriod());
  }

  @Test
  void getNilSubmission_SuccessView() {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("officeA");
    form.setAreaOfLaw(AreaOfLaw.MEDIATION.getValue());
    doReturn(true).when(featureFlagsConfig).getIsNilSubmissionEnabled();

    final SubmissionsResultSet response = getSubmissionsResultSet(0);
    when(submissionPeriodService.searchSubmissions(any())).thenReturn(response);
    when(messageSource.getMessage(any(), any(), any())).thenReturn("Message");

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

    final SubmissionsResultSet response = getSubmissionsResultSet(0);

    when(submissionPeriodService.searchSubmissions(any())).thenReturn(response);

    String view = nilSubmissionPeriodController.postSubmissionPeriod(form, "JAN-2024");
    assertEquals("redirect:/nil-submission-reference", view);
    assertEquals("JAN-2024", form.getSubmissionPeriod());
  }

  @Test
  void postNilSubmission_InvalidPeriod_ReturnsErrorView() {}

  @Test
  void getNilSubmission_NoPeriods_ReturnsInfoMessageView() {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("officeA");
    form.setAreaOfLaw(AreaOfLaw.MEDIATION.getValue());
    doReturn(true).when(featureFlagsConfig).getIsNilSubmissionEnabled();

    final SubmissionsResultSet response = getSubmissionsResultSet(12);

    when(submissionPeriodService.searchSubmissions(any())).thenReturn(response);

    String view = nilSubmissionPeriodController.getSubmissionPeriods(form, model);
    assertEquals("pages/nil-submission-info-message", view);
    verify(model, times(0)).addAttribute(eq("submissionPeriods"), any(Map.class));
  }

  private @NonNull SubmissionsResultSet getSubmissionsResultSet(int noOfPeriods) {

    List<SubmissionBase> content = new ArrayList<>();
    YearMonth ym = YearMonth.now().minusMonths(1);
    for (int i = 0; i < noOfPeriods; i++) {
      SubmissionBase s1 = new SubmissionBase();
      content.add(
          s1.submissionPeriod(
              ym.format(DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH)).toUpperCase()));
      ym = ym.minusMonths(1);
    }

    final SubmissionsResultSet response = new SubmissionsResultSet();
    response.setContent(content);
    response.setTotalElements(2);
    response.setNumber(0);
    response.setSize(12);
    response.setTotalPages(1);
    return response;
  }
}
