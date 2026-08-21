package uk.gov.justice.laa.bulkclaim.view;

import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper.OIDC_USER;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionClaimDetailsBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMatterStartsDetailsBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionMessagesBuilder;
import uk.gov.justice.laa.bulkclaim.builder.SubmissionSummaryBuilder;
import uk.gov.justice.laa.bulkclaim.constants.ViewSubmissionNavigationTab;
import uk.gov.justice.laa.bulkclaim.controller.SubmissionDetailController;
import uk.gov.justice.laa.bulkclaim.dto.PaginationLinks;
import uk.gov.justice.laa.bulkclaim.dto.PaginationPageLink;
import uk.gov.justice.laa.bulkclaim.util.PaginationLinksBuilder;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.Page;

@WebMvcTest(SubmissionDetailController.class)
public abstract class SubmissionDetailsViewTestBase extends ViewTestBase {

  protected static final int PAGE_SIZE = 50;
  protected static final String OFFICE_CODE = "123456";

  @MockitoBean protected SubmissionSummaryBuilder submissionSummaryBuilder;
  @MockitoBean protected SubmissionClaimDetailsBuilder submissionClaimDetailsBuilder;
  @MockitoBean protected SubmissionMessagesBuilder submissionMessagesBuilder;
  @MockitoBean protected SubmissionMatterStartsDetailsBuilder submissionMatterStartsDetailsBuilder;
  @MockitoBean protected PaginationLinksBuilder paginationLinksBuilder;

  protected SubmissionDetailsViewTestBase() {
    this.mapping = "/submissions/%s".formatted(submissionId);
  }

  @BeforeEach
  void beforeEach() {
    when(oidcAttributeUtils.getUserOffices(OIDC_USER)).thenReturn(List.of(OFFICE_CODE));
  }

  protected static Page pagination(int currentPage, int totalPages) {
    return Page.builder()
        .number(currentPage)
        .totalPages(totalPages)
        .size(PAGE_SIZE)
        .totalElements(totalPages * PAGE_SIZE)
        .build();
  }

  protected static PaginationLinks buildSubmissionDetailPaginationLinks(
      UUID submissionId,
      int currentPage,
      int totalPages,
      String pageVarName,
      ViewSubmissionNavigationTab navTab,
      String sort) {
    List<PaginationPageLink> pageLinks = new ArrayList<>();
    for (int pageNumber = 0; pageNumber < totalPages; pageNumber++) {
      pageLinks.add(
          new PaginationPageLink(
              pageNumber,
              submissionDetailPageHref(submissionId, pageVarName, pageNumber, navTab, sort)));
    }
    String previousHref =
        currentPage > 0
            ? submissionDetailPageHref(submissionId, pageVarName, currentPage - 1, navTab, sort)
            : null;
    String nextHref =
        currentPage < totalPages - 1
            ? submissionDetailPageHref(submissionId, pageVarName, currentPage + 1, navTab, sort)
            : null;
    return new PaginationLinks(previousHref, nextHref, pageLinks);
  }

  protected static String submissionDetailPageHref(
      UUID submissionId,
      String pageVarName,
      int pageNumber,
      ViewSubmissionNavigationTab navTab,
      String sort) {
    if ("messagesPage".equals(pageVarName)) {
      return "/submissions/%s?navTab=%s&messagesPage=%s&messagesSort=%s"
          .formatted(submissionId, navTab, pageNumber, sort);
    }
    return "/submissions/%s?navTab=%s&page=%s&sort=%s"
        .formatted(submissionId, navTab, pageNumber, sort);
  }
}
