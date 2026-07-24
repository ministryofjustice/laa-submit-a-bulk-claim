package uk.gov.justice.laa.bulkclaim.controller.nilsubmission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.List;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.util.OidcAttributeUtils;
import uk.gov.justice.laa.bulkclaim.view.ViewTestBase;

@WebMvcTest(NilSubmissionOfficeController.class)
@AutoConfigureMockMvc(addFilters = false)
class NilSubmissionOfficeViewTest extends ViewTestBase {

  @MockitoBean private OidcAttributeUtils oidcAttributeUtils;

  NilSubmissionOfficeViewTest() {
    this.mapping = "/nil-submission/office";
  }

  @Test
  void invalidOfficeShowsInlineError() throws Exception {
    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("OfficeA", "OfficeB"));

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("office", "UnauthorizedOffice");

    NilSubmissionForm form = new NilSubmissionForm();
    session.setAttribute("nilSubmissionForm", form);

    var doc =
        mockMvc
            .perform(post(mapping).with(csrf()).params(params).session(session))
            .andReturn()
            .getResponse();

    var document = Jsoup.parse(doc.getContentAsString());

    assertThat(doc.getStatus()).isEqualTo(200);
    assertFormFieldHasErrorMessage(document, "Select a valid office account number");
  }

  @Test
  void officeNotSelectedShowsInlineError() throws Exception {
    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("OfficeA", "OfficeB"));

    NilSubmissionForm form = new NilSubmissionForm();
    session.setAttribute("nilSubmissionForm", form);

    var doc =
        mockMvc.perform(post(mapping).with(csrf()).session(session)).andReturn().getResponse();

    var document = Jsoup.parse(doc.getContentAsString());

    assertThat(doc.getStatus()).isEqualTo(200);
    assertFormFieldHasInlineErrorMessage(document, "Select the office account number");
  }
}
