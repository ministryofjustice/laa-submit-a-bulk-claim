package uk.gov.justice.laa.bulkclaim.service;

import static java.util.stream.Collectors.toMap;
import static uk.gov.justice.laa.bulkclaim.dto.SubmissionOutcomeFilter.SUCCEEDED;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.dto.submission.search.SubmissionSearchQuery;
import uk.gov.justice.laa.bulkclaim.util.DateWrapperUtil;
import uk.gov.justice.laa.bulkclaim.util.SubmissionPeriodUtil;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionBase;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

@Service
@RequiredArgsConstructor
public class SubmissionPeriodService {

  private final DataClaimsRestClient claimsRestService;
  private final DateWrapperUtil dateWrapperUtil;

  public SubmissionsResultSet searchSubmissions(NilSubmissionForm selection) {
    SubmissionSearchQuery submissionSearchQuery =
        SubmissionSearchQuery.builder()
            .areaOfLaw(selection.getAreaOfLaw())
            .offices(List.of(selection.getOffice()))
            .submissionStatuses(SUCCEEDED)
            .build();

    return claimsRestService
        .search(
            submissionSearchQuery.getOffices(),
            null,
            AreaOfLaw.valueOf(submissionSearchQuery.getAreaOfLaw()),
            submissionSearchQuery.getSubmissionStatuses().getStatuses(),
            submissionSearchQuery.getPage(),
            12,
            getSubmissionDateFrom(),
            getSubmissionDateTo(),
            "createdOn,desc")
        .block();
  }

  private String getSubmissionDateTo() {
    return getFormatted(dateWrapperUtil.now());
  }

  private static @NonNull String getFormatted(LocalDate lastDateOfMonth) {
    return lastDateOfMonth.format(DateTimeFormatter.ISO_LOCAL_DATE);
  }

  private String getSubmissionDateFrom() {
    LocalDate lastDateOfMonth = YearMonth.from(dateWrapperUtil.now()).minusYears(1).atEndOfMonth();
    return getFormatted(lastDateOfMonth);
  }

  public Map<String, String> getMonthsWithOutSubmissions(SubmissionsResultSet submissionsResults) {
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

  public Map<String, String> sortSubmissionPeriods(Map<String, String> submissionPeriods) {
    DateTimeFormatter formatter =
        new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("MMMM yyyy")
            .toFormatter(Locale.UK);

    return submissionPeriods.entrySet().stream()
        .sorted(Comparator.comparing(e -> YearMonth.parse(e.getValue(), formatter)))
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
  }

  Map<String, String> getLastTwelveMonths() {

    SubmissionPeriodUtil submissionPeriodUtil =
        new SubmissionPeriodUtil(
            dateWrapperUtil,
            dateWrapperUtil.nowYearMonth().minusMonths(12).format(SubmissionPeriodUtil.IN_FMT));
    return submissionPeriodUtil.getAllPossibleSubmissionPeriods();
  }
}
