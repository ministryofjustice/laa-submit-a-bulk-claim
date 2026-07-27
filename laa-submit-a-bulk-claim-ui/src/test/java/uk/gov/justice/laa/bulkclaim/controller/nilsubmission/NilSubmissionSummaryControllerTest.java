package uk.gov.justice.laa.bulkclaim.controller.nilsubmission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.NIL_SUBMISSION_FORM;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.MEDIATION;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.controller.BaseControllerTest;
import uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionValidationErrorResponse;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.NilSubmissionMessagesSummary;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.CreateSubmission201Response;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionPost;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@WebMvcTest(NilSubmissionsSummaryController.class)
class NilSubmissionSummaryControllerTest extends BaseControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private DataClaimsRestClient claimsRestService;
  @MockitoBean private ObjectMapper objectMapper;

  @Test
  void whenFeatureFlagDisabled_allMappings_returnsErrorView() throws Exception {
    doThrow(new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND))
        .when(featureFlagsConfig)
        .checkNilSubmissionEnabled();

    mockMvc
        .perform(
            get("/nil-submission/summary-details")
                .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                .sessionAttr(NIL_SUBMISSION_FORM, buildSessionForm()))
        .andExpect(status().isNotFound());

    mockMvc
        .perform(
            post("/nil-submission/summary-details")
                .with(csrf())
                .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                .sessionAttr(NIL_SUBMISSION_FORM, buildSessionForm()))
        .andExpect(status().isNotFound());

    verifyNoInteractions(claimsRestService);
  }

  @Test
  void whenFeatureFlagEnabled_getSummary_returnsSummaryView() throws Exception {
    mockMvc
        .perform(
            get("/nil-submission/summary-details")
                .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                .sessionAttr(NIL_SUBMISSION_FORM, buildSessionForm()))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/nil-submission/summary-details"));
  }

  @Test
  void postSummary_redirectsToSubmissionDetails() throws Exception {
    UUID submissionId = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    CreateSubmission201Response submissionResponse =
        org.mockito.Mockito.mock(CreateSubmission201Response.class);
    when(submissionResponse.getId()).thenReturn(submissionId);
    when(claimsRestService.createSubmission(any()))
        .thenReturn(ResponseEntity.ok(submissionResponse));

    var session = sessionWithForm(buildSessionForm());

    mockMvc
        .perform(
            post("/nil-submission/summary-details")
                .with(csrf())
                .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                .session(session))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/submission/" + submissionId))
        .andExpect(flash().attribute(SUBMISSION_ID, submissionId));

    ArgumentCaptor<SubmissionPost> submissionPostCaptor =
        ArgumentCaptor.forClass(SubmissionPost.class);
    verify(claimsRestService).createSubmission(submissionPostCaptor.capture());
    SubmissionPost submissionPost = submissionPostCaptor.getValue();
    assertEquals("12345", submissionPost.getOfficeAccountNumber());
    assertEquals(0, submissionPost.getNumberOfClaims());
    assertEquals(SubmissionStatus.READY_FOR_VALIDATION, submissionPost.getStatus());
    assertEquals(MEDIATION, submissionPost.getAreaOfLaw());
    assertEquals("OCT-2025", submissionPost.getSubmissionPeriod());

    var form = (NilSubmissionForm) session.getAttribute(NIL_SUBMISSION_FORM);
    assertNull(form.getOffice());
    assertNull(form.getAreaOfLaw());
    assertNull(form.getSubmissionPeriod());
    assertNull(form.getSubmissionReference());
  }

  @Test
  void postSummary_returnsInvalidViewWithMessagesSummary() throws Exception {
    SubmissionValidationErrorResponse errorResponse =
        new SubmissionValidationErrorResponse(
            null,
            null,
            null,
            null,
            null,
            null,
            List.of(
                new SubmissionValidationErrorResponse.Issue(
                    null,
                    "Mediation submission reference must be a maximum of 20 characters and contain only letters, numbers and forward slashes",
                    null,
                    null,
                    null)));
    when(objectMapper.readValue(any(String.class), eq(SubmissionValidationErrorResponse.class)))
        .thenReturn(errorResponse);
    when(claimsRestService.createSubmission(any()))
        .thenThrow(new WebClientResponseException(400, "", null, null, null, null));

    var session = sessionWithForm(buildSessionForm());

    mockMvc
        .perform(
            post("/nil-submission/summary-details")
                .with(csrf())
                .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                .session(session))
        .andExpect(status().isOk())
        .andExpect(view().name("pages/nil-submission/detail-invalid"))
        .andExpect(model().attributeExists("messagesSummary"));

    var form = (NilSubmissionForm) session.getAttribute(NIL_SUBMISSION_FORM);
    assertNull(form.getOffice());
    assertNull(form.getAreaOfLaw());
    assertNull(form.getSubmissionPeriod());
    assertNull(form.getSubmissionReference());
  }

  @Test
  void buildNilSubmissionMessagesSummary_mapsFieldsCorrectly() {
    NilSubmissionForm form = buildSessionForm();
    NilSubmissionMessagesSummary summary =
        NilSubmissionsSummaryController.buildNilSubmissionMessagesSummary(
            form,
            List.of(
                "Submission already exists for Office (12345), Area of Law (MEDIATION), Period (OCT-2025)",
                "Mediation submission reference must be a maximum of 20 characters and contain only letters, numbers and forward slashes"));

    assertEquals(2, summary.totalMessageCount());
    assertEquals(MEDIATION, summary.areaOfLaw());
    assertEquals("12345", summary.officeAccount());
    assertEquals("OCT-2025", summary.submissionPeriod());
    assertEquals("REF-123", summary.submissionReference());
    assertEquals(
        List.of(
            "Submission already exists for Office (12345), Area of Law (MEDIATION), Period (OCT-2025)",
            "Mediation submission reference must be a maximum of 20 characters and contain only letters, numbers and forward slashes"),
        summary.messages());
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
