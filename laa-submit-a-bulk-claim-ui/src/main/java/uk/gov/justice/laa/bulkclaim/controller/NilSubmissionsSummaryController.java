package uk.gov.justice.laa.bulkclaim.controller;

import com.fasterxml.uuid.Generators;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
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
import uk.gov.justice.laa.dstew.payments.claimsdata.model.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.BULK_SUBMISSION_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;

@Controller
@RequiredArgsConstructor
@Slf4j
@SessionAttributes({"nilSubmissionForm", SUBMISSION_ID})
public class NilSubmissionsSummaryController {
  private final DataClaimsRestClient claimsRestService;
  private final FeatureFlagsConfig featureFlagsConfig;
  private final ObjectMapper objectMapper;

  @GetMapping("/nil-submission-summary-details")
  public String getSummary() {

    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
      return "error";
    }

    return "pages/nil-submission-summary-details";
  }

  @PostMapping("/nil-submission-summary-details")
  public String postSummary(
      @ModelAttribute("nilSubmissionForm") NilSubmissionForm form,
      RedirectAttributes redirectAttributes,
      Model model,
      @AuthenticationPrincipal OidcUser oidcUser) {

    SubmissionPost submissionPost =
        SubmissionPost.builder()
            .officeAccountNumber(form.getOffice())
            .numberOfClaims(0)
            .status(SubmissionStatus.READY_FOR_VALIDATION)
            .areaOfLaw(AreaOfLaw.valueOf(form.getAreaOfLaw()))
            .legalHelpSubmissionReference(form.getScheduleReference())
            .isNilSubmission(true)
            .submissionId(Generators.timeBasedEpochGenerator().generate())
            .submissionPeriod(form.getSubmissionPeriod())
            .providerUserId(oidcUser.getPreferredUsername())
            .createdByUserId("Submit-a-bulk-claim")
            .build();

      try {
          ResponseEntity<CreateSubmission201Response> responseEntity =  claimsRestService.createSubmission(submissionPost);
          CreateSubmission201Response submissionResponse = responseEntity.getBody();

          log.info(
                  "Claims API submission UUID: {}",
                  submissionResponse.getId());

          model.addAttribute(
                  SUBMISSION_ID, submissionResponse.getId());
          addSessionAttributesToModel(model, form);
          System.out.println("redirect:/submission/" + responseEntity.getBody().getId());
          redirectAttributes.addFlashAttribute(
                  SUBMISSION_ID, responseEntity.getBody().getId());
          return "redirect:/submission/" + responseEntity.getBody().getId();

      } catch (WebClientResponseException e) {
          try {
              SubmissionValidationErrorResponse error =
                      objectMapper.readValue(
                              e.getResponseBodyAsString(),
                              SubmissionValidationErrorResponse.class);

              List<String> errorMessages = error.getIssues().stream()
                      .map(SubmissionValidationErrorResponse.Issue::getMessage)
                      .filter(StringUtils::hasText)
                      .toList();

              log.error("API upload failed: {}", errorMessages.getFirst());

          } catch (Exception ex) {
              log.error("Failed to submit nil submission to Claims API with message: {}", ex.getMessage());

          }

          return "redirect:/view-nil-submission-detail-rejected";

      } catch (Exception e) {
          log.error("Failed to submit nil submission API failure: {}", e.getMessage());
          return "error";
      }
  }

  private void addSessionAttributesToModel(Model model, NilSubmissionForm form) {
      model.addAttribute("areaOfLaw", form.getAreaOfLaw());
  }
}
