package uk.gov.justice.laa.bulkclaim.view;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.controller.SearchController;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionOutcomeFilter;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;
import uk.gov.justice.laa.bulkclaim.util.PaginationLinksBuilder;
import uk.gov.justice.laa.bulkclaim.util.PaginationUtil;
import uk.gov.justice.laa.bulkclaim.util.SubmissionPeriodUtil;
import uk.gov.justice.laa.bulkclaim.validation.SubmissionSearchValidator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionBase;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

@WebMvcTest(SearchController.class)
class SearchResultsViewTest extends ViewTestBase {

  @MockitoBean DataClaimsRestClient claimsRestService;
  @MockitoBean SubmissionSearchValidator submissionSearchValidator;
  @MockitoBean PaginationUtil paginationUtil;
  @MockitoBean OidcAttributeUtils oidcAttributeUtils;
  @MockitoBean PaginationLinksBuilder paginationLinksBuilder;

  @MockitoBean("submissionPeriodUtil") // Naming required as this bean is used in thymeleaf
  SubmissionPeriodUtil submissionPeriodUtil;

  SearchResultsViewTest() {
    this.mapping = "/submissions/search/results";
  }

  @Test
  void searchResultsHasSortableSubmissionHeaders() {
    var submission = SubmissionBase.builder().submissionId(submissionId).build();
    var response =
        SubmissionsResultSet.builder()
            .content(List.of(submission))
            .totalElements(1)
            .number(0)
            .size(10)
            .totalPages(1)
            .build();

    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("1"));
    when(claimsRestService.search(anyList(), any(), any(), any(), anyInt(), anyInt(), any()))
        .thenReturn(Mono.just(response));
    when(paginationUtil.fromSubmissionsResultSet(response, 0, 10))
        .thenReturn(new Page().totalElements(1));

    var doc =
        renderDocumentWithParams(
            Map.of("offices", "12345", "submissionStatuses", SubmissionOutcomeFilter.ALL.name()));
  }
}
