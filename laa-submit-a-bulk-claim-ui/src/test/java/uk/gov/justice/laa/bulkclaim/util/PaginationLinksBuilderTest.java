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
            "/submissions/%s#claims-table".formatted("123"),
            page,
            "page",
            "offices",
            List.of("A", "B"));

    assertThat(links.previousHref())
        .isEqualTo("/submissions/%s?page=0&offices=A&offices=B#claims-table".formatted("123"));
    assertThat(links.nextHref())
        .isEqualTo("/submissions/%s?page=2&offices=A&offices=B#claims-table".formatted("123"));
    assertThat(links.pageLinks().get(1).pageNumber()).isEqualTo(1);
    assertThat(links.pageLinks().get(1).href())
        .isEqualTo("/submissions/%s?page=1&offices=A&offices=B#claims-table".formatted("123"));
  }
}
