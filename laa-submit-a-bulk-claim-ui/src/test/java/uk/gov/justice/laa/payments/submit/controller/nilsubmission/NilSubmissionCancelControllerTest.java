package uk.gov.justice.laa.payments.submit.controller.nilsubmission;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.CRIME_LOWER;
import static uk.gov.justice.laa.payments.submit.constants.SessionConstants.NIL_SUBMISSION_FORM;
import static uk.gov.justice.laa.payments.submit.controller.ControllerTestHelper.OIDC_USER;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.payments.submit.controller.BaseControllerTest;
import uk.gov.justice.laa.payments.submit.dto.submission.NilSubmissionForm;

@WebMvcTest(NilSubmissionCancelController.class)
class NilSubmissionCancelControllerTest extends BaseControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void whenFeatureFlagDisabled_shouldReturnErrorView() throws Exception {
    doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND))
        .when(featureFlagsConfig)
        .checkNilSubmissionEnabled();

    mockMvc
        .perform(
            get("/nil-submission/cancel")
                .param("destination", "UPLOAD")
                .with(oidcLogin().oidcUser(OIDC_USER))
                .sessionAttr(NIL_SUBMISSION_FORM, new NilSubmissionForm()))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenFeatureFlagEnabledAndDestinationUpload_shouldRedirectToUpload() throws Exception {
    var session = sessionWithForm();

    mockMvc
        .perform(
            get("/nil-submission/cancel")
                .param("destination", "UPLOAD")
                .with(oidcLogin().oidcUser(OIDC_USER))
                .session(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/upload"));

    var form = (NilSubmissionForm) session.getAttribute(NIL_SUBMISSION_FORM);
    assertNull(form.getOffice());
    assertNull(form.getAreaOfLaw());
    assertNull(form.getSubmissionPeriod());
    assertNull(form.getSubmissionReference());
  }

  @Test
  void whenFeatureFlagEnabledAndDestinationSearch_shouldRedirectToSearchAndRetainSession()
      throws Exception {
    var session = sessionWithForm();

    mockMvc
        .perform(
            get("/nil-submission/cancel")
                .param("destination", "SEARCH")
                .with(oidcLogin().oidcUser(OIDC_USER))
                .session(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/submissions/search"));

    var form = (NilSubmissionForm) session.getAttribute(NIL_SUBMISSION_FORM);
    assertNotNull(form.getOffice());
    assertNotNull(form.getAreaOfLaw());
    assertNotNull(form.getSubmissionPeriod());
    assertNotNull(form.getSubmissionReference());
  }

  private MockHttpSession sessionWithForm() {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    form.setAreaOfLaw(CRIME_LOWER);
    form.setSubmissionPeriod("submissionPeriod1");
    form.setSubmissionReference("submissionReference1");

    MockHttpSession session = new MockHttpSession();
    session.setAttribute(NIL_SUBMISSION_FORM, form);
    return session;
  }
}
