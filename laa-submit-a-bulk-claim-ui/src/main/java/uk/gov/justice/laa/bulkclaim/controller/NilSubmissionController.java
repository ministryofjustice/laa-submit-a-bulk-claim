package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.NIL_SUBMISSION_SELECTION;
import static uk.gov.justice.laa.bulkclaim.dto.SubmissionOutcomeFilter.SUCCEEDED;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.justice.laa.bulkclaim.dto.sorting.SortDirection;
import uk.gov.justice.laa.bulkclaim.dto.submission.search.SubmissionSearchQuery;
import uk.gov.justice.laa.bulkclaim.dto.submission.search.SubmissionSearchSort;
import uk.gov.justice.laa.bulkclaim.dto.submission.search.SubmissionSearchSortField;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;
import uk.gov.justice.laa.bulkclaim.util.SubmissionPeriodUtil;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

/** Controller providing a nil-submission view. Accepts an office account as a parameter. */
@Slf4j
@Controller
@RequiredArgsConstructor
@SessionAttributes({NIL_SUBMISSION_SELECTION})
public class NilSubmissionController {

  public static final String OFFICE_SELECTION = "office";
  public static final String AREA_OF_LAW_SELECTION = "areaOfLaw";
  public static final String SUBMISSION_PERIOD_SELECTION = "submissionPeriod";
  public static final String SCHEDULE_REFERENCE_SELECTION = "scheduleReference";
  private final OidcAttributeUtils oidcAttributeUtils;
  private final DataClaimsRestClient claimsRestService;
  private final SubmissionPeriodUtil submissionPeriodUtil;
  private final FeatureFlagsConfig featureFlagsConfig;

//  @GetMapping("/nil-submission")
//  public String getNilSubmission(Model model, @AuthenticationPrincipal OidcUser oidcUser) {
//
//    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
//      return "error";
//    }
//
//    var userOffices = oidcAttributeUtils.getUserOffices(oidcUser);
//    model.addAttribute("userOffices", userOffices);
//    return "pages/nil-submission-office";
//  }
//
//  @PostMapping("/nil-submission-office")
//  public String getNilSubmissionOffice(
//      Model model,
//      @ModelAttribute("") Map<String, String> selection,
//      @AuthenticationPrincipal OidcUser oidcUser,
//      @RequestParam String offices) {
//
//    model.addAttribute(NIL_SUBMISSION_SELECTION, selection);
//    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
//      return "error";
//    }
//    // Store selection
//    if (selection != null) {
//      model.addAttribute(NIL_SUBMISSION_SELECTION, offices);
//    }
//
//    // Provide the Area of Law options for the selected office
//    Set<String> areasOfLaw =
//        new HashSet<>(Set.of(AreaOfLaw.values()).stream().map(Enum::name).toList());
//    model.addAttribute(AREA_OF_LAW_SELECTION, areasOfLaw);
//
//    return "pages/nil-submission-areaoflaw";
//  }
//
//  @PostMapping("/nil-submission-areaoflaw")
//  public String getNilSubmissionAreaOfLaw(
//      Model model,
//      @ModelAttribute(NIL_SUBMISSION_SELECTION) Map<String, String> selection,
//      @RequestParam String areaOfLaw,
//      @AuthenticationPrincipal OidcUser oidcUser) {
//
//    model.addAttribute(NIL_SUBMISSION_SELECTION, selection);
//    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
//      return "error";
//    }
//
//    //      // Store selection
//    if (selection != null) {
//      model.addAttribute(NIL_SUBMISSION_SELECTION, areaOfLaw);
//    }
//    //
//    //    var userOffices = oidcAttributeUtils.getUserOffices(oidcUser);
//    //    model.addAttribute("userOffices", userOffices);
//
//    return "pages/nil-submission-areaoflaw";
//  }

//  @GetMapping("/nil-submission/{office}")
//  public String getNilSubmissionPeriod(
//      Model model,
//      @PathVariable String office,
//      @RequestParam(value = "areaOfLaw") String areaOfLaw,
//      @AuthenticationPrincipal OidcUser oidcUser) {
//
//    if (!featureFlagsConfig.getIsNilSubmissionEnabled()) {
//      return "error";
//    }
//    var offices = oidcAttributeUtils.getUserOffices(oidcUser);
//    if (!offices.contains(office)) {
//      throw new SubmitBulkClaimException(
//          "User (%s) does not have access to office: %s"
//              .formatted(oidcUser.getPreferredUsername(), office));
//    }
//    SubmissionSearchQuery submissionSearchQuery =
//        SubmissionSearchQuery.builder()
//            .areaOfLaw(areaOfLaw)
//            .offices(List.of(office))
//            .submissionStatuses(SUCCEEDED)
//            .build();
//    SubmissionsResultSet submissionsResults =
//        claimsRestService
//            .search(
//                Collections.singletonList(office),
//                null,
//                getAreaOfLaw(areaOfLaw),
//                List.of(SubmissionStatus.VALIDATION_SUCCEEDED),
//                submissionSearchQuery.getPage(),
//                12,
//                getSubmissionDateFrom(),
//                getSubmissionDateTo(),
//                Objects.toString(
//                    SubmissionSearchSort.builder()
//                        .field(SubmissionSearchSortField.SUBMISSION_PERIOD)
//                        .direction(SortDirection.DESCENDING)))
//            .block();
//
//    model.addAttribute("submissionPeriods", getMonthsWithOutSubmissions(submissionsResults));
//    model.addAttribute("submissionSearchQuery", submissionSearchQuery);
//
//    return "pages/nil-submission-period";
//  }

  private static AreaOfLaw getAreaOfLaw(String areaOfLaw) {
    try {
      return Objects.isNull(areaOfLaw)
          ? null
          : AreaOfLaw.fromValue(areaOfLaw.replace("_", " ").toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  Set<String> getMonthsWithOutSubmissions(SubmissionsResultSet submissionsResults) {
    Set<String> nonSubmissionMonths = getLastTwelveMonths();

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

  static Set<String> getLastTwelveMonths() {
    Set<String> months = new LinkedHashSet<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM uuuu", Locale.ENGLISH);
    Stream.iterate(YearMonth.now(), m -> m.minusMonths(1))
        .limit(12)
        .map(m -> m.format(formatter))
        .forEach(months::add);
    return months;
  }
}
