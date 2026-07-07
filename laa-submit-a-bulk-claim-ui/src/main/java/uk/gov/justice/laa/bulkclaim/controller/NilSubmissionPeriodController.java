package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.NIL_SUBMISSION_FORM;

import java.util.Map;
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
import uk.gov.justice.laa.bulkclaim.service.SubmissionPeriodService;
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionPage;
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionSessionManager;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

@Controller
@RequiredArgsConstructor
@SessionAttributes(NIL_SUBMISSION_FORM)
public class NilSubmissionPeriodController {

  private final FeatureFlagsConfig featureFlagsConfig;
  private final SubmissionPeriodService submissionPeriodService;

  @GetMapping("/nil-submission-period")
  public String getSubmissionPeriods(
      @ModelAttribute(NIL_SUBMISSION_FORM) NilSubmissionForm selection, Model model) {

    featureFlagsConfig.checkNilSubmissionEnabled();

    NilSubmissionSessionManager.nilSubmissionCleanseSession(
        selection, NilSubmissionPage.SUBMISSION_PERIOD);

    SubmissionsResultSet submissionsResults = submissionPeriodService.searchSubmissions(selection);
    Map<String, String> sortedSubmissionPeriods =
        submissionPeriodService.sortSubmissionPeriods(
            submissionPeriodService.getMonthsWithOutSubmissions(submissionsResults));

    if (sortedSubmissionPeriods.isEmpty()) {
      return "pages/nil-submission-no-submission-periods";
    }
    model.addAttribute("submissionPeriods", sortedSubmissionPeriods);
    return "pages/nil-submission-period";
  }

  @PostMapping("/nil-submission-period")
  public String postSubmissionPeriod(
      @ModelAttribute(NIL_SUBMISSION_FORM) NilSubmissionForm form,
      @RequestParam String submissionPeriod) {

    featureFlagsConfig.checkNilSubmissionEnabled();

    form.setSubmissionPeriod(submissionPeriod);

    return "redirect:/nil-submission-reference";
  }
}
