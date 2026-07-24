package uk.gov.justice.laa.bulkclaim.view.nilsubmission;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper;
import uk.gov.justice.laa.bulkclaim.controller.nilsubmission.NilSubmissionAreaOfLawController;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.view.ViewTestBase;

@WebMvcTest(NilSubmissionAreaOfLawController.class)
class NilSubmissionAreaOfLawViewTest extends ViewTestBase {

  NilSubmissionAreaOfLawViewTest() {
    this.mapping = "/nil-submission/areaoflaw";
  }

  @Test
  void invalidAreaOfLawShowsInlineError() throws Exception {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("areaOfLaw", "potato");

    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    session.setAttribute("nilSubmissionForm", form);

    var doc =
        mockMvc
            .perform(
                post(mapping)
                    .with(csrf())
                    .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                    .params(params)
                    .session(session))
            .andReturn()
            .getResponse();

    var document = Jsoup.parse(doc.getContentAsString());

    assertFormFieldHasErrorMessage(document, "Select a valid area of law");
  }

  @Test
  void areaOfLawNotSelectedShowsInlineError() throws Exception {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    session.setAttribute("nilSubmissionForm", form);

    var doc =
        mockMvc
            .perform(
                post(mapping)
                    .with(csrf())
                    .with(oidcLogin().oidcUser(ControllerTestHelper.getOidcUser()))
                    .session(session))
            .andReturn()
            .getResponse();

    var document = Jsoup.parse(doc.getContentAsString());

    assertFormFieldHasInlineErrorMessage(document, "Select the area of law");
  }
}
