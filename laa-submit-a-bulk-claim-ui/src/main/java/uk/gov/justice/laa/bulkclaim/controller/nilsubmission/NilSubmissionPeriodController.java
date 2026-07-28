package uk.gov.justice.laa.bulkclaim.controller.nilsubmission;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.NIL_SUBMISSION_FORM;
import static uk.gov.justice.laa.bulkclaim.util.NilSubmissionSessionManager.cleanseSession;
import static uk.gov.justice.laa.bulkclaim.util.NilSubmissionSessionManager.validateSessionState;

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
import uk.gov.justice.laa.bulkclaim.service.SubmissionPeriodService;
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionPage;

@Controller
@RequiredArgsConstructor
@SessionAttributes(NIL_SUBMISSION_FORM)
public class NilSubmissionPeriodController {

  private final FeatureFlagsConfig featureFlagsConfig;
  private final SubmissionPeriodService submissionPeriodService;

  @GetMapping("/nil-submission/period")
  public String getSubmissionPeriods(
      @ModelAttribute(NIL_SUBMISSION_FORM) NilSubmissionForm selection, Model model) {

    featureFlagsConfig.checkNilSubmissionEnabled();
    validateSessionState(selection, NilSubmissionPage.SUBMISSION_PERIOD);
    cleanseSession(selection, NilSubmissionPage.SUBMISSION_PERIOD);

    var submissionsResults = submissionPeriodService.searchSubmissions(selection);
    var sortedSubmissionPeriods =
        submissionPeriodService.sortSubmissionPeriods(
            submissionPeriodService.getMonthsWithOutSubmissions(submissionsResults));

    if (sortedSubmissionPeriods.isEmpty()) {
      return "pages/nil-submission/no-submission-periods";
    }
    model.addAttribute("submissionPeriods", sortedSubmissionPeriods);
    return "pages/nil-submission/period";
  }

  @PostMapping("/nil-submission/period")
  public String postSubmissionPeriod(
      @ModelAttribute(NIL_SUBMISSION_FORM) NilSubmissionForm form,
      BindingResult bindingResult,
      Model model) {

    featureFlagsConfig.checkNilSubmissionEnabled();
    validateSessionState(form, NilSubmissionPage.SUBMISSION_PERIOD);

    var submissionsResults = submissionPeriodService.searchSubmissions(form);
    var sortedSubmissionPeriods =
        submissionPeriodService.sortSubmissionPeriods(
            submissionPeriodService.getMonthsWithOutSubmissions(submissionsResults));

    if (!StringUtils.hasText(form.getSubmissionPeriod())) {
      bindingResult.rejectValue("submissionPeriod", "nilSubmission.submissionPeriod.required");
    } else if (!sortedSubmissionPeriods.containsKey(form.getSubmissionPeriod())) {
      bindingResult.rejectValue("submissionPeriod", "nilSubmission.submissionPeriod.invalid");
    }

    if (bindingResult.hasErrors()) {
      form.setSubmissionPeriod(null);
      model.addAttribute("submissionPeriods", sortedSubmissionPeriods);
      return "pages/nil-submission/period";
    }

    return "redirect:/nil-submission/reference";
  }
}
