package uk.gov.justice.laa.bulkclaim.view.nilsubmission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper.OIDC_USER;

import java.util.List;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.justice.laa.bulkclaim.controller.nilsubmission.NilSubmissionOfficeController;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.view.ViewTestBase;

@WebMvcTest(NilSubmissionOfficeController.class)
class NilSubmissionOfficeViewTest extends ViewTestBase {

  NilSubmissionOfficeViewTest() {
    this.mapping = "/nil-submission/office";
  }

  @Test
  void multipleOfficeCodesShowsRadios() {
    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("0P322F", "OTHER"));

    var doc = renderDocument();

    assertPageHasTitle(doc, "Create a nil submission");
    assertPageHasBackLink(doc);

    assertPageHasHint(doc, "nil-submission-hint", "Create a nil submission");
    assertPageHasHeading(doc, "Select the office account number");

    assertPageHasRadioButtons(doc, "0P322F", "OTHER");
    assertNoRadioSelected(doc);

    assertPageHasPrimaryButton(doc, "Continue");
    assertPageHasSecondaryLink(doc, "Cancel");
  }

  @Test
  void singleOfficeCodePreselectsRadio() {
    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of("0P322F"));

    var doc = renderDocument();

    assertPageHasTitle(doc, "Create a nil submission");
    assertPageHasBackLink(doc);

    assertPageHasHint(doc, "nil-submission-hint", "Create a nil submission");
    assertPageHasHeading(doc, "Select the office account number");

    assertPageHasRadioButtons(doc, "0P322F");
    assertThat(selectFirst(doc, ".govuk-radios__input").hasAttr("checked")).isTrue();

    assertPageHasPrimaryButton(doc, "Continue");
    assertPageHasSecondaryLink(doc, "Cancel");
  }

  @Test
  void noOfficeCodesShowsMessage() {
    when(oidcAttributeUtils.getUserOffices(any())).thenReturn(List.of());

    var doc = renderDocument();

    assertPageHasTitle(doc, "Create a nil submission");
    assertPageHasBackLink(doc);

    assertPageHasHeading(doc, "Sorry, you do not have access");
    assertPageBodyText(
        doc,
        "There are no office account numbers assigned to your sign in details. Contact your firm"
            + " LAA administrator to add the relevant office account numbers to your SILAS"
            + " account");
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
            .perform(
                post(mapping)
                    .with(csrf())
                    .with(oidcLogin().oidcUser(OIDC_USER))
                    .params(params)
                    .session(session))
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
        mockMvc
            .perform(
                post(mapping).with(csrf()).with(oidcLogin().oidcUser(OIDC_USER)).session(session))
            .andReturn()
            .getResponse();

    var document = Jsoup.parse(doc.getContentAsString());

    assertThat(doc.getStatus()).isEqualTo(200);
    assertFormFieldHasInlineErrorMessage(document, "Select the office account number");
  }
}
