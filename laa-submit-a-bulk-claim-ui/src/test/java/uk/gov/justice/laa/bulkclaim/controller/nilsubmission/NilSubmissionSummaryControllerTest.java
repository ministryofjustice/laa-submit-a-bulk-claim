package uk.gov.justice.laa.bulkclaim.controller.nilsubmission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.MEDIATION;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tools.jackson.databind.ObjectMapper;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionValidationErrorResponse;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.NilSubmissionMessagesSummary;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.CreateSubmission201Response;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionPost;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

class NilSubmissionSummaryControllerTest {

  @Mock private DataClaimsRestClient claimsRestService;
  @Mock private FeatureFlagsConfig featureFlagsConfig;
  @Mock private ObjectMapper objectMapper;
  @Mock private Model model;
  @Mock private RedirectAttributes redirectAttributes;

  @InjectMocks private NilSubmissionsSummaryController controller;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void whenFeatureFlagDisabled_all_mappings_returnsErrorView() {
    doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "isNilSubmissionEnabled is false"))
        .when(featureFlagsConfig)
        .checkNilSubmissionEnabled();

    NilSubmissionForm form = buildSessionForm();

    assertThrows(ResponseStatusException.class, () -> controller.getSummary(form, model));

    assertThrows(
        ResponseStatusException.class,
        () ->
            controller.postSummary(
                form, redirectAttributes, model, ControllerTestHelper.getOidcUser()));

    verifyNoInteractions(claimsRestService);
  }

  @Test
  void whenFeatureFlagEnabled_getSummary_returnsSummaryView() {

    assertEquals(
        "pages/nil-submission/summary-details", controller.getSummary(buildSessionForm(), model));
  }

  @Test
  void postSummary_redirectsToSubmissionDetails() {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);

    UUID submissionId = UUID.fromString("00000000-0000-0000-0000-0000000000a1");
    CreateSubmission201Response submissionResponse = mock(CreateSubmission201Response.class);
    when(submissionResponse.getId()).thenReturn(submissionId);
    when(claimsRestService.createSubmission(any()))
        .thenReturn(ResponseEntity.ok(submissionResponse));

    NilSubmissionForm form = buildSessionForm();

    assertEquals(
        "redirect:/submission/" + submissionId,
        controller.postSummary(
            form, redirectAttributes, model, ControllerTestHelper.getOidcUser()));

    ArgumentCaptor<SubmissionPost> submissionPostCaptor =
        ArgumentCaptor.forClass(SubmissionPost.class);
    verify(claimsRestService).createSubmission(submissionPostCaptor.capture());
    SubmissionPost submissionPost = submissionPostCaptor.getValue();
    assertEquals("12345", submissionPost.getOfficeAccountNumber());
    assertEquals(0, submissionPost.getNumberOfClaims());
    assertEquals(SubmissionStatus.READY_FOR_VALIDATION, submissionPost.getStatus());
    assertEquals(MEDIATION, submissionPost.getAreaOfLaw());
    assertEquals("OCT-2025", submissionPost.getSubmissionPeriod());

    verify(model).addAttribute(eq(SUBMISSION_ID), eq(submissionId));
    verify(redirectAttributes).addFlashAttribute(eq(SUBMISSION_ID), eq(submissionId));

    assertNull(form.getOffice());
    assertNull(form.getAreaOfLaw());
    assertNull(form.getSubmissionPeriod());
    assertNull(form.getScheduleReference());
  }

  @Test
  void postSummary_returnsInvalidViewWithMessagesSummary() throws Exception {
    when(featureFlagsConfig.getIsNilSubmissionEnabled()).thenReturn(true);

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

    NilSubmissionForm form = buildSessionForm();

    assertEquals(
        "pages/nil-submission/detail-invalid",
        controller.postSummary(
            form, redirectAttributes, model, ControllerTestHelper.getOidcUser()));

    verify(model).addAttribute(eq("messagesSummary"), any(NilSubmissionMessagesSummary.class));

    assertNull(form.getOffice());
    assertNull(form.getAreaOfLaw());
    assertNull(form.getSubmissionPeriod());
    assertNull(form.getScheduleReference());
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
    form.setScheduleReference("REF-123");
    return form;
  }
}
