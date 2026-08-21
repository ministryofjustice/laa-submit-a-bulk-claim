package uk.gov.justice.laa.payments.submit.view.nilsubmission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw.MEDIATION;
import static uk.gov.justice.laa.payments.submit.controller.ControllerTestHelper.OIDC_USER;

import java.util.List;
import java.util.UUID;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.justice.laa.payments.submit.controller.nilsubmission.NilSubmissionsSummaryController;
import uk.gov.justice.laa.payments.submit.dto.NilSubmissionResult;
import uk.gov.justice.laa.payments.submit.dto.submission.NilSubmissionForm;
import uk.gov.justice.laa.payments.submit.service.NilSubmissionService;
import uk.gov.justice.laa.payments.submit.view.ViewTestBase;

@WebMvcTest(NilSubmissionsSummaryController.class)
class NilSubmissionSummaryViewTest extends ViewTestBase {

  @MockitoBean private NilSubmissionService nilSubmissionService;

  NilSubmissionSummaryViewTest() {
    this.mapping = "/nil-submission/summary-details";
  }

  @Test
  void summaryPageShowsExpectedContent() {
    session.setAttribute("nilSubmissionForm", buildSessionForm());

    var doc = renderDocument();

    assertPageHasTitle(doc, "Create a nil submission");
    assertPageDoesNotHaveBackLink(doc);
    assertPageHasHint(doc, "nil-submission-hint", "Create a nil submission");
    assertPageHasHeading(doc, "Check your nil submission");

    var summaryList = getFirstSummaryList(doc);
    assertThat(summaryList).hasSize(4);
    assertRowContainsValuesThenLink(
        summaryList.get(0), "Office account number", "Change", "/nil-submission/office", "0P322F");
    assertRowContainsValuesThenLink(
        summaryList.get(1), "Area of law", "Change", "/nil-submission/areaoflaw", "Mediation");
    assertRowContainsValuesThenLink(
        summaryList.get(2), "Submission period", "Change", "/nil-submission/period", "OCT-2025");
    assertRowContainsValuesThenLink(
        summaryList.get(3),
        "Mediation submission reference",
        "Change",
        "/nil-submission/reference",
        "REF-123");

    assertThat(doc.select(".moj-alert--error")).isEmpty();
    assertThat(doc.select("#messages-errors-heading")).isEmpty();

    assertPageHasPrimaryButton(doc, "Submit");
    assertPageHasSecondaryLink(doc, "Cancel");
  }

  @Test
  void postSummaryWithErrorsShowsBannerAndErrorList() throws Exception {
    session.setAttribute("nilSubmissionForm", buildSessionForm());
    when(nilSubmissionService.createSubmission(any(), any()))
        .thenReturn(
            new NilSubmissionResult(
                UUID.fromString("00000000-0000-0000-0000-0000000000a2"),
                List.of("First validation error", "Second validation error")));

    var response =
        mockMvc
            .perform(
                post(mapping).with(csrf()).with(oidcLogin().oidcUser(OIDC_USER)).session(session))
            .andReturn()
            .getResponse();

    assertThat(response.getStatus()).isEqualTo(200);
    var doc = Jsoup.parse(response.getContentAsString());

    assertPageHasHeading(doc, "Check your nil submission");
    assertThat(doc.select(".moj-alert--error")).isNotEmpty();
    assertPageHasContent(doc, "2 errors were found with your submission");
    assertPageHasContent(doc, "Resolve the errors and try again.");
    assertThat(selectFirst(doc, "#messages-errors-heading").text())
        .isEqualTo("2 submission errors");
    assertThat(selectFirst(doc, ".govuk-table__header").text()).isEqualTo("Messages");
    assertThat(selectFirst(doc, "#message-error-0 .govuk-table__cell").text())
        .isEqualTo("First validation error");
    assertThat(selectFirst(doc, "#message-error-1 .govuk-table__cell").text())
        .isEqualTo("Second validation error");

    assertPageHasPrimaryButton(doc, "Submit");
    assertPageHasSecondaryLink(doc, "Cancel");
  }

  private static NilSubmissionForm buildSessionForm() {
    NilSubmissionForm form = new NilSubmissionForm();
    form.setOffice("0P322F");
    form.setOfficeCount(2);
    form.setAreaOfLaw(MEDIATION);
    form.setSubmissionPeriod("OCT-2025");
    form.setSubmissionReference("REF-123");
    return form;
  }
}
