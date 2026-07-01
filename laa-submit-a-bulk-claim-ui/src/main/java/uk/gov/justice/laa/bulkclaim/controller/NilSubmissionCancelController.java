package uk.gov.justice.laa.bulkclaim.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionPage;
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionSessionManager;

@Controller
@RequiredArgsConstructor
@SessionAttributes("nilSubmissionForm")
public class NilSubmissionCancelController {
  private final FeatureFlagsConfig featureFlagsConfig;

  @GetMapping("/nil-submission-cancel")
  public String getCancel(@ModelAttribute("nilSubmissionForm") NilSubmissionForm form) {

    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
      return "error";
    }

    NilSubmissionSessionManager.nilSubmissionCleanseSession(form, NilSubmissionPage.OTHER);
    return "redirect:/upload";
  }
}
