package uk.gov.justice.laa.cwa.bulkupload.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.security.Principal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import uk.gov.justice.laa.cwa.bulkupload.config.WebMvcTestConfig;

@WebMvcTest(HomeController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebMvcTestConfig.class)
class HomeControllerTest {

  private static final String TEST_USER = "TESTUSER";

  @InjectMocks private HomeController homeController;

  @MockitoBean private Principal principal;

  private MockMvcTester mockMvc;

  @BeforeEach
  void beforeEach() {
    when(principal.getName()).thenReturn(TEST_USER);
    mockMvc = MockMvcTester.create(standaloneSetup(homeController).build());
  }

  @Nested
  @DisplayName("GET: /")
  class GetHomeTests {

    @Test
    @DisplayName("Should return expected result")
    void shouldReturnExpectedResult() {
      assertThat(mockMvc.perform(get("/"))).hasStatus(200).hasViewName("pages/home");
    }
  }
}
