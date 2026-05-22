package uk.gov.justice.laa.bulkclaim.dto.submission.messages;

import static uk.gov.justice.laa.bulkclaim.constants.ViewSubmissionNavigationTab.CLAIM_MESSAGES;

import jakarta.validation.constraints.NotNull;
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
public class MessageQuery implements PageQuery<MessageSortField, MessageSort> {

  private static final ViewSubmissionNavigationTab DEFAULT_NAV_TAB = CLAIM_MESSAGES;

  private Integer page;
  private MessageSort sort;

  @NotNull UUID submissionId;

  private ViewSubmissionNavigationTab navTab;

  public MessageQuery(
      Integer messagesPage,
      MessageSort messagesSort,
      UUID submissionId,
      ViewSubmissionNavigationTab navTab) {
    this.page = Objects.requireNonNullElse(messagesPage, DEFAULT_PAGE);
    this.sort = Objects.requireNonNullElse(messagesSort, MessageSort.defaults());
    this.navTab = Objects.requireNonNullElse(navTab, DEFAULT_NAV_TAB);
    this.submissionId = submissionId;
  }

  @Override
  public String getRedirectUrl(MessageSortField field, SortDirection direction) {
    return getRedirectUrl(
        DEFAULT_PAGE, MessageSort.builder().field(field).direction(direction).build());
  }

  @Override
  public String getRedirectUrl(int page, MessageSort sort) {
    UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/view-submission-detail");

    addQueryParam(builder, "submissionId", submissionId);
    addQueryParam(builder, "navTab", navTab);
    addQueryParam(builder, "messagesPage", String.valueOf(page));
    addQueryParam(builder, "messagesSort", Objects.toString(sort, null));

    return builder.build().toUriString();
  }
}
