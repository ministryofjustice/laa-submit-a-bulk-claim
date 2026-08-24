package uk.gov.justice.laa.bulkclaim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.client.RestClient;

@WebMvcTest(AccessibilityPageController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("Accessibility page test")
class AccessibilityPageControllerTest extends BaseControllerTest {

  @Autowired private MockMvcTester mockMvc;

  @MockitoBean private RestClient.Builder builder;

  @Test
  @DisplayName("Should return accessibility page")
  void getAccessibilityPage() {
    assertThat(mockMvc.perform(get("/accessibility-statement")))
        .hasStatusOk()
        .hasViewName("pages/accessibility-statement");
  }
}
