package uk.gov.justice.laa.bulkclaim.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpSession;
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
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.util.PaginationLinksBuilder;
import uk.gov.justice.laa.bulkclaim.util.PaginationUtil;
import uk.gov.justice.laa.bulkclaim.util.SubmissionPeriodUtil;
import uk.gov.justice.laa.bulkclaim.validation.SubmissionSearchValidator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionBase;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

@AutoConfigureMockMvc(addFilters = false)
class NilSubmissionPeriodControllerTest {

  @Mock private Model model;
  @Mock private BindingResult bindingResult;
  @Mock private HttpSession session;
  @Mock private SubmissionSearchValidator submissionSearchValidator;
  @Mock private DataClaimsRestClient claimsRestService;
  @Mock private PaginationUtil paginationUtil;
  @Mock private PaginationLinksBuilder paginationLinksBuilder;
  @Mock private SubmissionPeriodUtil submissionPeriodUtil;
  @Mock private FeatureFlagsConfig featureFlagsConfig;

  @InjectMocks private NilSubmissionPeriodController nilSubmissionPeriodController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void whenFeatureFlagDisabled_returnsErrorView() {
    NilSubmissionForm form = new NilSubmissionForm();

    doReturn(false).when(featureFlagsConfig).getIsNilSubmissionEnabled();
    String view = nilSubmissionPeriodController.getSubmissionPeriods(form, model);

    assertEquals("error", view);
    verify(model, times(0)).addAttribute("userOffices");
  }

  @Test
  void getNilSubmission_SuccessView() {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("officeA");
    form.setAreaOfLaw(AreaOfLaw.MEDIATION.getValue());
    doReturn(true).when(featureFlagsConfig).getIsNilSubmissionEnabled();

    final SubmissionsResultSet response = getSubmissionsEmptyPeriodResultSet();

    when(claimsRestService.search(
            anyList(), any(), any(), any(), anyInt(), anyInt(), any(), any(), any()))
        .thenReturn(Mono.just(response));

    String view = nilSubmissionPeriodController.getSubmissionPeriods(form, model);
    assertEquals("pages/nil-submission-period", view);
    verify(model, times(1)).addAttribute(eq("submissionPeriods"), any(Map.class));
  }

  private static @NonNull SubmissionsResultSet getSubmissionsResultSet() {
    SubmissionBase s1 = new SubmissionBase();
    SubmissionBase s2 = new SubmissionBase();
    List<SubmissionBase> content = new ArrayList<>();
    content.add(s1.submissionPeriod("January 2026"));
    content.add(s2.submissionPeriod("February 2026"));

    final SubmissionsResultSet response = new SubmissionsResultSet();
    response.setContent(content);
    response.setTotalElements(2);
    response.setNumber(0);
    response.setSize(12);
    response.setTotalPages(1);
    return response;
  }

  private static @NonNull SubmissionsResultSet getSubmissionsEmptyPeriodResultSet() {
    List<SubmissionBase> content = new ArrayList<>();

    final SubmissionsResultSet response = new SubmissionsResultSet();
    response.setContent(content);
    response.setTotalElements(2);
    response.setNumber(0);
    response.setSize(12);
    response.setTotalPages(1);
    return response;
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
    String ym =
        YearMonth.now()
            .minusMonths(1)
            .format(DateTimeFormatter.ofPattern("MMM-uuuu"))
            .toUpperCase();
    when(submissionPeriodUtil.getSubmissionPeriod(any())).thenReturn(ym);
    Map<String, String> months =
        nilSubmissionPeriodController.getMonthsWithOutSubmissions(getSubmissionsResultSet());
    assertEquals(11, months.size());
    assertFalse(months.containsKey(ym));
  }
}
