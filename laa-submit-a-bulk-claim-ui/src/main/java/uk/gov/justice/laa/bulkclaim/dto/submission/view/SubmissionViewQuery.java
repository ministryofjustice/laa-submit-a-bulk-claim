package uk.gov.justice.laa.bulkclaim.dto.submission.view;

import static uk.gov.justice.laa.bulkclaim.constants.ViewSubmissionNavigationTab.CLAIM_DETAILS;

import java.util.Objects;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.justice.laa.bulkclaim.constants.ViewSubmissionNavigationTab;
import uk.gov.justice.laa.bulkclaim.dto.PageQuery;
import uk.gov.justice.laa.bulkclaim.dto.sorting.SortDirection;

@Builder
@Getter
public class SubmissionViewQuery implements PageQuery<SubmissionViewSortField, SubmissionViewSort> {

  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_PAGE_SIZE = 10;
  private static final ViewSubmissionNavigationTab DEFAULT_NAV_TAB = CLAIM_DETAILS;

  private Integer page;

  private SubmissionViewSort sort;

  private UUID submissionId;
  private ViewSubmissionNavigationTab navTab;

  public SubmissionViewQuery(
      Integer page,
      SubmissionViewSort sort,
      UUID submissionId,
      ViewSubmissionNavigationTab navTab) {
    this.page = Objects.requireNonNullElse(page, DEFAULT_PAGE);
    this.sort = Objects.requireNonNullElse(sort, SubmissionViewSort.defaults());
    this.navTab = Objects.requireNonNullElse(navTab, DEFAULT_NAV_TAB);
    this.submissionId = submissionId;
  }

  @Override
  public Integer getSize() {
    return DEFAULT_PAGE_SIZE;
  }

  @Override
  public String getRedirectUrl(SubmissionViewSortField field, SortDirection direction) {
    return getRedirectUrl(
        DEFAULT_PAGE, SubmissionViewSort.builder().field(field).direction(direction).build());
  }

  @Override
  public String getRedirectUrl(int page, SubmissionViewSort sort) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/view-submission-detail");

    addQueryParam(builder, "submissionId", submissionId);
    addQueryParam(builder, "navTab", navTab);
    addQueryParam(builder, "page", String.valueOf(page));
    addQueryParam(builder, "sort", Objects.toString(sort, null));

    return builder.build().toUriString();
  }
}
