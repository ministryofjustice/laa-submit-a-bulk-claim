package uk.gov.justice.laa.bulkclaim.controller.nilsubmission;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.NIL_SUBMISSION_FORM;
import static uk.gov.justice.laa.bulkclaim.util.NilSubmissionSessionManager.cleanseSession;

import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionPage;

@Controller
@RequiredArgsConstructor
@SessionAttributes(NIL_SUBMISSION_FORM)
public class NilSubmissionReferenceController {

  private static final int MAX_REFERENCE_LENGTH = 20;
  private static final Pattern REFERENCE_PATTERN = Pattern.compile("^[a-zA-Z0-9/]+$");

  private final FeatureFlagsConfig featureFlagsConfig;

  @GetMapping("/nil-submission/reference")
  public String getReference(
      @ModelAttribute(NIL_SUBMISSION_FORM) NilSubmissionForm form, Model model) {

    featureFlagsConfig.checkNilSubmissionEnabled();
    cleanseSession(form, NilSubmissionPage.SUBMISSION_REFERENCE);

    return "pages/nil-submission/reference";
  }

  @PostMapping("/nil-submission/reference")
  public String postReference(
      @ModelAttribute(NIL_SUBMISSION_FORM) NilSubmissionForm form, BindingResult bindingResult) {
    featureFlagsConfig.checkNilSubmissionEnabled();

    if (!StringUtils.hasText(form.getSubmissionReference())) {
      bindingResult.rejectValue(
          "submissionReference", "nilSubmission.submissionReference.required");
    } else if (!isValidSubmissionReference(form.getSubmissionReference())) {
      bindingResult.rejectValue("submissionReference", "nilSubmission.submissionReference.invalid");
    }

    if (bindingResult.hasErrors()) {
      return "pages/nil-submission/reference";
    }

    return "redirect:/nil-submission/summary-details";
  }

  private boolean isValidSubmissionReference(String submissionReference) {
    return submissionReference.length() <= MAX_REFERENCE_LENGTH
        && REFERENCE_PATTERN.matcher(submissionReference).matches();
  }
}
