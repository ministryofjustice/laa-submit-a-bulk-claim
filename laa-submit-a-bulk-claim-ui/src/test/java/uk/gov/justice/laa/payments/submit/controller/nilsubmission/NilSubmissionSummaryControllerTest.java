package uk.gov.justice.laa.payments.submit.controller.nilsubmission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.MEDIATION;
import static uk.gov.justice.laa.payments.submit.constants.SessionConstants.NIL_SUBMISSION_FORM;
import static uk.gov.justice.laa.payments.submit.controller.ControllerTestHelper.OIDC_USER;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.payments.submit.controller.BaseControllerTest;
import uk.gov.justice.laa.payments.submit.dto.NilSubmissionResult;
import uk.gov.justice.laa.payments.submit.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.payments.submit.service.NilSubmissionService;

@WebMvcTest(NilSubmissionsSummaryController.class)
class NilSubmissionSummaryControllerTest extends BaseControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private NilSubmissionService nilSubmissionService;

  @Test
  void whenFeatureFlagDisabled_allMappings_returnsErrorView() throws Exception {
    doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND))
        .when(featureFlagsConfig)
        .checkNilSubmissionEnabled();

    mockMvc
        .perform(
            get("/nil-submission/summary-details")
                .with(oidcLogin().oidcUser(OIDC_USER))
                .sessionAttr(NIL_SUBMISSION_FORM, buildSessionForm()))
        .andExpect(status().isNotFound());

    mockMvc
        .perform(
            post("/nil-submission/summary-details")
                .with(csrf())
                .with(oidcLogin().oidcUser(OIDC_USER))
                .sessionAttr(NIL_SUBMISSION_FORM, buildSessionForm()))
        .andExpect(status().isNotFound());

    verifyNoInteractions(nilSubmissionService);
  }

  @Test
  void whenFeatureFlagEnabled_getSummary_returnsSummaryView() throws Exception {
    mockMvc
        .perform(
            get("/nil-submission/summary-details")
                .with(oidcLogin().oidcUser(OIDC_USER))
                .sessionAttr(NIL_SUBMISSION_FORM, buildSessionForm()))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/nil-submission/summary-details"));
  }

  @Test
  void postSummary_redirectsToSubmissionDetails() throws Exception {
    UUID submissionId = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    when(nilSubmissionService.createSubmission(any(), any()))
        .thenReturn(new NilSubmissionResult(submissionId, List.of()));

    var session = sessionWithForm(buildSessionForm());

    mockMvc
        .perform(
            post("/nil-submission/summary-details")
                .with(csrf())
                .with(oidcLogin().oidcUser(OIDC_USER))
                .session(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/submissions/" + submissionId));

    var form = (NilSubmissionForm) session.getAttribute(NIL_SUBMISSION_FORM);
    assertNull(form.getOffice());
    assertNull(form.getAreaOfLaw());
    assertNull(form.getSubmissionPeriod());
    assertNull(form.getSubmissionReference());
  }

  @Test
  void postSummary_returnsInvalidViewWithMessagesSummary() throws Exception {
    when(nilSubmissionService.createSubmission(any(), any()))
        .thenReturn(
            new NilSubmissionResult(
                UUID.fromString("00000000-0000-0000-0000-0000000000a2"),
                List.of(
                    "Mediation submission reference must be a maximum of 20 characters and contain only letters, numbers and forward slashes")));

    var session = sessionWithForm(buildSessionForm());

    mockMvc
        .perform(
            post("/nil-submission/summary-details")
                .with(csrf())
                .with(oidcLogin().oidcUser(OIDC_USER))
                .session(session))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/nil-submission/summary-details"))
        .andExpect(model().attributeExists("errorMessages"));

    var form = (NilSubmissionForm) session.getAttribute(NIL_SUBMISSION_FORM);
    assertEquals("12345", form.getOffice());
    assertEquals(MEDIATION, form.getAreaOfLaw());
    assertEquals("OCT-2025", form.getSubmissionPeriod());
    assertEquals("REF-123", form.getSubmissionReference());
  }

  @Test
  void postSummary_withoutRequiredSessionState_returnsNotFound() throws Exception {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("12345");
    form.setAreaOfLaw(MEDIATION);
    form.setSubmissionPeriod("OCT-2025");

    mockMvc
        .perform(
            post("/nil-submission/summary-details")
                .with(csrf())
                .with(oidcLogin().oidcUser(OIDC_USER))
                .sessionAttr(NIL_SUBMISSION_FORM, form))
        .andExpect(status().isNotFound());

    verifyNoInteractions(nilSubmissionService);
  }

  private static NilSubmissionForm buildSessionForm() {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("12345");
    form.setAreaOfLaw(MEDIATION);
    form.setSubmissionPeriod("OCT-2025");
    form.setSubmissionReference("REF-123");
    return form;
  }

  private MockHttpSession sessionWithForm(NilSubmissionForm form) {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(NIL_SUBMISSION_FORM, form);
    return session;
  }
}
