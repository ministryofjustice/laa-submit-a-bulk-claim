package uk.gov.justice.laa.bulkclaim.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.justice.laa.bulkclaim.dto.PaginationLinks;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;

class PaginationLinksBuilderTest {

  private final PaginationLinksBuilder paginationLinksBuilder =
      new PaginationLinksBuilder(new ThymeleafHrefUtils());

  @Test
  void shouldBuildPaginationLinksWithRepeatedParamsAndAnchor() {
    Page page = Page.builder().number(1).totalPages(3).build();

    PaginationLinks links =
        paginationLinksBuilder.build(
            "/view-submission-detail#claims-table",
            page,
            "page",
            "submissionId",
            "123",
            "offices",
            List.of("A", "B"));

    assertThat(links.previousHref())
        .isEqualTo(
            "/view-submission-detail?page=0&submissionId=123&offices=A&offices=B#claims-table");
    assertThat(links.nextHref())
        .isEqualTo(
            "/view-submission-detail?page=2&submissionId=123&offices=A&offices=B#claims-table");
    assertThat(links.pageLinks().get(1).pageNumber()).isEqualTo(1);
    assertThat(links.pageLinks().get(1).href())
        .isEqualTo(
            "/view-submission-detail?page=1&submissionId=123&offices=A&offices=B#claims-table");
  }
}
