package uk.gov.justice.laa.payments.submit.controller.nilsubmission;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.CRIME_LOWER;
import static uk.gov.justice.laa.payments.submit.constants.SessionConstants.NIL_SUBMISSION_FORM;
import static uk.gov.justice.laa.payments.submit.controller.ControllerTestHelper.OIDC_USER;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.payments.submit.controller.BaseControllerTest;
import uk.gov.justice.laa.payments.submit.dto.submission.NilSubmissionForm;

@WebMvcTest(NilSubmissionAreaOfLawController.class)
class NilSubmissionAreaOfLawControllerTest extends BaseControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void whenFeatureFlagDisabled_allMappings_returnsErrorView() throws Exception {
    doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND))
        .when(featureFlagsConfig)
        .checkNilSubmissionEnabled();

    mockMvc
        .perform(
            get("/nil-submission/areaoflaw")
                .with(oidcLogin().oidcUser(OIDC_USER))
                .sessionAttr(NIL_SUBMISSION_FORM, new NilSubmissionForm()))
        .andExpect(status().isNotFound());

    mockMvc
        .perform(
            post("/nil-submission/areaoflaw")
                .with(csrf())
                .with(oidcLogin().oidcUser(OIDC_USER))
                .sessionAttr(NIL_SUBMISSION_FORM, new NilSubmissionForm()))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenFeatureFlagEnabled_getAreasOfLaw_addsAreasAndReturnsView() throws Exception {
    mockMvc
        .perform(
            get("/nil-submission/areaoflaw")
                .with(oidcLogin().oidcUser(OIDC_USER))
                .sessionAttr(NIL_SUBMISSION_FORM, formWithOffice()))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/nil-submission/areaoflaw"))
        .andExpect(model().attributeExists("areasOfLaw"));
  }

  @Test
  void postAreaOfLaw_setsFormAndRedirects() throws Exception {
    var session = sessionWithForm(formWithOffice());

    mockMvc
        .perform(
            post("/nil-submission/areaoflaw")
                .with(csrf())
                .with(oidcLogin().oidcUser(OIDC_USER))
                .param("areaOfLaw", "CRIME_LOWER")
                .session(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/nil-submission/period"));

    var form = (NilSubmissionForm) session.getAttribute(NIL_SUBMISSION_FORM);
    org.junit.jupiter.api.Assertions.assertEquals(CRIME_LOWER, form.getAreaOfLaw());
  }

  @Test
  void postAreaOfLaw_whenBindingFails_returnsPageAndClearsSelection() throws Exception {
    var session = sessionWithForm(formWithSelectedAreaOfLaw());

    mockMvc
        .perform(
            post("/nil-submission/areaoflaw")
                .with(csrf())
                .with(oidcLogin().oidcUser(OIDC_USER))
                .param("areaOfLaw", "potato")
                .session(session))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/nil-submission/areaoflaw"))
        .andExpect(model().attributeExists("areasOfLaw"))
        .andExpect(model().attributeHasFieldErrors("nilSubmissionForm", "areaOfLaw"));

    var form = (NilSubmissionForm) session.getAttribute(NIL_SUBMISSION_FORM);
    assertNull(form.getAreaOfLaw());
  }

  @Test
  void postAreaOfLaw_whenAreaOfLawNotSelected_returnsPageAndAddsRequiredError() throws Exception {
    var session = sessionWithForm(formWithOffice());

    mockMvc
        .perform(
            post("/nil-submission/areaoflaw")
                .with(csrf())
                .with(oidcLogin().oidcUser(OIDC_USER))
                .session(session))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/nil-submission/areaoflaw"))
        .andExpect(model().attributeExists("areasOfLaw"))
        .andExpect(
            model()
                .attributeHasFieldErrorCode(
                    "nilSubmissionForm", "areaOfLaw", "nilSubmission.areaOfLaw.required"));

    var form = (NilSubmissionForm) session.getAttribute(NIL_SUBMISSION_FORM);
    assertNull(form.getAreaOfLaw());
  }

  @Test
  void postAreaOfLaw_withoutRequiredSessionState_returnsNotFound() throws Exception {
    mockMvc
        .perform(
            post("/nil-submission/areaoflaw")
                .with(csrf())
                .with(oidcLogin().oidcUser(OIDC_USER))
                .param("areaOfLaw", "CRIME_LOWER")
                .sessionAttr(NIL_SUBMISSION_FORM, new NilSubmissionForm()))
        .andExpect(status().isNotFound());
  }

  @Test
  void getAreaOfLaw_sessionManagementCleansing() throws Exception {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    form.setAreaOfLaw(AreaOfLaw.CRIME_LOWER);
    form.setSubmissionPeriod("submissionPeriod1");
    form.setSubmissionReference("submissionReference1");
    var session = sessionWithForm(form);

    mockMvc
        .perform(
            get("/nil-submission/areaoflaw").with(oidcLogin().oidcUser(OIDC_USER)).session(session))
        .andExpect(status().isOk());

    var updatedForm = (NilSubmissionForm) session.getAttribute(NIL_SUBMISSION_FORM);
    assertNotNull(updatedForm.getOffice());
    assertNull(updatedForm.getAreaOfLaw());
    assertNull(updatedForm.getSubmissionPeriod());
    assertNull(updatedForm.getSubmissionReference());
  }

  private NilSubmissionForm formWithOffice() {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    return form;
  }

  private NilSubmissionForm formWithSelectedAreaOfLaw() {
    NilSubmissionForm form = formWithOffice();
    form.setAreaOfLaw(CRIME_LOWER);
    return form;
  }

  private MockHttpSession sessionWithForm(NilSubmissionForm form) {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(NIL_SUBMISSION_FORM, form);
    return session;
  }
}
