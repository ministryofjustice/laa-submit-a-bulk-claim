package uk.gov.justice.laa.payments.submit.controller.nilsubmission;

import static uk.gov.justice.laa.payments.submit.constants.SessionConstants.NIL_SUBMISSION_FORM;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.justice.laa.payments.submit.config.FeatureFlagsConfig;
import uk.gov.justice.laa.payments.submit.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.payments.submit.util.NilSubmissionPage;
import uk.gov.justice.laa.payments.submit.util.NilSubmissionSessionManager;

@Controller
@RequiredArgsConstructor
@SessionAttributes(NIL_SUBMISSION_FORM)
public class NilSubmissionCancelController {
  private final FeatureFlagsConfig featureFlagsConfig;

  @GetMapping("/nil-submission/cancel")
  public String getCancel(
      @RequestParam(defaultValue = "UPLOAD") String destination,
      @ModelAttribute(NIL_SUBMISSION_FORM) NilSubmissionForm form) {

    featureFlagsConfig.checkNilSubmissionEnabled();
    NilSubmissionSessionManager.validateSessionState(form, NilSubmissionPage.OTHER);

    if ("SEARCH".equalsIgnoreCase(destination)) {
      return "redirect:/submissions/search";
    }

    NilSubmissionSessionManager.cleanseSession(form, NilSubmissionPage.OTHER);
    return "redirect:/upload";
  }
}
