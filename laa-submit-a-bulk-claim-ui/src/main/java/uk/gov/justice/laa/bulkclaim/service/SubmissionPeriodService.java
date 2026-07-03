package uk.gov.justice.laa.bulkclaim.service;

import static uk.gov.justice.laa.bulkclaim.dto.SubmissionOutcomeFilter.SUCCEEDED;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.dto.submission.search.SubmissionSearchQuery;
import uk.gov.justice.laa.bulkclaim.util.DateWrapperUtil;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
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
            getAreaOfLaw(submissionSearchQuery.getAreaOfLaw()),
            submissionSearchQuery.getSubmissionStatuses().getStatuses(),
            submissionSearchQuery.getPage(),
            12,
            getSubmissionDateFrom(),
            getSubmissionDateTo(),
            "createdOn,desc")
        .block();
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
}
