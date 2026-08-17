package uk.gov.justice.laa.bulkclaim.controller.nilsubmission;

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
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.MEDIATION;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.bulkclaim.controller.BaseControllerTest;
import uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;

@WebMvcTest(NilSubmissionOfficeController.class)
class NilSubmissionOfficeControllerTest extends BaseControllerTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void whenFeatureFlagDisabled_allMappings_returnsErrorView() throws Exception {
    doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND))
        .when(featureFlagsConfig)
        .checkNilSubmissionEnabled();

    mockMvc
        .perform(
            get("/nil-submission/office")
                .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                .sessionAttr(NIL_SUBMISSION_FORM, new NilSubmissionForm()))
        .andExpect(status().isNotFound());

    mockMvc
        .perform(
            post("/nil-submission/office")
                .with(csrf())
                .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                .sessionAttr(NIL_SUBMISSION_FORM, new NilSubmissionForm()))
        .andExpect(status().isNotFound());
  }

  @Test
  void whenFeatureFlagEnabled_getOffice_addsAreasAndReturnsView() throws Exception {
    List<String> offices = List.of("officeA", "officeB");
    when(oidcAttributeUtils.getUserOffices(any(OidcUser.class))).thenReturn(offices);

    mockMvc
        .perform(
            get("/nil-submission/office")
                .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser())))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/nil-submission/office"))
        .andExpect(model().attribute("userOffices", offices));
  }

  @Test
  void whenFeatureFlagEnabled_getOffice_noOffices_returnInfoMessage() throws Exception {
    when(oidcAttributeUtils.getUserOffices(any(OidcUser.class))).thenReturn(List.of());

    mockMvc
        .perform(
            get("/nil-submission/office")
                .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser())))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/nil-submission/info-message"))
        .andExpect(model().attributeDoesNotExist("userOffices"));
  }

  @Test
  void postOffice_setsFormAndRedirects() throws Exception {
    when(oidcAttributeUtils.getUserOffices(any(OidcUser.class)))
        .thenReturn(List.of("OfficeA", "OfficeB"));
    var session = sessionWithForm(new NilSubmissionForm());

    mockMvc
        .perform(
            post("/nil-submission/office")
                .with(csrf())
                .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                .param("office", "OfficeA")
                .session(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/nil-submission/areaoflaw"));

    var form = (NilSubmissionForm) session.getAttribute(NIL_SUBMISSION_FORM);
    org.junit.jupiter.api.Assertions.assertEquals("OfficeA", form.getOffice());
  }

  @Test
  void postOffice_whenOfficeNotSelected_returnsPageAndAddsRequiredError() throws Exception {
    when(oidcAttributeUtils.getUserOffices(any(OidcUser.class)))
        .thenReturn(List.of("OfficeA", "OfficeB"));
    var session = sessionWithForm(new NilSubmissionForm());

    mockMvc
        .perform(
            post("/nil-submission/office")
                .with(csrf())
                .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                .session(session))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/nil-submission/office"))
        .andExpect(model().attribute("userOffices", List.of("OfficeA", "OfficeB")))
        .andExpect(
            model()
                .attributeHasFieldErrorCode(
                    "nilSubmissionForm", "office", "nilSubmission.office.required"));

    var form = (NilSubmissionForm) session.getAttribute(NIL_SUBMISSION_FORM);
    assertNull(form.getOffice());
  }

  @Test
  void postOffice_whenOfficeNotAuthorized_returnsPageAndAddsInvalidError() throws Exception {
    when(oidcAttributeUtils.getUserOffices(any(OidcUser.class)))
        .thenReturn(List.of("OfficeA", "OfficeB"));
    var session = sessionWithForm(new NilSubmissionForm());

    mockMvc
        .perform(
            post("/nil-submission/office")
                .with(csrf())
                .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                .param("office", "UnauthorizedOffice")
                .session(session))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/nil-submission/office"))
        .andExpect(model().attribute("userOffices", List.of("OfficeA", "OfficeB")))
        .andExpect(
            model()
                .attributeHasFieldErrorCode(
                    "nilSubmissionForm", "office", "nilSubmission.office.invalid"));

    var form = (NilSubmissionForm) session.getAttribute(NIL_SUBMISSION_FORM);
    assertNull(form.getOffice());
  }

  @Test
  void getOffice_sessionManagementCleansing() throws Exception {
    when(oidcAttributeUtils.getUserOffices(any(OidcUser.class)))
        .thenReturn(List.of("office1", "office2"));

    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    form.setAreaOfLaw(MEDIATION);
    form.setSubmissionPeriod("submissionPeriod1");
    form.setSubmissionReference("submissionReference1");
    var session = sessionWithForm(form);

    mockMvc
        .perform(
            get("/nil-submission/office")
                .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                .session(session))
        .andExpect(status().isOk());

    var updatedForm = (NilSubmissionForm) session.getAttribute(NIL_SUBMISSION_FORM);
    assertNull(updatedForm.getOffice());
    assertNull(updatedForm.getAreaOfLaw());
    assertNull(updatedForm.getSubmissionPeriod());
    assertNull(updatedForm.getSubmissionReference());
  }

  private MockHttpSession sessionWithForm(NilSubmissionForm form) {
    MockHttpSession session = new MockHttpSession();
    session.setAttribute(NIL_SUBMISSION_FORM, form);
    return session;
  }
}
