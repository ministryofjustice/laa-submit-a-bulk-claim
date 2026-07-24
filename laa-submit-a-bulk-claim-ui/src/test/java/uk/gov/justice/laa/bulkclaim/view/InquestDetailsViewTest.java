package uk.gov.justice.laa.bulkclaim.view;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.controller.InquestDetailsController;
import uk.gov.justice.laa.bulkclaim.dto.inquest.InquestDepartment;
import uk.gov.justice.laa.bulkclaim.service.InquestClaimService;
import uk.gov.justice.laa.bulkclaim.service.InquestDepartmentService;
import uk.gov.justice.laa.bulkclaim.validation.InquestDetailsFormValidator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@WebMvcTest(InquestDetailsController.class)
class InquestDetailsViewTest extends ViewTestBase {

  @MockitoBean DataClaimsRestClient client;
  @MockitoBean InquestDepartmentService departmentService;
  @MockitoBean InquestClaimService inquestClaimService;
  @MockitoBean InquestDetailsFormValidator formValidator;

  InquestDetailsViewTest() {
    mapping = "/submissions/" + submissionId + "/claims/" + claimId + "/inquest-details";
  }

  @Test
  void repeatableInterestedOrganisationsUseAddAnotherAndDepartmentAutocomplete() {
    when(client.getSubmission(submissionId))
        .thenReturn(
            Mono.just(
                new SubmissionResponse()
                    .submissionId(submissionId)
                    .areaOfLaw(AreaOfLaw.LEGAL_HELP)
                    .status(SubmissionStatus.READY_FOR_SUBMISSION)));
    when(client.getSubmissionClaim(submissionId, claimId))
        .thenReturn(
            Mono.just(new ClaimResponse().id(claimId.toString()).matterTypeCode("INQUEST")));
    when(client.getClaimInquestData(claimId)).thenReturn(ResponseEntity.notFound().build());
    when(inquestClaimService.status(
            claimId, "INQUEST", AreaOfLaw.LEGAL_HELP, SubmissionStatus.READY_FOR_SUBMISSION))
        .thenReturn(InquestClaimService.Status.INCOMPLETE);
    when(departmentService.getActiveDepartments())
        .thenReturn(
            List.of(
                new InquestDepartment("MOJ", "Ministry of Justice", 1, true),
                new InquestDepartment("DHSC", "Department of Health and Social Care", 2, true)));

    var document = renderDocument();

    var departments = document.selectFirst("#interested-government-departments");
    assertThat(departments).isNotNull();
    assertThat(departments.attr("data-module")).isEqualTo("moj-add-another");
    assertThat(departments.select("select[data-module=make-autocomplete]")).hasSize(1);
    assertThat(departments.select("option[value=MOJ]")).hasSize(1);
    assertThat(departments.select("option[value=DHSC]")).hasSize(1);

    var authorities = document.selectFirst("#interested-public-authorities");
    assertThat(authorities).isNotNull();
    assertThat(authorities.attr("data-module")).isEqualTo("moj-add-another");
    assertThat(authorities.select(".moj-add-another__item")).hasSize(1);
    assertThat(authorities.text()).contains("Add another interested public authority");
    assertThat(authorities.text()).doesNotContain("up to 3");
  }
}
