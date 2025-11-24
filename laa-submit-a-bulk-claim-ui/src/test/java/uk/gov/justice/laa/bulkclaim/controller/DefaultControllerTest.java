package uk.gov.justice.laa.bulkclaim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.client.RestClient;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;

@WebMvcTest(DefaultController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcTestConfig.class)
class DefaultControllerTest {

  @Autowired private MockMvcTester mockMvc;

  @MockitoBean private RestClient.Builder builder;

  @Test
  void shouldReturnLoggedOutView() {
    assertThat(mockMvc.perform(get("/logged-out"))).hasStatusOk().hasViewName("pages/logged-out");
  }
}
