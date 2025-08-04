package uk.gov.justice.laa.cwa.bulkupload.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.security.Principal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(HomeController.class)
@AutoConfigureMockMvc(addFilters = false)
class HomeControllerTest {

  @InjectMocks private HomeController homeController;

  private MockMvcTester mockMvc;

  @Mock private Principal principal;

  @BeforeEach
  void beforeEach() {
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
