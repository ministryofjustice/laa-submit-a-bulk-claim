package uk.gov.justice.laa.bulkclaim.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.RequestDispatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.assertj.core.api.ThrowingConsumer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.MultiValueMap;
import uk.gov.justice.laa.bulkclaim.config.FeatureFlagsConfig;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper;
import uk.gov.justice.laa.bulkclaim.util.ThymeleafHrefUtils;

@Import({WebMvcTestConfig.class, ThymeleafHrefUtils.class})
public abstract class ViewTestBase {

  @Autowired protected MockMvc mockMvc;

  @MockitoBean protected FeatureFlagsConfig featureFlagsConfig;

  @BeforeEach
  public void setup() {
    session = new MockHttpSession();
  }

  protected String mapping;

  protected MockHttpSession session;

  protected UUID submissionId;
  protected UUID claimId;

  protected ViewTestBase() {
    this.submissionId = UUID.randomUUID();
    this.claimId = UUID.randomUUID();
  }

  protected Document renderDocument() {
    return renderDocument(Map.of(), Map.of());
  }

  protected Document renderDocumentWithParams(Map<String, String> params) {
    return renderDocument(params, Map.of());
  }

  protected Document renderDocumentWithFlashAttributes(Map<String, Object> flashAttributes) {
    return renderDocument(Map.of(), flashAttributes);
  }

  private Document renderDocument(
      Map<String, String> queryParams, Map<String, Object> flashAttributes) {
    MockHttpServletRequestBuilder requestBuilder = get(mapping);
    for (var entry : queryParams.entrySet()) {
      requestBuilder = requestBuilder.param(entry.getKey(), entry.getValue());
    }
    for (var entry : flashAttributes.entrySet()) {
      requestBuilder = requestBuilder.flashAttr(entry.getKey(), entry.getValue());
    }
    return renderDocument(requestBuilder, 200);
  }

  private Document renderDocument(
      MockHttpServletRequestBuilder requestBuilder, int expectedStatus) {
    try {
      String html =
          mockMvc
              .perform(
                  requestBuilder
                      .session(session)
                      .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser())))
              .andExpect(status().is(expectedStatus))
              .andReturn()
              .getResponse()
              .getContentAsString();

      return Jsoup.parse(html);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected Document renderErrorPage(int requestStatus, int responseStatus) {
    MockHttpServletRequestBuilder requestBuilder =
        get(mapping).requestAttr(RequestDispatcher.ERROR_STATUS_CODE, requestStatus);
    return renderDocument(requestBuilder, responseStatus);
  }

  protected Document renderDocumentWithErrors(MultiValueMap<String, String> params) {
    MockHttpServletRequestBuilder requestBuilder = post(mapping).with(csrf()).params(params);
    return renderDocument(requestBuilder, 400);
  }

  protected void assertPageHasHeading(Document doc, String expectedText) {
    Element heading = selectFirst(doc, "h1");
    Assertions.assertEquals(expectedText, heading.text());
  }

  protected void assertPageHasTitle(Document doc, String expectedText) {
    String title = doc.title();
    Assertions.assertEquals("Submit a bulk claim - %s".formatted(expectedText), title);
  }

  protected void assertPageBodyText(Document doc, String expectedText) {
    Elements body = doc.getElementsByClass("govuk-body");
    String bodyText = body.text();
    Assertions.assertEquals(bodyText, expectedText);
  }

  protected void assertPageDoesNotHaveBackLink(Document doc) {
    Elements elements = doc.getElementsByClass("govuk-back-link");
    Assertions.assertTrue(elements.isEmpty());
  }

  protected void assertPageHasBackLink(Document doc) {
    Elements elements = doc.getElementsByClass("govuk-back-link");
    Assertions.assertFalse(elements.isEmpty());
  }

  protected void assertPageHasPrimaryButton(Document doc, String expectedText) {
    Elements elements = doc.getElementsByClass("govuk-button");
    Assertions.assertFalse(elements.isEmpty());
    Assertions.assertEquals(expectedText, elements.getFirst().text());
  }

  protected void assertPageHasPrimaryButtonHidden(Document doc, String expectedText) {
    Elements buttons = doc.getElementsByClass("govuk-button");
    boolean found = buttons.stream().anyMatch(b -> expectedText.equals(b.text()));

    Assertions.assertFalse(found, "Button with text '" + expectedText + "' should not be present");
  }

