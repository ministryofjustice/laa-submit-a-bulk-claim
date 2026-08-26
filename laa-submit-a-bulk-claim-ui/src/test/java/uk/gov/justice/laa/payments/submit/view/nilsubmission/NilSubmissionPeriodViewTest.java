package uk.gov.justice.laa.payments.submit.view.nilsubmission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.MEDIATION;
import static uk.gov.justice.laa.payments.submit.controller.ControllerTestHelper.OIDC_USER;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.justice.laa.payments.submit.controller.nilsubmission.NilSubmissionPeriodController;
import uk.gov.justice.laa.payments.submit.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.payments.submit.helper.SubmissionsResultSetTestHelper;
import uk.gov.justice.laa.payments.submit.service.SubmissionPeriodService;
import uk.gov.justice.laa.payments.submit.view.ViewTestBase;

@WebMvcTest(NilSubmissionPeriodController.class)
class NilSubmissionPeriodViewTest extends ViewTestBase {

  @MockitoBean private SubmissionPeriodService submissionPeriodService;

  NilSubmissionPeriodViewTest() {
    this.mapping = "/nil-submission/period";
  }

  @Test
  void multipleSubmissionPeriodsShowsDropdown() {
    when(submissionPeriodService.searchSubmissions(any()))
        .thenReturn(SubmissionsResultSetTestHelper.getSubmissionsResultSet(0));
    when(submissionPeriodService.sortSubmissionPeriods(any()))
        .thenReturn(
            new LinkedHashMap<>(
                Map.ofEntries(
                    Map.entry("JAN-2024", "January 2024"),
                    Map.entry("FEB-2024", "February 2024"))));

    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("0P322F");
    form.setOfficeCount(2);
    form.setAreaOfLaw(MEDIATION);
    session.setAttribute("nilSubmissionForm", form);

    var doc = renderDocument();

    assertPageHasTitle(doc, "Create a nil submission");
    assertPageHasBackLink(doc);
    assertPageHasHint(doc, "nil-submission-hint", "Create a nil submission");
    assertPageHasHeading(doc, "Select a submission period");

    var summaryList = getFirstSummaryList(doc);
    assertThat(summaryList).hasSize(2);
    assertRowContainsValuesThenLink(
        summaryList.get(0), "Office account number", "Change", "/nil-submission/office", "0P322F");
    assertRowContainsValuesThenLink(
        summaryList.get(1), "Area of law", "Change", "/nil-submission/areaoflaw", "Mediation");

    assertPageHasLabel(doc, "submissionPeriod-input", "Submission period");
    assertDropDownList(
        doc, "Submission period", "Select a submission period", "January 2024", "February 2024");

    assertPageHasPrimaryButton(doc, "Continue");
    assertPageHasSecondaryLink(doc, "Cancel");
  }

  @Test
  void noSubmissionPeriodsShowsMessage() {
    when(submissionPeriodService.searchSubmissions(any()))
        .thenReturn(SubmissionsResultSetTestHelper.getSubmissionsResultSet(0));
    when(submissionPeriodService.sortSubmissionPeriods(any())).thenReturn(Map.of());

    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("0P322F");
    form.setOfficeCount(2);
    form.setAreaOfLaw(MEDIATION);
    session.setAttribute("nilSubmissionForm", form);

    var doc = renderDocument();

    assertPageHasTitle(doc, "Create a nil submission");
    assertPageDoesNotHaveBackLink(doc);
    assertPageHasHeading(doc, "You cannot submit a nil submission");

    var summaryList = getFirstSummaryList(doc);
    assertThat(summaryList).hasSize(2);
    assertRowContainsValuesThenLink(
        summaryList.get(0), "Office account number", "Change", "/nil-submission/office", "0P322F");
    assertRowContainsValuesThenLink(
        summaryList.get(1), "Area of law", "Change", "/nil-submission/areaoflaw", "Mediation");

    assertPageBodyText(
        doc,
        "No submission periods are available for the combination of office account number and"
            + " area of law you selected. Go back to submit a bulk claim or search for a"
            + " previous submission");

    assertPageHasLink(
        doc,
        "submit-a-bulk-claim",
        "submit a bulk claim",
        "/nil-submission/cancel?destination=UPLOAD");
    assertPageHasLink(
        doc,
        "search-for-previous-submission",
        "search for a previous submission",
        "/nil-submission/cancel?destination=SEARCH");
  }

  @Test
  void invalidSubmissionPeriodShowsInlineError() throws Exception {
    when(submissionPeriodService.searchSubmissions(any()))
        .thenReturn(SubmissionsResultSetTestHelper.getSubmissionsResultSet(0));
    when(submissionPeriodService.sortSubmissionPeriods(any()))
        .thenReturn(Map.of("JAN-2024", "January 2024"));

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("submissionPeriod", "INVALID-2024");

    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("Office1");
    form.setAreaOfLaw(MEDIATION);
    session.setAttribute("nilSubmissionForm", form);

    var doc =
        mockMvc
            .perform(
                post(mapping)
                    .with(csrf())
                    .with(oidcLogin().oidcUser(OIDC_USER))
                    .params(params)
                    .session(session))
            .andReturn()
            .getResponse();

    var document = Jsoup.parse(doc.getContentAsString());

    assertThat(doc.getStatus()).isEqualTo(200);
    assertFormFieldHasErrorMessage(document, "Select a valid submission period");
  }

  @Test
  void submissionPeriodNotSelectedShowsInlineError() throws Exception {
    when(submissionPeriodService.searchSubmissions(any()))
        .thenReturn(SubmissionsResultSetTestHelper.getSubmissionsResultSet(0));
    when(submissionPeriodService.sortSubmissionPeriods(any()))
        .thenReturn(Map.of("JAN-2024", "January 2024"));

    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("Office1");
    form.setAreaOfLaw(MEDIATION);
    session.setAttribute("nilSubmissionForm", form);

    var doc =
        mockMvc
            .perform(
                post(mapping).with(csrf()).with(oidcLogin().oidcUser(OIDC_USER)).session(session))
            .andReturn()
            .getResponse();

    var document = Jsoup.parse(doc.getContentAsString());

    assertThat(doc.getStatus()).isEqualTo(200);
    assertFormFieldHasInlineErrorMessage(document, "Select a submission period");
  }
}
