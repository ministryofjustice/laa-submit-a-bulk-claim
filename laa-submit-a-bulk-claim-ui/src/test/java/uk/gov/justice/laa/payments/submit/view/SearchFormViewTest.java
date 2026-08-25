package uk.gov.justice.laa.payments.submit.view;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.justice.laa.payments.submit.controller.ControllerTestHelper.OIDC_USER;

import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.justice.laa.payments.submit.controller.SearchController;
import uk.gov.justice.laa.payments.submit.dto.SubmissionOutcomeFilter;
import uk.gov.justice.laa.payments.submit.util.PaginationLinksBuilder;
import uk.gov.justice.laa.payments.submit.util.PaginationUtil;
import uk.gov.justice.laa.payments.submit.util.SubmissionPeriodUtil;
import uk.gov.justice.laa.payments.submit.validation.SubmissionSearchValidator;

@WebMvcTest(SearchController.class)
@Import(SubmissionSearchValidator.class)
class SearchFormViewTest extends ViewTestBase {

  @MockitoBean PaginationUtil paginationUtil;
  @MockitoBean PaginationLinksBuilder paginationLinksBuilder;

  @MockitoBean("submissionPeriodUtil")
  SubmissionPeriodUtil submissionPeriodUtil;

  SearchFormViewTest() {
    this.mapping = "/submissions/search";
  }

  @Test
  void searchFormRendersWithPageTitle() {
    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("12345"));

    var doc = renderDocument();

    assertPageHasTitle(doc, "Search for a submission");
  }

  @Test
  void searchFormRendersWithSearchAndClearAllButton() {
    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("12345"));

    var doc = renderDocument();

    assertPageHasPrimaryButton(doc, "Search");
    assertPageHasLink(doc, "clearAllLink", "Clear all", "/submissions/search");
  }

  @Test
  void searchFormRendersWithFilters() {
    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("12345", "67890"));
    when(submissionPeriodUtil.getAllPossibleSubmissionPeriods())
        .thenReturn(Map.of("JAN-2024", "January 2024"));

    var doc = renderDocument();

    assertPageHasContent(doc, "Filter");
    assertPageHasLabel(doc, "submission-period", "Submission period");
    assertAutocompleteDropDownList(doc, "Submission period", "January 2024");
    assertDropDownList(doc, "Area of law", "All", "Legal help", "Crime lower", "Mediation");
    assertPageHasInlineRadioButtons(doc);
    assertPageHasRadioButtons(
        doc, "Succeeded submissions", "Failed submissions", "All submissions");
    assertPageHasContent(doc, "Choose office account");

    Assertions.assertEquals("12345", doc.selectFirst("label[for=offices-input]").text());
    Assertions.assertEquals("67890", doc.selectFirst("label[for=offices-input-1]").text());
  }

  @Test
  void searchFormDisplaysOfficeValidationErrorIfNoAccountSelected() {
    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("12345"));

    var params = new LinkedMultiValueMap<String, String>();
    params.add("submissionStatuses", SubmissionOutcomeFilter.SUCCEEDED.name());
    var doc = renderSearchFormPost(params);

    assertPageHasErrorSummary(doc, "offices-input");
    assertPageHasContent(doc, "There is a problem");
    assertPageHasContent(doc, "Select an office code");
  }

  @Test
  void searchFormOpensOfficeDetailsWhenValidationFails() {
    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("12345"));

    var params = new LinkedMultiValueMap<String, String>();
    params.add("submissionStatuses", SubmissionOutcomeFilter.SUCCEEDED.name());
    var doc = renderSearchFormPost(params);

    Assertions.assertTrue(doc.selectFirst("details.govuk-details").hasAttr("open"));
  }

  private Document renderSearchFormPost(MultiValueMap<String, String> params) {
    try {
      String response =
          mockMvc
              .perform(
                  post(mapping)
                      .with(csrf())
                      .params(params)
                      .session(session)
                      .with(oidcLogin().oidcUser(OIDC_USER)))
              .andExpect(status().is(200))
              .andReturn()
              .getResponse()
              .getContentAsString();
      return Jsoup.parse(response);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
