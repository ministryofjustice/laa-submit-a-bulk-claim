package uk.gov.justice.laa.payments.submit.view;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import uk.gov.justice.laa.payments.submit.controller.LandingPageController;

@WebMvcTest(LandingPageController.class)
class LandingPageViewTest extends ViewTestBase {

  LandingPageViewTest() {
    this.mapping = "/";
  }

  @Test
  void landingPageShowsExpectedContent() {
    var doc = renderDocument();

    assertPageHasTitle(doc, "Submit a bulk claim");
    assertPageDoesNotHaveBackLink(doc);
    assertPageHasHeading(doc, "Submit a bulk claim");
    assertPageHasContent(
        doc,
        "Use this service to make monthly submissions for payments covering legal help, crime lower, and mediation.");
    assertPageHasContent(doc, "Your submission will be checked and calculated.");
    assertPageHasContent(doc, "Before you start");
    assertPageHasContent(doc, "Files can be in XML, CSV or TXT format.");
    assertPageHasContent(doc, "Start now");

    var startLink = doc.selectFirst("a.govuk-button--start");
    assertThat(startLink).isNotNull();
    assertThat(startLink.attr("href")).isEqualTo("/upload");
  }
}
