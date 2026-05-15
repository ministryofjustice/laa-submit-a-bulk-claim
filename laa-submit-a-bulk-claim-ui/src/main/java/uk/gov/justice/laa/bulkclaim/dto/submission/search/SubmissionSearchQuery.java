package uk.gov.justice.laa.bulkclaim.dto.submission.search;

import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.laa.bulkclaim.dto.PageQuery;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionOutcomeFilter;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionsSearchForm;
import uk.gov.justice.laa.bulkclaim.dto.sorting.SortDirection;

@Builder
@Getter
public class SubmissionSearchQuery
    implements PageQuery<SubmissionSearchSortField, SubmissionSearchSort> {

  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_PAGE_SIZE = 10;

  private Integer page;

  private SubmissionSearchSort sort;

  private String submissionPeriod;
  private String areaOfLaw;
  private List<String> offices;
  private SubmissionOutcomeFilter submissionStatuses;

  public SubmissionSearchQuery(
      Integer page,
      SubmissionSearchSort sort,
      String submissionPeriod,
      String areaOfLaw,
      List<String> offices,
      SubmissionOutcomeFilter submissionStatuses) {
    this.page = Objects.requireNonNullElse(page, DEFAULT_PAGE);
    this.sort = Objects.requireNonNullElse(sort, SubmissionSearchSort.defaults());
    this.submissionPeriod = submissionPeriod;
    this.areaOfLaw = areaOfLaw;
    this.offices = offices;
    this.submissionStatuses = submissionStatuses;
  }

  public static SubmissionSearchQuery from(SubmissionsSearchForm form) {
    return SubmissionSearchQuery.builder()
        .submissionPeriod(form.submissionPeriod())
        .areaOfLaw(form.areaOfLaw())
        .offices(form.offices())
        .submissionStatuses(form.submissionStatuses())
        .build();
  }

  @Override
  public Integer getSize() {
    return DEFAULT_PAGE_SIZE;
  }

  @Override
  public String getRedirectUrl(SubmissionSearchSortField field, SortDirection direction) {
    return getRedirectUrl(
        DEFAULT_PAGE, SubmissionSearchSort.builder().field(field).direction(direction).build());
  }

  @Override
  public String getRedirectUrl(int page, SubmissionSearchSort sort) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/submissions/search/results");

    addQueryParam(builder, "page", String.valueOf(page));
    addQueryParam(builder, "submissionPeriod", submissionPeriod);
    addQueryParam(builder, "areaOfLaw", trimToNull(areaOfLaw));
    addQueryParamIfNotEmptyList(builder, "offices", offices);
    addQueryParam(builder, "submissionStatuses", submissionStatuses);
    addQueryParam(builder, "sort", Objects.toString(sort, null));

    return builder.build().toUriString();
  }

  private static String trimToNull(String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  private static void addQueryParamIfNotEmptyList(
      UriComponentsBuilder redirectUrl, String name, List<?> values) {
    if (values != null && !values.isEmpty()) {
      redirectUrl.queryParam(name, values.toArray());
    }
  }
}