  protected void assertPageHasSecondaryButton(Document doc, String expectedText) {
    Elements elements = doc.getElementsByClass("govuk-button--secondary");
    Assertions.assertFalse(elements.isEmpty());
    Assertions.assertEquals(expectedText, elements.getFirst().text());
  }

  protected void assertPageHasSecondaryLink(Document doc, String expectedText) {
    Elements elements = doc.getElementsByClass("govuk-link govuk-link--no-visited-state");
    Assertions.assertFalse(elements.isEmpty());
    Assertions.assertEquals(expectedText, elements.getFirst().text());
  }

  protected void assertPageCancelLinkValue(Document doc, String expectedText, String expectedHref) {
    Elements element = doc.getElementsByClass("govuk-link govuk-link--no-visited-state");
    Assertions.assertEquals(expectedText, element.text());
    Assertions.assertEquals(expectedHref, element.attr("href"));
  }

  protected void assertPageHasLink(
      Document doc, String id, String expectedText, String expectedHref) {
    Element element = getElementById(doc, id);
    Assertions.assertEquals(expectedText, element.text());
    Assertions.assertEquals(expectedHref, element.attr("href"));
  }

  protected void assertPageHasHint(Document doc, String id, String expectedText) {
    Element hint = getElementById(doc, id);
    Assertions.assertEquals(expectedText, hint.text());
  }

  protected void assertPageHasLegend(Document doc, String expectedText) {
    Element legend = selectFirst(doc, "fieldset legend");
    Assertions.assertEquals(expectedText, legend.text());
  }

  protected void assertPageHasRadioButtons(Document doc, String... expectedLabels) {
    assertPageHasRadioButtons(doc);
    Elements radioLabels = doc.getElementsByClass("govuk-radios__label");
    Assertions.assertEquals(List.of(expectedLabels), radioLabels.eachText());
  }

  protected void assertNoRadioSelected(Document doc) {
    Elements radioInputs = doc.getElementsByClass("govuk-radios__input");
    Assertions.assertFalse(radioInputs.isEmpty());
    Assertions.assertTrue(
        radioInputs.stream().noneMatch(input -> input.hasAttr("checked")),
        "Expected no radio button to be selected");
  }

  protected void assertPageHasLabel(Document doc, String id, String expectedLabel) {
    Element label = selectFirst(doc, String.format("label[for=%s]", id));
    Assertions.assertEquals(expectedLabel, label.text());
  }

  protected void assertPageHasDateInput(Document doc, String expectedLegend) {
    Element input = selectFirst(doc, ".govuk-date-input");
    Element fieldset = input.parent();
    Assertions.assertNotNull(fieldset);
    Element legend = selectFirst(fieldset, "legend");
    Assertions.assertEquals(expectedLegend, legend.text());
  }

  protected void assertPageHasActiveServiceNavigationItem(Document doc, String expectedText) {
    Element element = selectFirst(doc, ".govuk-service-navigation__item--active");
    Assertions.assertEquals(expectedText, element.text());
  }

  protected void assertPageHasNoActiveServiceNavigationItems(Document doc) {
    Elements elements = doc.getElementsByClass("govuk-service-navigation__item--active");
    Assertions.assertTrue(
        elements.isEmpty(), "Expected page to have no active service navigation items");
  }

  protected void assertPageHasActiveSubNavigationItem(
      Document doc, String expectedText, String expectedHref) {
    assertElementExists(
        doc,
        ".moj-sub-navigation__item a[aria-current=page]",
        link -> {
          assertThat(link.text()).isEqualTo(expectedText);
          assertThat(link.attr("href")).isEqualTo(expectedHref);
        });
  }

  protected void assertPageHasInactiveSubNavigationItem(
      Document doc, String expectedText, String expectedHref) {
    assertElementExists(
        doc,
        ".moj-sub-navigation__item a:not([aria-current=page])",
        link -> {
          assertThat(link.text()).isEqualTo(expectedText);
          assertThat(link.attr("href")).isEqualTo(expectedHref);
        });
  }

  protected Element selectFirst(Element element, String cssQuery) {
    Element result = element.selectFirst(cssQuery);
    Assertions.assertNotNull(
        result, String.format("Expected page to have element with CSS query '%s'", cssQuery));
    return result;
  }

  private Element getElementById(Element element, String id) {
    return selectFirst(element, String.format("#%s", id));
  }

  protected void assertH2Exists(Element parent, String expectedText) {
    assertElementExists(
        parent,
        "h2",
        h2 -> {
          assertThat(h2.text()).isEqualTo(expectedText);
        });
  }

