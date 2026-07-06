package uk.gov.justice.laa.bulkclaim.controller;

import static java.util.stream.Collectors.toMap;
import static uk.gov.justice.laa.bulkclaim.constants.NilSubmissionInfoMessageConstants.SUBMISSION_INFO_MESSAGE_PAGE_HEADING;
import static uk.gov.justice.laa.bulkclaim.constants.NilSubmissionInfoMessageConstants.SUBMISSION_INFO_MESSAGE_TEXT;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.NIL_SUBMISSION_FORM;

import java.time.YearMonth;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
import uk.gov.justice.laa.bulkclaim.util.DateWrapperUtil;
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionPage;
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionSessionManager;
import uk.gov.justice.laa.bulkclaim.util.SubmissionPeriodUtil;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionBase;
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

    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
      return "error";
    }

    NilSubmissionSessionManager.nilSubmissionCleanseSession(
        selection, NilSubmissionPage.SUBMISSION_PERIOD);

    SubmissionsResultSet submissionsResults = submissionPeriodService.searchSubmissions(selection);
    Map<String, String> submissionPeriods = getMonthsWithOutSubmissions(submissionsResults);
    if (submissionPeriods.isEmpty()) {
      model.addAttribute(
          SUBMISSION_INFO_MESSAGE_PAGE_HEADING, "nilSubmission.noPeriods.primary.heading");
      model.addAttribute(SUBMISSION_INFO_MESSAGE_TEXT, "nilSubmission.noPeriods.message");
      return "pages/nil-submission-info-message";
    }
    model.addAttribute("submissionPeriods", submissionPeriods);
    return "pages/nil-submission-period";
  }

  @PostMapping("/nil-submission-period")
  public String postSubmissionPeriod(
      @ModelAttribute(NIL_SUBMISSION_FORM) NilSubmissionForm form,
      @RequestParam String submissionPeriod) {

    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
      return "error";
    }
    form.setSubmissionPeriod(submissionPeriod);

    return "redirect:/nil-submission-reference";
  }

  Map<String, String> getMonthsWithOutSubmissions(SubmissionsResultSet submissionsResults) {
    Map<String, String> nonSubmissionMonths = getLastTwelveMonths();

    if (submissionsResults != null && submissionsResults.getContent() != null) {
      Set<String> submissionPeriods =
          submissionsResults.getContent().stream()
              .map(SubmissionBase::getSubmissionPeriod)
              .collect(Collectors.toSet());
      return nonSubmissionMonths.entrySet().stream()
          .filter(entry -> !submissionPeriods.contains(entry.getKey()))
          .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    return nonSubmissionMonths;
  }

  static Map<String, String> getLastTwelveMonths() {
    SubmissionPeriodUtil submissionPeriodUtil =
        new SubmissionPeriodUtil(
            new DateWrapperUtil(),
            YearMonth.now().minusMonths(12).format(SubmissionPeriodUtil.IN_FMT));
    return submissionPeriodUtil.getAllPossibleSubmissionPeriods();
  }
}
