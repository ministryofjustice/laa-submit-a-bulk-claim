package uk.gov.justice.laa.bulkclaim.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.CRIME_LOWER;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.LEGAL_HELP;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.MEDIATION;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.CreateSubmission201Response;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionPost;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@ExtendWith(MockitoExtension.class)
class NilSubmissionServiceTest {

  @Mock private DataClaimsRestClient claimsRestService;
  @Mock private ObjectMapper objectMapper;
  @Mock private OidcUser oidcUser;

  @InjectMocks private NilSubmissionService nilSubmissionService;

  @Test
  void createSubmission_success_returnsSubmissionIdAndNoErrors() {
    when(oidcUser.getPreferredUsername()).thenReturn("user-123");
    when(claimsRestService.createSubmission(any()))
        .thenReturn(ResponseEntity.ok(org.mockito.Mockito.mock(CreateSubmission201Response.class)));

    var result = nilSubmissionService.createSubmission(buildForm(MEDIATION), oidcUser);

    assertNotNull(result.submissionId());
    assertTrue(result.errorMessages().isEmpty());

    var postCaptor = ArgumentCaptor.forClass(SubmissionPost.class);
    verify(claimsRestService).createSubmission(postCaptor.capture());
    SubmissionPost post = postCaptor.getValue();

    assertEquals("0P322F", post.getOfficeAccountNumber());
    assertEquals(0, post.getNumberOfClaims());
    assertEquals(SubmissionStatus.READY_FOR_VALIDATION, post.getStatus());
    assertEquals(MEDIATION, post.getAreaOfLaw());
    assertEquals("OCT-2025", post.getSubmissionPeriod());
    assertEquals("user-123", post.getProviderUserId());
    assertEquals("Submit-a-bulk-claim", post.getCreatedByUserId());
    assertFalse(Boolean.FALSE.equals(post.getIsNilSubmission()));
  }

  @Test
  void createSubmission_mapsMediationReference() {
    when(oidcUser.getPreferredUsername()).thenReturn("user-123");
    when(claimsRestService.createSubmission(any()))
        .thenReturn(ResponseEntity.ok(org.mockito.Mockito.mock(CreateSubmission201Response.class)));

    nilSubmissionService.createSubmission(buildForm(MEDIATION), oidcUser);

    var postCaptor = ArgumentCaptor.forClass(SubmissionPost.class);
    verify(claimsRestService).createSubmission(postCaptor.capture());
    SubmissionPost post = postCaptor.getValue();

    assertEquals("REF-123", post.getMediationSubmissionReference());
    assertNull(post.getLegalHelpSubmissionReference());
    assertNull(post.getCrimeLowerScheduleNumber());
  }

  @Test
  void createSubmission_mapsLegalHelpReference() {
    when(oidcUser.getPreferredUsername()).thenReturn("user-123");
    when(claimsRestService.createSubmission(any()))
        .thenReturn(ResponseEntity.ok(org.mockito.Mockito.mock(CreateSubmission201Response.class)));

    nilSubmissionService.createSubmission(buildForm(LEGAL_HELP), oidcUser);

    var postCaptor = ArgumentCaptor.forClass(SubmissionPost.class);
    verify(claimsRestService).createSubmission(postCaptor.capture());
    SubmissionPost post = postCaptor.getValue();

    assertEquals("REF-123", post.getLegalHelpSubmissionReference());
    assertNull(post.getMediationSubmissionReference());
    assertNull(post.getCrimeLowerScheduleNumber());
  }

  @Test
  void createSubmission_mapsCrimeLowerReference() {
    when(oidcUser.getPreferredUsername()).thenReturn("user-123");
    when(claimsRestService.createSubmission(any()))
        .thenReturn(ResponseEntity.ok(org.mockito.Mockito.mock(CreateSubmission201Response.class)));

    nilSubmissionService.createSubmission(buildForm(CRIME_LOWER), oidcUser);

    var postCaptor = ArgumentCaptor.forClass(SubmissionPost.class);
    verify(claimsRestService).createSubmission(postCaptor.capture());
    SubmissionPost post = postCaptor.getValue();

    assertEquals("REF-123", post.getCrimeLowerScheduleNumber());
    assertNull(post.getMediationSubmissionReference());
    assertNull(post.getLegalHelpSubmissionReference());
  }

  @Test
  void createSubmission_validationFailure_returnsValidationMessages() {
    when(oidcUser.getPreferredUsername()).thenReturn("user-123");
    when(claimsRestService.createSubmission(any()))
        .thenThrow(
            webClientResponseException(
                """
                {
                  "issues": [
                    {"message": "Error 1"},
                    {"message": " "},
                    {"message": "Error 2"}
                  ]
                }
                """));

    var result = nilSubmissionService.createSubmission(buildForm(MEDIATION), oidcUser);

    assertNotNull(result.submissionId());
    assertEquals(List.of("Error 1", "Error 2"), result.errorMessages());
  }

  @Test
  void createSubmission_validationFailureWithoutMessages_throwsException() {
    when(oidcUser.getPreferredUsername()).thenReturn("user-123");
    when(claimsRestService.createSubmission(any()))
        .thenThrow(webClientResponseException("{\"issues\":[]}"));

    assertThrows(
        IllegalStateException.class,
        () -> nilSubmissionService.createSubmission(buildForm(MEDIATION), oidcUser));
  }

  private NilSubmissionForm buildForm(
      uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw areaOfLaw) {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("0P322F");
    form.setAreaOfLaw(areaOfLaw);
    form.setSubmissionPeriod("OCT-2025");
    form.setSubmissionReference("REF-123");
    return form;
  }

  private static WebClientResponseException webClientResponseException(String body) {
    return new WebClientResponseException(
        400, "Bad Request", null, body.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
  }
}
