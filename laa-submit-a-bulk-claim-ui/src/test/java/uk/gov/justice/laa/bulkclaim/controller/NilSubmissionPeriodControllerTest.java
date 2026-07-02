package uk.gov.justice.laa.bulkclaim.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
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
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionBase;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

@AutoConfigureMockMvc(addFilters = false)
class NilSubmissionPeriodControllerTest {

  @Mock private Model model;

  @Mock private DataClaimsRestClient claimsRestService;
  @Mock private FeatureFlagsConfig featureFlagsConfig;
  @Mock private MessageSource messageSource;

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

    when(claimsRestService.search(
            anyList(), any(), any(), any(), anyInt(), anyInt(), any(), any(), any()))
        .thenReturn(Mono.just(response));
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

    when(claimsRestService.search(
            anyList(), any(), any(), any(), anyInt(), anyInt(), any(), any(), any()))
        .thenReturn(Mono.just(response));

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

    when(claimsRestService.search(
            anyList(), any(), any(), any(), anyInt(), anyInt(), any(), any(), any()))
        .thenReturn(Mono.just(response));

    String view = nilSubmissionPeriodController.getSubmissionPeriods(form, model);
    assertEquals("pages/nil-submission-info-message", view);
    verify(model, times(0)).addAttribute(eq("submissionPeriods"), any(Map.class));
  }

  @Test
  void pastTwelveMonthGeneration() {
    Map<String, String> months = NilSubmissionPeriodController.getLastTwelveMonths();
    assertEquals(12, months.size());
    YearMonth ym = YearMonth.now().minusMonths(1);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH);
    assertTrue(months.containsKey(ym.format(formatter).toUpperCase()));
    ym = ym.minusYears(1);
    assertFalse(months.containsKey(ym.format(formatter).toUpperCase()));

    for (int i = 1; i <= 11; i++) {
      assertTrue(
          months.containsKey(ym.plusMonths(i).format(formatter).toUpperCase()),
          "Month " + ym.format(formatter).toUpperCase() + " should be removed");
    }
  }

  @Test
  void removedMatchingSubmissionPeriods() {
    SubmissionsResultSet results = getSubmissionsResultSet(2);
    Map<String, String> months = nilSubmissionPeriodController.getMonthsWithOutSubmissions(results);
    assertEquals(10, months.size());
    assertFalse(months.containsKey(results.getContent().get(0).getSubmissionPeriod()));
    assertFalse(months.containsKey(results.getContent().get(1).getSubmissionPeriod()));
  }

  @Test
  void getAreaOfLaw_session_management_cleansing() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);

    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    form.setAreaOfLaw("areaOfLaw1");
    form.setSubmissionPeriod("submissionPeriod1");
    form.setScheduleReference("scheduleReference1");

    when(claimsRestService.search(
            anyList(), any(), any(), any(), anyInt(), anyInt(), any(), any(), any()))
        .thenReturn(Mono.just(getSubmissionsResultSet(0)));

    nilSubmissionPeriodController.getSubmissionPeriods(form, model);
    assertNotNull(form.getOffice());
    assertNotNull(form.getAreaOfLaw());
    assertNotNull(form.getSubmissionPeriod());
    assertNull(form.getScheduleReference());
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
