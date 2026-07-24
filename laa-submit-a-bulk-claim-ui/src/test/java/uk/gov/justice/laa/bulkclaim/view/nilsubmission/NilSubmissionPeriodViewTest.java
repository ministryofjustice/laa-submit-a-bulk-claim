package uk.gov.justice.laa.bulkclaim.view.nilsubmission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.MEDIATION;

import java.util.Map;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper;
import uk.gov.justice.laa.bulkclaim.controller.nilsubmission.NilSubmissionPeriodController;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.helper.SubmissionsResultSetTestHelper;
import uk.gov.justice.laa.bulkclaim.service.SubmissionPeriodService;
import uk.gov.justice.laa.bulkclaim.view.ViewTestBase;

@WebMvcTest(NilSubmissionPeriodController.class)
class NilSubmissionPeriodViewTest extends ViewTestBase {

  @MockitoBean private SubmissionPeriodService submissionPeriodService;

  NilSubmissionPeriodViewTest() {
    this.mapping = "/nil-submission/period";
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
                    .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
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
                post(mapping)
                    .with(csrf())
                    .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                    .session(session))
            .andReturn()
            .getResponse();

    var document = Jsoup.parse(doc.getContentAsString());

    assertThat(doc.getStatus()).isEqualTo(200);
    assertFormFieldHasInlineErrorMessage(document, "Select a submission period");
  }
}
