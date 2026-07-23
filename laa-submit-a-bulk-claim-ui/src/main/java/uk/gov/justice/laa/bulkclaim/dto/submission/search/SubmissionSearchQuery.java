package uk.gov.justice.laa.bulkclaim.dto.submission.search;

import java.util.List;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.laa.bulkclaim.dto.PageQuery;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionOutcomeFilter;
import uk.gov.justice.laa.bulkclaim.dto.sorting.SortDirection;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;

@Builder
@Getter
public class SubmissionSearchQuery
    implements PageQuery<SubmissionSearchSortField, SubmissionSearchSort> {

  private Integer page;

  private SubmissionSearchSort sort;

  private String submissionPeriod;
  private AreaOfLaw areaOfLaw;
  private List<String> offices;
  private SubmissionOutcomeFilter submissionStatuses;

  public SubmissionSearchQuery(
      Integer page,
      SubmissionSearchSort sort,
      String submissionPeriod,
      AreaOfLaw areaOfLaw,
      List<String> offices,
      SubmissionOutcomeFilter submissionStatuses) {
    this.page = Objects.requireNonNullElse(page, DEFAULT_PAGE);
    this.sort = Objects.requireNonNullElse(sort, SubmissionSearchSort.defaults());
    this.submissionPeriod = submissionPeriod;
    this.areaOfLaw = areaOfLaw;
    this.offices = offices;
    this.submissionStatuses = submissionStatuses;
  }

  public static SubmissionSearchQuery from(SubmissionSearchQuery form) {
    return SubmissionSearchQuery.builder()
        .submissionPeriod(form.getSubmissionPeriod())
        .areaOfLaw(form.getAreaOfLaw())
        .offices(form.getOffices())
        .submissionStatuses(form.getSubmissionStatuses())
        .build();
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
    addQueryParam(builder, "areaOfLaw", areaOfLaw);
    addQueryParamIfNotEmptyList(builder, "offices", offices);
    addQueryParam(builder, "submissionStatuses", submissionStatuses);
    addQueryParam(builder, "sort", Objects.toString(sort, null));

    return builder.build().toUriString();
  }

  private static void addQueryParamIfNotEmptyList(
      UriComponentsBuilder redirectUrl, String name, List<?> values) {
    if (values != null && !values.isEmpty()) {
      redirectUrl.queryParam(name, values.toArray());
    }
  }
}
