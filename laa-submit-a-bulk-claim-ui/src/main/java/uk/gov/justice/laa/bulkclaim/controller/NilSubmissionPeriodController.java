package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.NIL_SUBMISSION_SELECTION;
import static uk.gov.justice.laa.bulkclaim.controller.NilSubmissionController.AREA_OF_LAW_SELECTION;
import static uk.gov.justice.laa.bulkclaim.controller.NilSubmissionController.OFFICE_SELECTION;
import static uk.gov.justice.laa.bulkclaim.controller.NilSubmissionController.SCHEDULE_REFERENCE_SELECTION;
import static uk.gov.justice.laa.bulkclaim.controller.NilSubmissionController.SUBMISSION_PERIOD_SELECTION;
import static uk.gov.justice.laa.bulkclaim.dto.SubmissionOutcomeFilter.SUCCEEDED;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.dto.submission.search.SubmissionSearchQuery;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;
import uk.gov.justice.laa.bulkclaim.util.SubmissionPeriodUtil;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

@Controller
@RequiredArgsConstructor
@SessionAttributes("nilSubmissionForm")
public class NilSubmissionPeriodController {

  private final OidcAttributeUtils oidcAttributeUtils;
  private final SubmissionPeriodUtil submissionPeriodUtil;
  private final FeatureFlagsConfig featureFlagsConfig;
  private final DataClaimsRestClient claimsRestService;

  @GetMapping("/nil-submission/{office}")
  public String getPage(
      @ModelAttribute("nilSubmissionForm") NilSubmissionForm selection,
      @PathVariable String office,
      Model model,
      @AuthenticationPrincipal OidcUser oidcUser) {

    String areaOfLaw = selection.getAreaOfLaw();
    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
      return "error";
    }
    var offices = oidcAttributeUtils.getUserOffices(oidcUser);

    if (!offices.contains(office)) {
      throw new SubmitBulkClaimException(
          "User (%s) does not have access to office: %s"
              .formatted(oidcUser.getPreferredUsername(), office));
    }
    //
    //        if (selection.getAreaOfLaw() == null) {
    //            return "redirect:/nil-submission-areaoflaw";
    //        }

    SubmissionSearchQuery submissionSearchQuery =
        SubmissionSearchQuery.builder()
            .areaOfLaw(areaOfLaw)
            .offices(List.of(office))
            .submissionStatuses(SUCCEEDED)
            .build();

    SubmissionsResultSet submissionsResults =
        claimsRestService
            .search(
                Collections.singletonList(office),
                null,
                getAreaOfLaw(areaOfLaw),
                List.of(SubmissionStatus.VALIDATION_SUCCEEDED),
                submissionSearchQuery.getPage(),
                12,
                getSubmissionDateFrom(),
                getSubmissionDateTo(),
                // pre-existing was wrong
                "createdOn,desc")
            .block();

    model.addAttribute("submissionPeriods", getMonthsWithOutSubmissions(submissionsResults));
    // model.addAttribute("submissionSearchQuery", submissionSearchQuery);
    return "pages/nil-submission-period";
  }

  @PostMapping("/nil-submission/{office}")
  public String postPage(
      @ModelAttribute("nilSubmissionForm") NilSubmissionForm form,
      @RequestParam String submissionPeriod) {

    System.out.println("form: " + form);
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

  HashMap<String, String> getMonthsWithOutSubmissions(SubmissionsResultSet submissionsResults) {
    HashMap<String, String> nonSubmissionMonths = getLastTwelveMonths();

    if (submissionsResults != null && submissionsResults.getContent() != null) {
      submissionsResults
          .getContent()
          .forEach(
              submission -> {
                String period = submissionPeriodUtil.getSubmissionPeriod(submission);
                System.out.println("period: " + period);
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

  static HashMap<String, String> getLastTwelveMonths() {
    DateTimeFormatter formatterView = DateTimeFormatter.ofPattern("MMMM uuuu", Locale.ENGLISH);
    DateTimeFormatter formatterKey = DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH);
    HashMap<String, String> months =
        new LinkedHashMap<>(
            Stream.iterate(YearMonth.now(), m -> m.minusMonths(1))
                .limit(12)
                .collect(
                    Collectors.toMap(m -> m.format(formatterKey), m -> m.format(formatterView))));
    ;
    return months;
  }
}