  protected void assertParagraphExists(Element parent, String expectedText) {
    assertElementExists(
        parent,
        "p",
        p -> {
          assertThat(p.text()).isEqualTo(expectedText);
        });
  }

  protected void assertPageHasSummaryCard(Document doc, String expectedText) {
    assertElementExists(
        doc,
        ".govuk-summary-card",
        card -> {
          var text = card.select(".govuk-summary-card__title").text().trim();
          assertThat(text).isEqualTo(expectedText);
        });
  }

  protected void assertPageHasContent(Document doc, String expectedText) {
    Assertions.assertTrue(doc.text().contains(expectedText));
  }

  protected void assertPageHasTable(Document doc) {
    Elements elements = doc.getElementsByClass("govuk-table");
    Assertions.assertFalse(elements.isEmpty());
  }

  protected void assertPageHasPagination(Document doc) {
    Elements elements = doc.getElementsByClass("govuk-pagination");
    Assertions.assertFalse(elements.isEmpty());
  }

  protected void assertPaginationRenders(
      Document doc,
      String pageParamName,
      int currentPage,
      List<Integer> expectedVisiblePages,
      boolean expectedPreviousLink,
      boolean expectedNextLink,
      int expectedEllipsesCount) {
    Element pagination = selectFirst(doc, "nav.govuk-pagination");

    List<Integer> visiblePages =
        pagination
            .select(
                ".govuk-pagination__list .govuk-pagination__item:not(.govuk-pagination__item--ellipses) span")
            .stream()
            .map(Element::text)
            .map(Integer::parseInt)
            .toList();
    Assertions.assertEquals(expectedVisiblePages, visiblePages);

    Element currentPageLink =
        selectFirst(pagination, ".govuk-pagination__item--current a[aria-current=page]");
    Assertions.assertEquals("#", currentPageLink.attr("href"));
    Assertions.assertEquals(String.valueOf(currentPage + 1), currentPageLink.text());

    Elements previousLinks = pagination.select(".govuk-pagination__prev a.govuk-pagination__link");
    Assertions.assertEquals(expectedPreviousLink ? 1 : 0, previousLinks.size());
    if (expectedPreviousLink) {
      Assertions.assertTrue(
          previousLinks
              .getFirst()
              .attr("href")
              .contains("%s=%s".formatted(pageParamName, currentPage - 1)));
    }

    Elements nextLinks = pagination.select(".govuk-pagination__next a.govuk-pagination__link");
    Assertions.assertEquals(expectedNextLink ? 1 : 0, nextLinks.size());
    if (expectedNextLink) {
      Assertions.assertTrue(
          nextLinks
              .getFirst()
              .attr("href")
              .contains("%s=%s".formatted(pageParamName, currentPage + 1)));
    }

    Elements ellipses = pagination.select(".govuk-pagination__item--ellipses");
    Assertions.assertEquals(expectedEllipsesCount, ellipses.size());
  }

  protected void assertPageHasSuccessBanner(Document doc, String expectedText) {
    Element banner = selectFirst(doc, ".govuk-notification-banner--success");
    Element title = selectFirst(banner, ".govuk-notification-banner__title");
    Assertions.assertEquals("Success", title.text());
    Element content = selectFirst(banner, ".govuk-notification-banner__content");
    Assertions.assertEquals(expectedText, content.text());
  }

  protected void assertPageHasRadioButtons(Document doc) {
    Elements elements = doc.getElementsByClass("govuk-radios");
    Assertions.assertFalse(elements.isEmpty());
  }

  protected void assertPageHasInlineRadioButtons(Document doc) {
    Elements elements = doc.getElementsByClass("govuk-radios--inline");
    Assertions.assertFalse(elements.isEmpty());
  }

  protected void assertPageHasErrorSummary(Document doc, String... errorFields) {
    Element errorSummary = selectFirst(doc, ".govuk-error-summary");
    Element errorSummaryList = selectFirst(errorSummary, ".govuk-error-summary__list");

    List<String> actualErrorHrefs =
        errorSummaryList.select("li a").stream().map(element -> element.attr("href")).toList();

    List<String> expectedErrorHrefs = Arrays.stream(errorFields).map(field -> "#" + field).toList();

    Assertions.assertEquals(expectedErrorHrefs, actualErrorHrefs);
  }

