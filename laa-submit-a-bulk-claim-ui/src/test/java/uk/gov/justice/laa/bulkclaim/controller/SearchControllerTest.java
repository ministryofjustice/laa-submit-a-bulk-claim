package uk.gov.justice.laa.bulkclaim.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper.getOidcUser;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.SubmissionsSearchForm;
import uk.gov.justice.laa.bulkclaim.validation.SubmissionSearchValidator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionBase;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionsResultSet;

@AutoConfigureMockMvc(addFilters = false)
class SearchControllerTest {

  private static final String PROVIDER = "1";
  private static final String SEARCH_TERM = "ref";
  private static final String TEST_USER = "TESTUSER";

  @Mock private Model model;
  @Mock private Principal principal;
  @Mock private DataClaimsRestClient claimsRestService;
  @Mock private BindingResult bindingResult;
  @Mock private HttpServletRequest request;
  @Mock private SubmissionSearchValidator submissionSearchValidator;

  @InjectMocks private SearchController searchController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(principal.getName()).thenReturn(TEST_USER);
    when(model.asMap()).thenReturn(Collections.emptyMap());
  }

  @Test
  @DisplayName("Search form should return submissions when parameters are valid.")
  void submissionsSearchShouldReturnSubmissionResults_whenNoErrors() {
    List<SubmissionBase> submissions = Collections.emptyList();
    String submissionId = "1234";

    SubmissionsResultSet response = new SubmissionsResultSet();
    response.setNumber(0);
    response.setTotalPages(1);
    response.setTotalElements(3);
    response.setSize(3);
    response.setContent(submissions);
    when(claimsRestService.search(eq(List.of("1")), eq(submissionId), isNull(), isNull()))
        .thenReturn(Mono.just(response));
    when(request.getQueryString()).thenReturn("submissionId=" + submissionId);
    when(request.getRequestURI()).thenReturn("/submissions/search/");
    SubmissionsSearchForm submissionsSearchForm =
        new SubmissionsSearchForm(submissionId, null, null);
    RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
    String view =
        searchController.handleSearch(
            model,
            new SubmissionsSearchForm(submissionId, null, null),
            bindingResult,
            getOidcUser(),
            redirectAttributes);

    assertEquals(0, bindingResult.getErrorCount());
    assertEquals("redirect:/submissions/search/results", view);
  }
}
