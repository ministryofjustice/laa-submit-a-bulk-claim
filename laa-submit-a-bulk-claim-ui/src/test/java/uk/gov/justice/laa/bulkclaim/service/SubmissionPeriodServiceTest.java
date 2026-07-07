package uk.gov.justice.laa.bulkclaim.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.helper.SubmissionsResultSetTestHelper;
import uk.gov.justice.laa.bulkclaim.util.DateWrapperUtil;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

@ExtendWith(MockitoExtension.class)
public class SubmissionPeriodServiceTest {

  @Mock private DataClaimsRestClient claimsRestService;
  @Mock private DateWrapperUtil dateWrapperUtil;

  @InjectMocks private SubmissionPeriodService submissionPeriodService;

  @Test
  void searchSubmissions_passesCorrectAreaAndDates() {
    when(dateWrapperUtil.now()).thenReturn(LocalDate.of(2024, 7, 3));

    SubmissionsResultSet response = new SubmissionsResultSet();
    when(claimsRestService.search(
            anyList(),
            any(),
            any(),
            anyList(),
            anyInt(),
            anyInt(),
            anyString(),
            anyString(),
            anyString()))
        .thenReturn(Mono.just(response));

    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    form.setAreaOfLaw(AreaOfLaw.MEDIATION.getValue());

    SubmissionsResultSet result = submissionPeriodService.searchSubmissions(form);

    assertSame(
        response,
        result,
        "Returned SubmissionsResultSet should be the same instance provided by the client");

    ArgumentCaptor<AreaOfLaw> areaCaptor = ArgumentCaptor.forClass(AreaOfLaw.class);
    ArgumentCaptor<String> fromCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);

    verify(claimsRestService)
        .search(
            anyList(),
            any(),
            areaCaptor.capture(),
            anyList(),
            anyInt(),
            anyInt(),
            fromCaptor.capture(),
            toCaptor.capture(),
            anyString());

    assertEquals(AreaOfLaw.MEDIATION, areaCaptor.getValue());
    assertEquals("2023-07-31", fromCaptor.getValue());
    assertEquals("2024-07-03", toCaptor.getValue());
  }

  @Test
  void searchSubmissions_withInvalidAreaPassesNullArea() {
    when(dateWrapperUtil.now()).thenReturn(LocalDate.of(2024, 7, 3));

    SubmissionsResultSet response = new SubmissionsResultSet();
    when(claimsRestService.search(
            anyList(),
            any(),
            any(),
            anyList(),
            anyInt(),
            anyInt(),
            anyString(),
            anyString(),
            anyString()))
        .thenReturn(Mono.just(response));

    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    form.setAreaOfLaw("INVALID_VALUE_THAT_DOES_NOT_MATCH_ENUM");

    submissionPeriodService.searchSubmissions(form);

    ArgumentCaptor<AreaOfLaw> areaCaptor = ArgumentCaptor.forClass(AreaOfLaw.class);
    verify(claimsRestService)
        .search(
            anyList(),
            any(),
            areaCaptor.capture(),
            anyList(),
            anyInt(),
            anyInt(),
            anyString(),
            anyString(),
            anyString());

    assertNull(
        areaCaptor.getValue(),
        "Invalid areaOfLaw strings should result in null AreaOfLaw passed to the client");
  }

  @Test
  void removal_of_submission_months_from_selection_list() {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("officeA");
    form.setAreaOfLaw(AreaOfLaw.MEDIATION.getValue());
    when(dateWrapperUtil.nowYearMonth()).thenReturn(YearMonth.now());
    when(dateWrapperUtil.now()).thenReturn(LocalDate.now());

    SubmissionsResultSet results = SubmissionsResultSetTestHelper.getSubmissionsResultSet(12);
    Map<String, String> validPeriods = submissionPeriodService.getMonthsWithOutSubmissions(results);
    assertTrue(validPeriods.isEmpty());

    results = SubmissionsResultSetTestHelper.getSubmissionsResultSet(1);
    validPeriods = submissionPeriodService.getMonthsWithOutSubmissions(results);
    assertEquals(11, validPeriods.size());
    assertFalse(validPeriods.containsKey(results.getContent().getFirst().getSubmissionPeriod()));
  }
}
