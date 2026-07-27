package uk.gov.justice.laa.bulkclaim.view.nilsubmission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.MEDIATION;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper;
import uk.gov.justice.laa.bulkclaim.controller.nilsubmission.NilSubmissionReferenceController;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.view.ViewTestBase;

@WebMvcTest(NilSubmissionReferenceController.class)
class NilSubmissionReferenceViewTest extends ViewTestBase {

  NilSubmissionReferenceViewTest() {
    this.mapping = "/nil-submission/reference";
  }

  @Test
  void invalidSubmissionReferenceShowsInlineError() throws Exception {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("submissionReference", "invalid-reference-with-hyphen");

    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    form.setAreaOfLaw(MEDIATION);
    form.setSubmissionPeriod("OCT-2025");
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

    assertThat(doc.getStatus()).isEqualTo(200);
    assertFormFieldHasErrorMessage(
        document,
        "Submission reference must be a maximum of 20 characters and contain only letters, numbers and forward slashes");
  }

  @Test
  void submissionReferenceNotSelectedShowsInlineError() throws Exception {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    form.setAreaOfLaw(MEDIATION);
    form.setSubmissionPeriod("OCT-2025");
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

    assertThat(doc.getStatus()).isEqualTo(200);
    assertFormFieldHasInlineErrorMessage(document, "Enter a submission reference");
  }
}
