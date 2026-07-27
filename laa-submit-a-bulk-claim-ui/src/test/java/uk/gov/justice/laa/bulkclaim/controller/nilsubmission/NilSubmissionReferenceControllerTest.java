package uk.gov.justice.laa.bulkclaim.controller.nilsubmission;

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
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.NIL_SUBMISSION_FORM;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.MEDIATION;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.bulkclaim.controller.BaseControllerTest;
import uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;

@WebMvcTest(NilSubmissionReferenceController.class)
class NilSubmissionReferenceControllerTest extends BaseControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void whenFeatureFlagDisabled_allMappings_returnsErrorView() throws Exception {
    doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND))
        .when(featureFlagsConfig)
        .checkNilSubmissionEnabled();

    mockMvc
        .perform(
            get("/nil-submission/reference")
                .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                .sessionAttr(NIL_SUBMISSION_FORM, buildSessionForm()))
        .andExpect(status().isNotFound());

    mockMvc
        .perform(
            post("/nil-submission/reference")
                .with(csrf())
                .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                .sessionAttr(NIL_SUBMISSION_FORM, buildSessionForm()))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenFeatureFlagEnabled_getReference_returnsView() throws Exception {
    mockMvc
        .perform(
            get("/nil-submission/reference")
                .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                .sessionAttr(NIL_SUBMISSION_FORM, buildSessionForm()))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/nil-submission/reference"));
  }

  @Test
  void postReference_setsFormAndRedirects() throws Exception {
    var session = sessionWithForm(buildSessionForm());

    mockMvc
        .perform(
            post("/nil-submission/reference")
                .with(csrf())
                .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                .param("submissionReference", "reference")
                .session(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/nil-submission/summary-details"));

    var form = (NilSubmissionForm) session.getAttribute(NIL_SUBMISSION_FORM);
    org.junit.jupiter.api.Assertions.assertEquals("reference", form.getSubmissionReference());
  }

  @Test
  void postReference_whenNotEntered_returnsError() throws Exception {
    mockMvc
        .perform(
            post("/nil-submission/reference")
                .with(csrf())
                .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                .param("submissionReference", "")
                .sessionAttr(NIL_SUBMISSION_FORM, buildSessionForm()))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/nil-submission/reference"))
        .andExpect(
            model()
                .attributeHasFieldErrorCode(
                    "nilSubmissionForm",
                    "submissionReference",
                    "nilSubmission.submissionReference.required"));
  }

  @Test
  void postReference_whenInvalidFormat_returnsError() throws Exception {
    mockMvc
        .perform(
            post("/nil-submission/reference")
                .with(csrf())
                .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                .param("submissionReference", "invalid-reference-with-hyphen")
                .sessionAttr(NIL_SUBMISSION_FORM, buildSessionForm()))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/nil-submission/reference"))
        .andExpect(
            model()
                .attributeHasFieldErrorCode(
                    "nilSubmissionForm",
                    "submissionReference",
                    "nilSubmission.submissionReference.invalid"));
  }

  @Test
  void getSubmissionReference_sessionManagementCleansing() throws Exception {
    NilSubmissionForm form = buildSessionForm();
    form.setSubmissionReference("submissionReference1");
    var session = sessionWithForm(form);

    mockMvc
        .perform(
            get("/nil-submission/reference")
                .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                .session(session))
        .andExpect(status().isOk());

    var updatedForm = (NilSubmissionForm) session.getAttribute(NIL_SUBMISSION_FORM);
    assertNotNull(updatedForm.getOffice());
    assertNotNull(updatedForm.getAreaOfLaw());
    assertNotNull(updatedForm.getSubmissionPeriod());
    assertNull(updatedForm.getSubmissionReference());
  }

  private static NilSubmissionForm buildSessionForm() {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    form.setAreaOfLaw(MEDIATION);
    form.setSubmissionPeriod("OCT-2025");
    return form;
  }

  private MockHttpSession sessionWithForm(NilSubmissionForm form) {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(NIL_SUBMISSION_FORM, form);
    return session;
  }
}
