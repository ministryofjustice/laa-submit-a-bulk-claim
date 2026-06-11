package uk.gov.justice.laa.bulkclaim.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper.getOidcUser;

import jakarta.servlet.http.HttpSession;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.search.SubmissionSearchQuery;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;
import uk.gov.justice.laa.bulkclaim.util.PaginationLinksBuilder;
import uk.gov.justice.laa.bulkclaim.util.PaginationUtil;
import uk.gov.justice.laa.bulkclaim.util.SubmissionPeriodUtil;
import uk.gov.justice.laa.bulkclaim.validation.SubmissionSearchValidator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionBase;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

@AutoConfigureMockMvc(addFilters = false)
class NilSubmissionControllerTest {

  @Mock private Model model;
  @Mock private BindingResult bindingResult;
  @Mock private HttpSession session;
  @Mock private SubmissionSearchValidator submissionSearchValidator;
  @Mock private DataClaimsRestClient claimsRestService;
  @Mock private PaginationUtil paginationUtil;
  @Mock private PaginationLinksBuilder paginationLinksBuilder;
  @Mock private SubmissionPeriodUtil submissionPeriodUtil;
  @Mock private OidcAttributeUtils oidcAttributeUtils;
  @Mock private FeatureFlagsConfig featureFlagsConfig;

  @InjectMocks private NilSubmissionController nilSubmissionController;

  private OidcUser oidcUser;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void whenFeatureFlagDisabled_returnsErrorView() {
    doReturn(false).when(featureFlagsConfig).getIsNilSubmissionEnabled();
    String view = nilSubmissionController.getNilSubmission(model, getOidcUser());

    assertEquals("error", view);
    verify(model, times(0)).addAttribute("userOffices");
  }

  @Test
  void getNilSubmission_SuccessView() {
    doReturn(true).when(featureFlagsConfig).getIsNilSubmissionEnabled();
    List<String> offices = List.of("officeA", "officeB");
    doReturn(offices).when(oidcAttributeUtils).getUserOffices(any(OidcUser.class));

    String view = nilSubmissionController.getNilSubmission(model, getOidcUser());
    assertEquals("pages/nil-submission", view);
    verify(model, times(1)).addAttribute("userOffices", offices);
  }

  @Test
  void getNilSubmission_officeWithAccess_returnsSubmissionPeriodsAndSearchQuery() {
    doReturn(true).when(featureFlagsConfig).getIsNilSubmissionEnabled();
    String office = "officeA";
    List<String> offices = List.of(office);
    doReturn(offices).when(oidcAttributeUtils).getUserOffices(any(OidcUser.class));
    final SubmissionsResultSet response = getSubmissionsResultSet();

    when(claimsRestService.search(
            anyList(), any(), any(), any(), anyInt(), anyInt(), any(), any(), any()))
        .thenReturn(Mono.just(response));

    String view =
        nilSubmissionController.getNilSubmission(
            model, office, AreaOfLaw.MEDIATION.getValue(), getOidcUser());
    assertEquals("pages/nil-submission-period", view);

    verify(model).addAttribute(eq("submissionPeriods"), any(Set.class));
    verify(model).addAttribute(eq("submissionSearchQuery"), any(SubmissionSearchQuery.class));

    // Verify claimsRestService.search called and that the submissionSearchQuery built contains the
    // office and areaOfLaw
    verify(claimsRestService)
        .search(
            eq(Collections.singletonList(office)),
            any(),
            eq(AreaOfLaw.MEDIATION),
            any(),
            eq(0),
            eq(12),
            any(),
            any(),
            any());
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

  @Test
  void getNilSubmission_officeWithoutAccess() {
    doReturn(true).when(featureFlagsConfig).getIsNilSubmissionEnabled();
    String office = "officeA";
    List<String> offices = List.of("otherOffice");
    doReturn(offices).when(oidcAttributeUtils).getUserOffices(any(OidcUser.class));
    assertThrows(
        SubmitBulkClaimException.class,
        () ->
            nilSubmissionController.getNilSubmission(
                model, office, AreaOfLaw.MEDIATION.getValue(), getOidcUser()));
  }

  @Test
  void pastTwelveMonthGeneration() {
    Set<String> months = NilSubmissionController.getLastTwelveMonths();
    assertEquals(12, months.size());
    YearMonth ym = YearMonth.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM uuuu", Locale.ENGLISH);
    assertTrue(months.contains(ym.format(formatter)));
    ym = ym.minusYears(1);
    assertFalse(months.contains(ym.format(formatter)));

    for (int i = 1; i <= 11; i++) {
      assertTrue(
          months.contains(ym.plusMonths(i).format(formatter)),
          "Month " + ym.format(formatter) + " should be removed");
    }
  }

  @Test
  void removedMatchingSubmissionPeriods() {
    String ym = YearMonth.now().minusMonths(1).format(DateTimeFormatter.ofPattern("MMM uuuu"));
    when(submissionPeriodUtil.getSubmissionPeriod(any())).thenReturn(ym);
    Set<String> months =
        nilSubmissionController.getMonthsWithOutSubmissions(getSubmissionsResultSet());
    assertEquals(11, months.size());
    assertFalse(months.contains(ym));
  }
}