  protected void assertPageHasErrorAlert(Document doc) {
    Elements elements = doc.getElementsByClass("moj-alert moj-alert--error");
    Assertions.assertFalse(elements.isEmpty());
  }

  protected void assertPageHasInformationAlert(
      Document doc, String expectedTitle, String expectedText) {
    Element element = selectFirst(doc, "#information-alert");

    Element title = selectFirst(element, ".moj-alert__heading");
    Assertions.assertEquals(expectedTitle, title.text());

    Element content = selectFirst(element, ".moj-alert__content > span");
    Assertions.assertEquals(expectedText, content.text());
  }

  protected void assertPageHasPanel(Document doc) {
    Elements elements = doc.getElementsByClass("govuk-panel");
    Assertions.assertFalse(elements.isEmpty());
  }

  protected void assertFormFieldHasErrorMessage(Document doc, String expectedErrorMessage) {
    var formGroup = doc.selectFirst(".govuk-form-group");
    Assertions.assertNotNull(formGroup, "Expected page to have a form group");
    Assertions.assertTrue(
        formGroup.className().contains("govuk-form-group--error"),
        "Expected form group to have error styling");

    var errorMessages = doc.select(".govuk-error-message");
    Assertions.assertEquals(1, errorMessages.size(), "Expected exactly one error message");

    var errorMessage = errorMessages.getFirst();
    Assertions.assertTrue(
        errorMessage.text().contains(expectedErrorMessage),
        String.format(
            "Expected error message to contain '%s' but got '%s'",
            expectedErrorMessage, errorMessage.text()));
  }

  protected void assertFormFieldHasInlineErrorMessage(Document doc, String expectedErrorMessage) {
    var errorMessages = doc.select(".govuk-error-message");
    Assertions.assertEquals(1, errorMessages.size(), "Expected exactly one error message");

    var errorMessage = errorMessages.getFirst();
    Assertions.assertTrue(
        errorMessage.text().contains(expectedErrorMessage),
        String.format(
            "Expected error message to contain '%s' but got '%s'",
            expectedErrorMessage, errorMessage.text()));
  }

  protected List<List<Element>> getTable(Document doc, String summaryCardTitle) {
    Element summaryCard = getSummaryCard(doc, summaryCardTitle);
    Element table = selectFirst(summaryCard, "table.govuk-table");
    List<List<Element>> matrix = new ArrayList<>();
    for (Element row : table.select("tbody tr")) {
      List<Element> cells = row.select("td").stream().toList();
      matrix.add(cells);
    }
    return matrix;
  }

  protected List<List<Element>> getSummaryListInCard(Document doc, String summaryCardTitle) {
    Element summaryCard = getSummaryCard(doc, summaryCardTitle);
    Element summaryList = selectFirst(summaryCard, "dl.govuk-summary-list");
    return extractElementsInSummaryList(summaryList);
  }

  protected List<List<Element>> getFirstSummaryList(Document doc) {
    Element summaryList = selectFirst(doc, "dl.govuk-summary-list");
    return extractElementsInSummaryList(summaryList);
  }

  private List<List<Element>> extractElementsInSummaryList(Element summaryList) {
    List<List<Element>> matrix = new ArrayList<>();
    for (Element row : summaryList.select(".govuk-summary-list__row")) {
      Element key = selectFirst(row, ".govuk-summary-list__key");
      List<Element> values = row.select(".govuk-summary-list__value").stream().toList();
      List<Element> actions = row.select(".govuk-summary-list__actions").stream().toList();
      List<Element> cells = new ArrayList<>();
      cells.add(key);
      cells.addAll(values);
      cells.addAll(actions);
      matrix.add(cells);
    }
    return matrix;
  }

  protected Element getSummaryCard(Document doc, String summaryCardTitle) {
    return selectFirst(
        doc,
        ".govuk-summary-card:has(h2.govuk-summary-card__title:matchesOwn(^"
            + summaryCardTitle
            + "$))");
  }

  protected void assertCellContainsText(Element cell, String expectedText) {
    var allText = cell.text() + cell.select("input").attr("value");
    Assertions.assertEquals(
        expectedText, allText, "Cell does not contain expected text: " + expectedText);
  }

  private void assertCellContainsLink(
      Element cell, String expectedText, String expectedHref, String expectedHiddenText) {
    Element link = selectFirst(cell, "a.govuk-link");
    Assertions.assertEquals(expectedText, link.ownText());
    Assertions.assertEquals(expectedHref, link.attr("href"));
    Element hiddenText = selectFirst(link, "span.govuk-visually-hidden");
    Assertions.assertEquals(String.format(" %s", expectedHiddenText), hiddenText.wholeOwnText());
  }

