package uk.gov.justice.laa.bulkclaim.controller.nilsubmission;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.NIL_SUBMISSION_FORM;
import static uk.gov.justice.laa.bulkclaim.util.NilSubmissionSessionManager.cleanseSession;
import static uk.gov.justice.laa.bulkclaim.util.NilSubmissionSessionManager.validateSessionState;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.service.NilSubmissionService;
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionPage;

@Controller
@RequiredArgsConstructor
@Slf4j
@SessionAttributes({NIL_SUBMISSION_FORM})
public class NilSubmissionsSummaryController {

  private final NilSubmissionService nilSubmissionService;
  private final FeatureFlagsConfig featureFlagsConfig;

  @GetMapping("/nil-submission/summary-details")
  public String getSummary(@ModelAttribute(NIL_SUBMISSION_FORM) NilSubmissionForm form) {

    featureFlagsConfig.checkNilSubmissionEnabled();
    validateSessionState(form, NilSubmissionPage.SUMMARY_DETAILS);
    cleanseSession(form, NilSubmissionPage.SUMMARY_DETAILS);

    return "pages/nil-submission/summary-details";
  }

  @PostMapping("/nil-submission/summary-details")
  public String postSummary(
      @ModelAttribute(NIL_SUBMISSION_FORM) NilSubmissionForm form,
      RedirectAttributes redirectAttributes,
      Model model,
      @AuthenticationPrincipal OidcUser oidcUser) {

    featureFlagsConfig.checkNilSubmissionEnabled();
    validateSessionState(form, NilSubmissionPage.SUMMARY_DETAILS);
    cleanseSession(form, NilSubmissionPage.SUMMARY_DETAILS);

    var result = nilSubmissionService.createSubmission(form, oidcUser);

    if (!result.errorMessages().isEmpty()) {
      log.error("Failed to create nil submission: {}", result);
      model.addAttribute("errorMessages", result.errorMessages());
      return "pages/nil-submission/summary-details";
    }

    cleanseSession(form, NilSubmissionPage.OTHER);
    return "redirect:/submissions/" + result.submissionId();
  }
}
