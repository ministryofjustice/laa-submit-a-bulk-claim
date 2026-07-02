package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.NIL_SUBMISSION_FORM;

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
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionReferenceUtil;

@Controller
@RequiredArgsConstructor
@SessionAttributes(NIL_SUBMISSION_FORM)
public class NilSubmissionScheduleReferenceController {

  private final FeatureFlagsConfig featureFlagsConfig;
  private final NilSubmissionReferenceUtil nilSubmissionReferenceUtil;

  @GetMapping("/nil-submission-reference")
  public String getReference(
      @ModelAttribute(NIL_SUBMISSION_FORM) NilSubmissionForm form, Model model) {

    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
      return "error";
    }

    String label =
        nilSubmissionReferenceUtil.getSubmissionReferenceByAreaOfLaw(
            form.getAreaOfLaw(), "reference");
    String exampleText =
        nilSubmissionReferenceUtil.getSubmissionReferenceByAreaOfLaw(
            form.getAreaOfLaw(), "example");

    model.addAttribute("referenceLabel", label);
    model.addAttribute("exampleText", exampleText);

    return "pages/nil-submission-reference";
  }

  @PostMapping("/nil-submission-reference")
  public String postReference(
      @ModelAttribute(NIL_SUBMISSION_FORM) NilSubmissionForm form,
      @RequestParam String scheduleReference) {
    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
      return "error";
    }
    form.setScheduleReference(scheduleReference);
    form.setScheduleReference(scheduleReference);
    return "redirect:/nil-submission-summary-details";
  }
}
