package uk.gov.justice.laa.bulkclaim.controller.nilsubmission;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.NIL_SUBMISSION_FORM;
import static uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper.OIDC_USER;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.MEDIATION;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.bulkclaim.controller.BaseControllerTest;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.helper.SubmissionsResultSetTestHelper;
import uk.gov.justice.laa.bulkclaim.service.SubmissionPeriodService;

@WebMvcTest(NilSubmissionPeriodController.class)
class NilSubmissionPeriodControllerTest extends BaseControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private SubmissionPeriodService submissionPeriodService;

  @Test
  void whenFeatureFlagDisabled_allMappings_returnsErrorView() throws Exception {
    doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND))
        .when(featureFlagsConfig)
        .checkNilSubmissionEnabled();

    mockMvc
        .perform(
            get("/nil-submission/period")
                .with(oidcLogin().oidcUser(OIDC_USER))
                .sessionAttr(NIL_SUBMISSION_FORM, buildSessionForm()))
        .andExpect(status().isNotFound());

    mockMvc
        .perform(
            post("/nil-submission/period")
                .with(csrf())
                .with(oidcLogin().oidcUser(OIDC_USER))
                .sessionAttr(NIL_SUBMISSION_FORM, buildSessionForm()))
        .andExpect(status().isNotFound());
  }

  @Test
  void postNilSubmission_successView() throws Exception {
    stubPeriods(Map.of("JAN-2024", "January 2024"));
    var session = sessionWithForm(buildSessionForm());

    mockMvc
        .perform(
            post("/nil-submission/period")
                .with(csrf())
                .with(oidcLogin().oidcUser(OIDC_USER))
                .param("submissionPeriod", "JAN-2024")
                .session(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/nil-submission/reference"));

    var form = (NilSubmissionForm) session.getAttribute(NIL_SUBMISSION_FORM);
    org.junit.jupiter.api.Assertions.assertEquals("JAN-2024", form.getSubmissionPeriod());
  }

  @Test
  void postNilSubmission_invalidPeriod_returnsErrorView() throws Exception {
    stubPeriods(Map.of("JAN-2024", "January 2024"));
    var session = sessionWithForm(buildSessionForm());

    mockMvc
        .perform(
            post("/nil-submission/period")
                .with(csrf())
                .with(oidcLogin().oidcUser(OIDC_USER))
                .param("submissionPeriod", "INVALID-2024")
                .session(session))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/nil-submission/period"))
        .andExpect(model().attributeExists("submissionPeriods"))
        .andExpect(
            model()
                .attributeHasFieldErrorCode(
                    "nilSubmissionForm",
                    "submissionPeriod",
                    "nilSubmission.submissionPeriod.invalid"));

    var form = (NilSubmissionForm) session.getAttribute(NIL_SUBMISSION_FORM);
    assertNull(form.getSubmissionPeriod());
  }

  @Test
  void postNilSubmission_withoutRequiredSessionState_returnsNotFound() throws Exception {
    mockMvc
        .perform(
            post("/nil-submission/period")
                .with(csrf())
                .with(oidcLogin().oidcUser(OIDC_USER))
                .param("submissionPeriod", "JAN-2024")
                .sessionAttr(NIL_SUBMISSION_FORM, new NilSubmissionForm()))
        .andExpect(status().isNotFound());
  }

  @Test
  void getNilSubmission_successView() throws Exception {
    stubPeriods(Map.of("JAN-2024", "January 2024"));

    mockMvc
        .perform(
            get("/nil-submission/period")
                .with(oidcLogin().oidcUser(OIDC_USER))
                .sessionAttr(NIL_SUBMISSION_FORM, buildSessionForm()))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/nil-submission/period"))
        .andExpect(model().attributeExists("submissionPeriods"));
  }

  @Test
  void getNilSubmission_noPeriods_returnsInfoMessageView() throws Exception {
    stubPeriods(Map.of());

    mockMvc
        .perform(
            get("/nil-submission/period")
                .with(oidcLogin().oidcUser(OIDC_USER))
                .sessionAttr(NIL_SUBMISSION_FORM, buildSessionForm()))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/nil-submission/no-submission-periods"))
        .andExpect(model().attributeDoesNotExist("submissionPeriods"));
  }

  @Test
  void getSubmissionPeriod_sessionManagementCleansing() throws Exception {
    stubPeriods(Map.of("JAN-2024", "January 2024"));

    NilSubmissionForm form = buildSessionForm();
    form.setSubmissionPeriod("submissionPeriod1");
    form.setSubmissionReference("submissionReference1");
    var session = sessionWithForm(form);

    mockMvc
        .perform(
            get("/nil-submission/period").with(oidcLogin().oidcUser(OIDC_USER)).session(session))
        .andExpect(status().isOk());

    var updatedForm = (NilSubmissionForm) session.getAttribute(NIL_SUBMISSION_FORM);
    assertNotNull(updatedForm.getOffice());
    assertNotNull(updatedForm.getAreaOfLaw());
    assertNull(updatedForm.getSubmissionPeriod());
    assertNull(updatedForm.getSubmissionReference());
  }

  private void stubPeriods(Map<String, String> sortedPeriods) {
    var resultSet = SubmissionsResultSetTestHelper.getSubmissionsResultSet(0);
    when(submissionPeriodService.searchSubmissions(any())).thenReturn(resultSet);
    when(submissionPeriodService.getMonthsWithOutSubmissions(resultSet))
        .thenReturn(new LinkedHashMap<>(sortedPeriods));
    when(submissionPeriodService.sortSubmissionPeriods(new LinkedHashMap<>(sortedPeriods)))
        .thenReturn(new LinkedHashMap<>(sortedPeriods));
  }

  private NilSubmissionForm buildSessionForm() {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("officeA");
    form.setAreaOfLaw(MEDIATION);
    return form;
  }

  private MockHttpSession sessionWithForm(NilSubmissionForm form) {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(NIL_SUBMISSION_FORM, form);
    return session;
  }
}