  protected void assertRowContainsValues(List<Element> row, String label, String... values) {
    assertThat(row.size()).isGreaterThanOrEqualTo(values.length + 1); // Plus label
    assertCellContainsText(row.getFirst(), label);
    for (int i = 0; i < values.length; i++) {
      assertCellContainsText(row.get(i + 1), values[i]);
    }
  }

  protected void assertRowContainsValuesThenLink(
      List<Element> row, String label, String linkText, String linkUrl, String... values) {
    assertThat(row.size()).isGreaterThanOrEqualTo(values.length + 2); // Plus label and link
    assertRowContainsValues(row, label, values);
    var linkCell = row.getLast();
    assertCellContainsLink(linkCell, linkText, linkUrl, label);
  }

  protected Elements getTableHeaders(Document doc) {
    return doc.getElementsByClass("govuk-table__header");
  }

  protected void assertTableHeaderIsSortable(
      Element header, String ariaSort, String expectedText, String expectedLink) {
    Assertions.assertEquals(ariaSort, header.attr("aria-sort"));
    Element link = selectFirst(header, "a");
    Element span = selectFirst(link, "span");
    Assertions.assertEquals(expectedText, span.text());
    Assertions.assertEquals(expectedLink, link.attr("href"));
  }

  protected void assertDropDownList(Document doc, String labelName, String... expectedOptions) {
    var label = selectFirst(doc, String.format("label:matchesOwn(^%s$)", Pattern.quote(labelName)));
    var id = label.attr("for");
    var select = selectFirst(doc, "#" + id);
    var options = select.children();
    for (var expectedOption : expectedOptions) {
      Assertions.assertEquals(
          expectedOption,
          options.stream()
              .map(Element::text)
              .filter(x -> Objects.equals(expectedOption, x))
              .findFirst()
              .orElse(null));
    }
  }

  protected void assertAutocompleteDropDownList(
      Document doc, String labelName, String... expectedOptions) {
    var label = selectFirst(doc, String.format("label:matchesOwn(^%s$)", Pattern.quote(labelName)));
    var id = label.attr("for");
    var select = selectFirst(doc, "#" + id);
    assertThat(select.attr("data-module")).isEqualTo("make-autocomplete");
    var parent = select.parent();
    assertThat(parent).isNotNull();
    assertThat(parent.tagName()).isEqualTo("div");
    assertThat(parent.hasClass("autocomplete-wrapper")).isTrue();

    var options = select.children();
    for (var expectedOption : expectedOptions) {
      Assertions.assertEquals(
          expectedOption,
          options.stream()
              .map(Element::text)
              .filter(x -> Objects.equals(expectedOption, x))
              .findFirst()
              .orElse(null));
    }
  }

  protected void assertTableHeaderIsNotSortable(Element header, String expectedText) {
    Assertions.assertEquals(expectedText, header.text());
  }

  protected static void assertElementExists(
      Element parentElement, String cssQuery, ThrowingConsumer<Element> assertions) {
    var elements = parentElement.selectStream(cssQuery).toList();
    assertThat(elements).isNotEmpty();
    assertThat(elements).anySatisfy(assertions);
  }

  public static Element getButtonByLabel(Document doc, String label) {
    // Matches the button's own text, ignoring surrounding whitespace
    return doc.selectFirst("button:matchesOwn(^\\s*" + Pattern.quote(label) + "\\s*$)");
  }

  protected static Stream<Arguments> detailFieldIsSortableArgs() {
    return Stream.of(
        // currentDirection, currentPage, expectedAriaDirection, expectedLinkDirection
        of("desc", 0, "descending", "asc"),
        of("asc", 0, "ascending", "desc"),
        of("asc", 5, "ascending", "desc"));
  }

  protected static Stream<Arguments> paginationRendersArgs() {

    return Stream.of(
        // currentPage, totalPages, expectedVisiblePages, expectedPreviousLink, expectedNextLink,
        // expectedEllipsesCount
        of(0, 2, List.of(1, 2), false, true, 0),
        of(1, 2, List.of(1, 2), true, false, 0),
        of(5, 10, List.of(1, 5, 6, 7, 10), true, true, 2),
        of(9, 10, List.of(1, 9, 10), true, false, 1));
  }
}
