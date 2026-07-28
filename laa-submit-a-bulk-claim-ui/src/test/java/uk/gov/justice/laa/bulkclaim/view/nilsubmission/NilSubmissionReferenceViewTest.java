package uk.gov.justice.laa.bulkclaim.view.nilsubmission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.CRIME_LOWER;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.LEGAL_HELP;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.MEDIATION;

import java.util.stream.Stream;
import org.jsoup.Jsoup;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.justice.laa.bulkclaim.controller.ControllerTestHelper;
import uk.gov.justice.laa.bulkclaim.controller.nilsubmission.NilSubmissionReferenceController;
import uk.gov.justice.laa.bulkclaim.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.bulkclaim.view.ViewTestBase;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;

@WebMvcTest(NilSubmissionReferenceController.class)
class NilSubmissionReferenceViewTest extends ViewTestBase {

  NilSubmissionReferenceViewTest() {
    this.mapping = "/nil-submission/reference";
  }

  private static Stream<Arguments> referencePageContentArguments() {
    return Stream.of(
        of(
            CRIME_LOWER,
            "Crime lower",
            "CRM/0P322F/2025",
            "Crime schedule number",
            "Add your crime schedule number"),
        of(
            LEGAL_HELP,
            "Legal help",
            "0P322F/Civil/01",
            "Civil submission reference",
            "Add your civil submission reference"),
        of(
            MEDIATION,
            "Mediation",
            "0P322F/MEDI2024/01",
            "Mediation submission reference",
            "Add your mediation submission reference"));
  }

  @ParameterizedTest
  @MethodSource("referencePageContentArguments")
  void referencePageContent(
      AreaOfLaw areaOfLaw,
      String areaOfLawText,
      String exampleText,
      String labelText,
      String headingText) {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("0P322F");
    form.setOfficeCount(2);
    form.setAreaOfLaw(areaOfLaw);
    form.setSubmissionPeriod("OCT-2025");
    session.setAttribute("nilSubmissionForm", form);

    var doc = renderDocument();

    assertPageHasTitle(doc, "Create a nil submission");
    assertPageHasBackLink(doc);

    assertPageHasHint(doc, "nil-submission-hint", "Create a nil submission");
    assertPageHasHeading(doc, headingText);
    assertPageHasHint(doc, "nil-submission-example-hint", exampleText);

    var summaryList = getFirstSummaryList(doc);
    assertThat(summaryList).hasSize(3);
    assertSummaryListRowContainsValues(summaryList.get(0), "Office account number", "0P322F");
    assertSummaryListRowContainsValues(summaryList.get(1), "Area of law", areaOfLawText);
    assertSummaryListRowContainsValues(summaryList.get(2), "Submission period", "OCT-2025");

    assertPageHasLabel(doc, "submissionReference-input", labelText);
    assertThat(selectFirst(doc, "#submissionReference-input").attr("value")).isEmpty();

    assertPageHasPrimaryButton(doc, "Continue");
    assertPageHasSecondaryLink(doc, "Cancel");
  }

  @ParameterizedTest
  @MethodSource("referenceInvalidArguments")
  void invalidSubmissionReferenceShowsInlineError(AreaOfLaw areaOfLaw, String expectedErrorMessage)
      throws Exception {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("submissionReference", "invalid-reference-with-hyphen");

    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    form.setAreaOfLaw(areaOfLaw);
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
    assertFormFieldHasErrorMessage(document, expectedErrorMessage);
  }

  @ParameterizedTest
  @MethodSource("referenceRequiredArguments")
  void submissionReferenceNotSelectedShowsInlineError(
      AreaOfLaw areaOfLaw, String expectedErrorMessage) throws Exception {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("office1");
    form.setAreaOfLaw(areaOfLaw);
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
    assertFormFieldHasInlineErrorMessage(document, expectedErrorMessage);
  }

  private static Stream<Arguments> referenceRequiredArguments() {
    return Stream.of(
        of(CRIME_LOWER, "Enter a crime schedule number"),
        of(LEGAL_HELP, "Enter a civil submission reference"),
        of(MEDIATION, "Enter a mediation submission reference"));
  }

  private static Stream<Arguments> referenceInvalidArguments() {
    return Stream.of(
        of(
            CRIME_LOWER,
            "Crime schedule number must be a maximum of 20 characters and contain only letters, numbers and forward slashes"),
        of(
            LEGAL_HELP,
            "Civil submission reference must be a maximum of 20 characters and contain only letters, numbers and forward slashes"),
        of(
            MEDIATION,
            "Mediation submission reference must be a maximum of 20 characters and contain only letters, numbers and forward slashes"));
  }
}
