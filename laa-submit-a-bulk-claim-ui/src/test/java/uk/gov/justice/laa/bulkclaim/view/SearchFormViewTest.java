package uk.gov.justice.laa.bulkclaim.view;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.Errors;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper;
import uk.gov.justice.laa.bulkclaim.controller.SearchController;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;
import uk.gov.justice.laa.bulkclaim.util.PaginationLinksBuilder;
import uk.gov.justice.laa.bulkclaim.util.PaginationUtil;
import uk.gov.justice.laa.bulkclaim.util.SubmissionPeriodUtil;
import uk.gov.justice.laa.bulkclaim.validation.SubmissionSearchValidator;

@WebMvcTest(SearchController.class)
class SearchFormViewTest extends ViewTestBase {

  @MockitoBean DataClaimsRestClient claimsRestService;
  @MockitoBean SubmissionSearchValidator submissionSearchValidator;
  @MockitoBean PaginationUtil paginationUtil;
  @MockitoBean OidcAttributeUtils oidcAttributeUtils;
  @MockitoBean PaginationLinksBuilder paginationLinksBuilder;

  @MockitoBean("submissionPeriodUtil")
  SubmissionPeriodUtil submissionPeriodUtil;

  SearchFormViewTest() {
    this.mapping = "/submissions/search";
  }

  @BeforeEach
  void setUpValidator() {
    when(submissionSearchValidator.supports(any())).thenReturn(true);
  }

  @Test
  @DisplayName("Search form renders with page heading")
  void searchFormRendersWithPageHeading() {
    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("12345"));

    var doc = renderDocument();

    assertPageHasHeading(doc, "Search for a submission");
  }

  @Test
  @DisplayName("Search form renders with search button")
  void searchFormRendersWithSearchButton() {
    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("12345"));

    var doc = renderDocument();

    assertPageHasPrimaryButton(doc, "Search");
  }

  @Test
  @DisplayName("Search form shows office error summary when no office is selected")
  void searchFormDisplaysOfficeValidationError() {
    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("12345"));
    doAnswer(
            invocation -> {
              Errors errors = invocation.getArgument(1);
              errors.rejectValue("offices", "search.error.offices.empty", "Select an office code");
              return null;
            })
        .when(submissionSearchValidator)
        .validate(any(), any());

    var params = new LinkedMultiValueMap<String, String>();
    params.add("submissionStatuses", "SUCCEEDED");
    var doc = renderSearchFormPost(params);

    assertPageHasErrorSummary(doc, "offices-input");
  }

  private Document renderSearchFormPost(MultiValueMap<String, String> params) {
    try {
      String html =
          mockMvc
              .perform(
                  post(mapping)
                      .with(csrf())
                      .params(params)
                      .session(session)
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser())))
              .andExpect(status().is(200))
              .andReturn()
              .getResponse()
              .getContentAsString();
      return Jsoup.parse(html);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
