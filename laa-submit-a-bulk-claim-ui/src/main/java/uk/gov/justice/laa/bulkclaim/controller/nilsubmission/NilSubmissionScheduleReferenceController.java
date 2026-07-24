package uk.gov.justice.laa.bulkclaim.controller.nilsubmission;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.NIL_SUBMISSION_FORM;
import static uk.gov.justice.laa.bulkclaim.util.NilSubmissionSessionManager.cleanseSession;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionPage;

@Controller
@RequiredArgsConstructor
@SessionAttributes(NIL_SUBMISSION_FORM)
public class NilSubmissionScheduleReferenceController {

  private final FeatureFlagsConfig featureFlagsConfig;

  @GetMapping("/nil-submission/reference")
  public String getReference(
      @ModelAttribute(NIL_SUBMISSION_FORM) NilSubmissionForm form, Model model) {

    featureFlagsConfig.checkNilSubmissionEnabled();
    cleanseSession(form, NilSubmissionPage.SCHEDULE_REFERENCE);

    model.addAttribute("displayReference", form.getScheduleReference());
    return "pages/nil-submission/reference";
  }

  @PostMapping("/nil-submission/reference")
  public String postReference(
      @ModelAttribute(NIL_SUBMISSION_FORM) NilSubmissionForm form,
      @RequestParam String scheduleReference) {
    featureFlagsConfig.checkNilSubmissionEnabled();

    form.setScheduleReference(scheduleReference);
    return "redirect:/nil-submission/summary-details";
  }
}
