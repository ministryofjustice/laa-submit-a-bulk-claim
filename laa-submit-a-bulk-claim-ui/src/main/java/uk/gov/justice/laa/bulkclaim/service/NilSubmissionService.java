package uk.gov.justice.laa.bulkclaim.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tools.jackson.databind.ObjectMapper;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.NilSubmissionResult;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionValidationErrorResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionPost;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@Slf4j
@RequiredArgsConstructor
@Service
public class NilSubmissionService {

  private final DataClaimsRestClient claimsRestService;
  private final ObjectMapper objectMapper;

  public NilSubmissionResult createSubmission(NilSubmissionForm form, OidcUser oidcUser) {
    var submissionPost = buildSubmissionPost(form, oidcUser);
    log.info("Creating nil submission with submissionId {}", submissionPost.getSubmissionId());

    try {
      claimsRestService.createSubmission(submissionPost);
      return new NilSubmissionResult(submissionPost.getSubmissionId(), List.of());

    } catch (WebClientResponseException ex) {
      return new NilSubmissionResult(submissionPost.getSubmissionId(), parseErrorMessages(ex));
    }
  }

  private static List<String> parseErrorMessages(WebClientResponseException ex) {
    var body = ex.getResponseBodyAsString();
    var mapper = new ObjectMapper();
    var error = mapper.readValue(body, SubmissionValidationErrorResponse.class);

    List<String> messages = List.of();
    if (error.issues() != null) {
      messages =
          error.issues().stream()
              .map(SubmissionValidationErrorResponse.Issue::message)
              .filter(StringUtils::hasText)
              .toList();
    }

    if (messages.isEmpty()) {
      throw new IllegalStateException("No error messages found in response", ex);
    }

    return messages;
  }

  private static SubmissionPost buildSubmissionPost(NilSubmissionForm form, OidcUser oidcUser) {
    var submissionPost =
        SubmissionPost.builder()
            .officeAccountNumber(form.getOffice())
            .numberOfClaims(0)
            .status(SubmissionStatus.READY_FOR_VALIDATION)
            .areaOfLaw(form.getAreaOfLaw())
            .isNilSubmission(true)
            .submissionId(UUID.randomUUID())
            .submissionPeriod(form.getSubmissionPeriod())
            .providerUserId(oidcUser.getPreferredUsername())
            .createdByUserId("Submit-a-bulk-claim")
            .build();

    return setSubmissionReferenceByAreaOfLaw(form, submissionPost);
  }

  private static SubmissionPost setSubmissionReferenceByAreaOfLaw(
      NilSubmissionForm form, SubmissionPost submissionPost) {
    return switch (form.getAreaOfLaw()) {
      case LEGAL_HELP -> submissionPost.legalHelpSubmissionReference(form.getSubmissionReference());
      case MEDIATION -> submissionPost.mediationSubmissionReference(form.getSubmissionReference());
      case CRIME_LOWER -> submissionPost.crimeLowerScheduleNumber(form.getSubmissionReference());
    };
  }
}
