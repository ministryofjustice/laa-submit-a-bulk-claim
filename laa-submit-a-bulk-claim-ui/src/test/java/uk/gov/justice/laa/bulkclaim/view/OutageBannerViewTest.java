package uk.gov.justice.laa.bulkclaim.view;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.justice.laa.bulkclaim.controller.LandingPageController;
import uk.gov.justice.laa.bulkclaim.util.DateWrapperUtil;

@WebMvcTest(
    properties = {
      "app.maintenance.disable-at-time=2026-05-29T01:00:00Z",
      "app.maintenance.outage-banner-message=Service unavailable"
    },
    controllers = LandingPageController.class)
class OutageBannerViewTest extends ViewTestBase {

  OutageBannerViewTest() {
    this.mapping = "/";
  }

  @MockitoBean DateWrapperUtil dateWrapperUtil;

  @BeforeEach
  void beforeEach() {
    // Set to return midnight, whereas property is set to 1am. This should result in the banner
    // showing.
    when(dateWrapperUtil.timeNow()).thenReturn(LocalDateTime.of(2026, 5, 29, 0, 0));
  }

  @Test
  void shouldShowOutageBanner() {
    var doc = renderDocument();

    Element outageBannerElement = doc.getElementById("outage-banner");
    Element outageBannerContentElement = doc.getElementById("outage-banner-content");

    assertNotNull(outageBannerElement);
    assertNotNull(outageBannerContentElement);
    assertThat(outageBannerElement.attr("role")).isEqualTo("region");
    assertThat(outageBannerElement.attr("data-module")).isEqualTo("moj-alert");
    assertThat(outageBannerElement.attr("aria-label")).startsWith("information: ");
    assertThat(outageBannerElement.text()).contains("Service unavailable");
  }
}
