package uk.gov.justice.laa.bulkclaim.controller;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.dto.sorting.SortDirection;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.dto.submission.search.SubmissionSearchQuery;
import uk.gov.justice.laa.bulkclaim.dto.submission.search.SubmissionSearchSort;
import uk.gov.justice.laa.bulkclaim.dto.submission.search.SubmissionSearchSortField;
import uk.gov.justice.laa.bulkclaim.exception.SubmitBulkClaimException;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;
import uk.gov.justice.laa.bulkclaim.util.SubmissionPeriodUtil;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.NIL_SUBMISSION_SELECTION;
import static uk.gov.justice.laa.bulkclaim.controller.NilSubmissionController.*;
import static uk.gov.justice.laa.bulkclaim.dto.SubmissionOutcomeFilter.SUCCEEDED;

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
                                //pre-existing was wrong
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
