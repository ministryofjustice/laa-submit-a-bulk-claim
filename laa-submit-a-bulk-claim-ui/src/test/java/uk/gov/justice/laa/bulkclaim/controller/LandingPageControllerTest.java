package uk.gov.justice.laa.bulkclaim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.client.RestClient;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;

@WebMvcTest(LandingPageController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Landing page controller test")
@Import(WebMvcTestConfig.class)
class LandingPageControllerTest {

  @Autowired private MockMvcTester mockMvc;

  @MockitoBean private RestClient.Builder builder;

  @Test
  @DisplayName("Should render landing page")
  void shouldRenderLandingPage() {
    assertThat(mockMvc.perform(get("/"))).hasStatusOk().hasViewName("pages/landing");
  }
}
