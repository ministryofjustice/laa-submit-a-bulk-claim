package uk.gov.justice.laa.bulkclaim.view;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.RequestDispatcher;
import java.util.Map;
import java.util.UUID;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.MultiValueMap;
import uk.gov.justice.laa.bulkclaim.config.WebMvcTestConfig;
import uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper;
import uk.gov.justice.laa.bulkclaim.util.ThymeleafHrefUtils;

@Import({WebMvcTestConfig.class, ThymeleafHrefUtils.class})
public abstract class ViewTestBase {

  @Autowired protected MockMvc mockMvc;

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
    return renderDocument(Map.of());
  }

  protected Document renderDocument(Map<String, Object> variables) {
    MockHttpServletRequestBuilder requestBuilder = get(mapping);
    for (Map.Entry<String, Object> entry : variables.entrySet()) {
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
}
