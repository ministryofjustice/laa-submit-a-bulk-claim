package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.justice.laa.bulkclaim.service.SubmissionService;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;

@RequiredArgsConstructor
@ControllerAdvice(annotations = SubmissionControllerAdvice.Enabled.class)
@Slf4j
public class SubmissionControllerAdvice {

  private final SubmissionService submissionService;

  @ModelAttribute("submissionResponse")
  public SubmissionResponse getSubmission(
      @AuthenticationPrincipal OidcUser oidcUser,
      @PathVariable(name = SUBMISSION_ID, required = false) UUID submissionIdFromPath,
      @RequestParam(name = SUBMISSION_ID, required = false) UUID submissionIdFromQueryParam,
      @ModelAttribute(name = SUBMISSION_ID) UUID submissionIdFromModelAttribute) {
    var submissionId =
        Optional.ofNullable(submissionIdFromPath)
            .or(() -> Optional.ofNullable(submissionIdFromQueryParam))
            .or(() -> Optional.ofNullable(submissionIdFromModelAttribute))
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing submissionId"));

    return submissionService.getSubmission(submissionId, oidcUser);
  }

  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Enabled {}
}
