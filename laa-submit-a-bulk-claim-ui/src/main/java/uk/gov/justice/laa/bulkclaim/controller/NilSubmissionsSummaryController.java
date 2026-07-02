package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.AreaOfLawConstants.CRIME_LOWER;
import static uk.gov.justice.laa.bulkclaim.constants.AreaOfLawConstants.LEGAL_HELP;
import static uk.gov.justice.laa.bulkclaim.constants.AreaOfLawConstants.MEDIATION;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.NIL_SUBMISSION_FORM;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tools.jackson.databind.ObjectMapper;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.dto.submission.SubmissionValidationErrorResponse;
import uk.gov.justice.laa.bulkclaim.dto.submission.messages.NilSubmissionMessagesSummary;
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionPage;
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionReferenceUtil;
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionSessionManager;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.CreateSubmission201Response;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionPost;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@Controller
@RequiredArgsConstructor
@Slf4j
@SessionAttributes({NIL_SUBMISSION_FORM, SUBMISSION_ID})
public class NilSubmissionsSummaryController {

  private final DataClaimsRestClient claimsRestService;
  private final FeatureFlagsConfig featureFlagsConfig;
  private final ObjectMapper objectMapper;
  private final NilSubmissionReferenceUtil nilSubmissionReferenceUtil;

  @GetMapping("/nil-submission-summary-details")
  public String getSummary(
      @ModelAttribute(NIL_SUBMISSION_FORM) NilSubmissionForm form, Model model) {

    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
      return "error";
    }

    String label =
        nilSubmissionReferenceUtil.getSubmissionReferenceByAreaOfLaw(
            form.getAreaOfLaw(), "reference");
    model.addAttribute("referenceLabel", label);

    return "pages/nil-submission-summary-details";
  }

  @PostMapping("/nil-submission-summary-details")
  public String postSummary(
      @ModelAttribute(NIL_SUBMISSION_FORM) NilSubmissionForm form,
      RedirectAttributes redirectAttributes,
      Model model,
      @AuthenticationPrincipal OidcUser oidcUser) {

    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
      return "error";
    }
    SubmissionPost submissionPost = buildSubmissionPost(form, oidcUser);

    try {
      ResponseEntity<CreateSubmission201Response> responseEntity =
          claimsRestService.createSubmission(submissionPost);
      CreateSubmission201Response submissionResponse = responseEntity.getBody();

      log.info("Claims API submission UUID: {}", submissionResponse.getId());

      model.addAttribute(SUBMISSION_ID, submissionResponse.getId());
      redirectAttributes.addFlashAttribute(SUBMISSION_ID, responseEntity.getBody().getId());

      NilSubmissionSessionManager.nilSubmissionCleanseSession(form, NilSubmissionPage.OTHER);
      return "redirect:/submission/" + responseEntity.getBody().getId();

    } catch (WebClientResponseException e) {
      try {
        SubmissionValidationErrorResponse error =
            objectMapper.readValue(
                e.getResponseBodyAsString(), SubmissionValidationErrorResponse.class);

        List<String> errorMessages =
            error.getIssues().stream()
                .map(SubmissionValidationErrorResponse.Issue::getMessage)
                .filter(StringUtils::hasText)
                .toList();

        log.error("API upload failed: {}", errorMessages.getFirst());

        NilSubmissionMessagesSummary summary =
            buildNilSubmissionMessagesSummary(form, errorMessages);

        String label =
            nilSubmissionReferenceUtil.getSubmissionReferenceByAreaOfLaw(
                form.getAreaOfLaw(), "reference");

        model.addAttribute("messagesSummary", summary);
        model.addAttribute("referenceLabel", label);

      } catch (Exception ex) {
        log.error(
            "Failed to submit nil submission to Claims API with message: {}", ex.getMessage());
      }

      NilSubmissionSessionManager.nilSubmissionCleanseSession(form, NilSubmissionPage.OTHER);
      return "pages/nil-submission-detail-invalid";

    } catch (Exception e) {
      log.error("Failed to submit nil submission API failure: {}", e.getMessage());
      return "error";
    }
  }

  static NilSubmissionMessagesSummary buildNilSubmissionMessagesSummary(
      NilSubmissionForm form, List<String> errorMessages) {
    return NilSubmissionMessagesSummary.builder()
        .totalMessageCount(errorMessages.size())
        .submitted(OffsetDateTime.now())
        .officeAccount(form.getOffice())
        .areaOfLaw(form.getAreaOfLaw())
        .submissionPeriod(form.getSubmissionPeriod())
        .submissionReference(form.getScheduleReference())
        .messages(errorMessages)
        .build();
  }

  SubmissionPost buildSubmissionPost(NilSubmissionForm form, OidcUser oidcUser) {
    SubmissionPost submissionPost =
        SubmissionPost.builder()
            .officeAccountNumber(form.getOffice())
            .numberOfClaims(0)
            .status(SubmissionStatus.READY_FOR_VALIDATION)
            .areaOfLaw(AreaOfLaw.valueOf(form.getAreaOfLaw()))
            .isNilSubmission(true)
            .submissionId(UUID.randomUUID())
            .submissionPeriod(form.getSubmissionPeriod())
            .providerUserId(oidcUser.getPreferredUsername())
            .createdByUserId("Submit-a-bulk-claim")
            .build();

    setSubmissionReferenceByAreaOfLaw(form, submissionPost);
    return submissionPost;
  }

  void setSubmissionReferenceByAreaOfLaw(NilSubmissionForm form, SubmissionPost submissionPost) {
    switch (form.getAreaOfLaw()) {
      case LEGAL_HELP ->
          submissionPost.setLegalHelpSubmissionReference(form.getScheduleReference());
      case MEDIATION -> submissionPost.setMediationSubmissionReference(form.getScheduleReference());
      case CRIME_LOWER -> submissionPost.setCrimeLowerScheduleNumber(form.getScheduleReference());
      default -> log.error("Area of law {} is not valid", form.getAreaOfLaw());
    }
  }
}
