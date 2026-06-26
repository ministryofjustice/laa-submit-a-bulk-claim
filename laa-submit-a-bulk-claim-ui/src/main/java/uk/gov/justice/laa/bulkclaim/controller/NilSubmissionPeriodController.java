package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.NIL_SUBMISSION_SELECTION;
import static uk.gov.justice.laa.bulkclaim.dto.SubmissionOutcomeFilter.SUCCEEDED;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.dto.submission.search.SubmissionSearchQuery;
import uk.gov.justice.laa.bulkclaim.util.DateWrapperUtil;
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionPage;
import uk.gov.justice.laa.bulkclaim.util.NilSubmissionSessionManager;
import uk.gov.justice.laa.bulkclaim.util.SubmissionPeriodUtil;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

@Controller
@RequiredArgsConstructor
@SessionAttributes("nilSubmissionForm")
public class NilSubmissionPeriodController {

  private static final String OFFICE_SELECTION = "office";
  private static final String AREA_OF_LAW_SELECTION = "areaOfLaw";
  private static final String SUBMISSION_PERIOD_SELECTION = "submissionPeriod";
  private static final String SCHEDULE_REFERENCE_SELECTION = "scheduleReference";

  private final SubmissionPeriodUtil submissionPeriodUtil;
  private final FeatureFlagsConfig featureFlagsConfig;
  private final DataClaimsRestClient claimsRestService;

  @GetMapping("/nil-submission-period")
  public String getSubmissionPeriods(
      @ModelAttribute("nilSubmissionForm") NilSubmissionForm selection,
      Model model,
      NilSubmissionForm form) {

    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
      return "error";
    }

    NilSubmissionSessionManager.nilSubmissionCleanseSession(
        form, NilSubmissionPage.SUBMISSION_PERIOD);

    SubmissionSearchQuery submissionSearchQuery =
        SubmissionSearchQuery.builder()
            .areaOfLaw(selection.getAreaOfLaw())
            .offices(List.of(selection.getOffice()))
            .submissionStatuses(SUCCEEDED)
            .build();

    SubmissionsResultSet submissionsResults =
        claimsRestService
            .search(
                Collections.singletonList(selection.getOffice()),
                null,
                getAreaOfLaw(selection.getAreaOfLaw()),
                List.of(SubmissionStatus.VALIDATION_SUCCEEDED),
                submissionSearchQuery.getPage(),
                12,
                getSubmissionDateFrom(),
                getSubmissionDateTo(),
                // pre-existing was wrong
                "createdOn,desc")
            .block();

    if (submissionsResults == null) {
      return "pages/nil-submission-no-periods";
    }
    model.addAttribute("submissionPeriods", getMonthsWithOutSubmissions(submissionsResults));
    return "pages/nil-submission-period";
  }

  @PostMapping("/nil-submission-period")
  public String postSubmissionPeriod(
      @ModelAttribute("nilSubmissionForm") NilSubmissionForm form,
      @RequestParam String submissionPeriod) {

    form.setSubmissionPeriod(submissionPeriod);

    return "redirect:/nil-submission-reference";
  }

  private static AreaOfLaw getAreaOfLaw(String areaOfLaw) {
    try {
      return Objects.isNull(areaOfLaw)
          ? null
          : AreaOfLaw.fromValue(areaOfLaw.replace("_", " ").toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  Map<String, String> getMonthsWithOutSubmissions(SubmissionsResultSet submissionsResults) {
    Map<String, String> nonSubmissionMonths = getLastTwelveMonths();

    if (submissionsResults != null && submissionsResults.getContent() != null) {
      submissionsResults
          .getContent()
          .forEach(
              submission -> {
                String period = submissionPeriodUtil.getSubmissionPeriod(submission);
                if (period != null) {
                  nonSubmissionMonths.remove(period);
                }
              });
    }

    return nonSubmissionMonths;
  }

  private String getSubmissionDateTo() {
    LocalDate lastDateOfMonth = YearMonth.from(LocalDate.now()).minusMonths(1).atEndOfMonth();
    return getFormatted(lastDateOfMonth);
  }

  private static @NonNull String getFormatted(LocalDate lastDateOfMonth) {
    return lastDateOfMonth.format(DateTimeFormatter.ISO_LOCAL_DATE);
  }

  private String getSubmissionDateFrom() {
    LocalDate lastDateOfMonth = YearMonth.from(LocalDate.now()).minusYears(1).atEndOfMonth();
    return getFormatted(lastDateOfMonth);
  }

  @ModelAttribute(NIL_SUBMISSION_SELECTION)
  public Map<String, String> nilSubmissionSelection() {
    HashMap<String, String> selectionMap = new HashMap<>();
    selectionMap.put(OFFICE_SELECTION, "");
    selectionMap.put(AREA_OF_LAW_SELECTION, "");
    selectionMap.put(SUBMISSION_PERIOD_SELECTION, "");
    selectionMap.put(SCHEDULE_REFERENCE_SELECTION, "");
    return selectionMap;
  }

  static Map<String, String> getLastTwelveMonths() {
    DateTimeFormatter formatterKey = DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH);
    SubmissionPeriodUtil submissionPeriodUtil =
        new SubmissionPeriodUtil(
            new DateWrapperUtil(), YearMonth.now().minusMonths(12).format(formatterKey));
    return submissionPeriodUtil.getAllPossibleSubmissionPeriods();
  }
}
