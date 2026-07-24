package uk.gov.justice.laa.bulkclaim.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ConcurrentModel;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import reactor.core.publisher.Mono;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.inquest.ClaimInquestData;
import uk.gov.justice.laa.bulkclaim.dto.inquest.ClaimInquestDataWrite;
import uk.gov.justice.laa.bulkclaim.dto.inquest.InquestDepartment;
import uk.gov.justice.laa.bulkclaim.dto.inquest.InquestDetailsForm;
import uk.gov.justice.laa.bulkclaim.service.InquestClaimService;
import uk.gov.justice.laa.bulkclaim.service.InquestDepartmentService;
import uk.gov.justice.laa.bulkclaim.validation.InquestDetailsFormValidator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

class InquestDetailsControllerTest {

  private final DataClaimsRestClient client = Mockito.mock(DataClaimsRestClient.class);
  private final InquestDepartmentService departments = Mockito.mock(InquestDepartmentService.class);
  private final InquestClaimService inquestClaims =
      new InquestClaimService(client, Set.of("INQUEST"));
  private final InquestDetailsController controller =
      new InquestDetailsController(
          client,
          departments,
          inquestClaims,
          new InquestDetailsFormValidator(
              "DECEASED_FORENAME,DECEASED_SURNAME,DECEASED_DATE_OF_BIRTH,"
                  + "DECEASED_DATE_OF_DEATH,CORONERS_INQUEST_REFERENCE,"
                  + "INTERESTED_GOVERNMENT_DEPARTMENT,INTERESTED_PUBLIC_AUTHORITY"));

  @Test
  void populatesReferencesAndExistingClaimData() {
    UUID submissionId = UUID.randomUUID();
    UUID claimId = UUID.randomUUID();
    givenOpenInquestClaim(submissionId, claimId);
    when(departments.getActiveDepartments())
        .thenReturn(List.of(new InquestDepartment("MOJ", "Ministry of Justice", 1, true)));
    when(client.getClaimInquestData(claimId))
        .thenReturn(
            ResponseEntity.ok(
                new ClaimInquestData(
                    "Ada",
                    "Lovelace",
                    LocalDate.of(1815, 12, 10),
                    LocalDate.of(1852, 11, 27),
                    "COR-1",
                    Set.of("MOJ"),
                    List.of("NHS Trust"),
                    "xml",
                    false)));

    ConcurrentModel model = new ConcurrentModel();
    InquestDetailsForm form = controller.inquestDetailsForm();

    String view = controller.show(submissionId, claimId, form, model);

    assertThat(view).isEqualTo("pages/inquest-details");
    assertThat(model.getAttribute("departments")).isEqualTo(departments.getActiveDepartments());
    assertThat(form.getDeceasedForename()).isEqualTo("Ada");
    assertThat(form.getInterestedDepartmentCodes()).containsExactly("MOJ");
    assertThat(form.getInterestedPublicAuthorities()).contains("NHS Trust");
  }

  @Test
  void validationErrorsPreserveEnteredValuesAndDoNotPersist() {
    UUID submissionId = UUID.randomUUID();
    UUID claimId = UUID.randomUUID();
    givenOpenInquestClaim(submissionId, claimId);
    when(departments.getActiveDepartments()).thenReturn(List.of());
    InquestDetailsForm form = new InquestDetailsForm();
    form.setDeceasedForename("Ada");
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(form, "inquestDetailsForm");

    String view =
        controller.save(
            submissionId,
            claimId,
            form,
            errors,
            new ConcurrentModel(),
            new RedirectAttributesModelMap());

    assertThat(view).isEqualTo("pages/inquest-details");
    assertThat(errors.hasFieldErrors()).isTrue();
    assertThat(form.getDeceasedForename()).isEqualTo("Ada");
    verify(client, never()).replaceClaimInquestData(eq(claimId), Mockito.any());
  }

  @Test
  void persistsValidDataAndReturnsToUpdatedDraftReview() {
    UUID submissionId = UUID.randomUUID();
    UUID claimId = UUID.randomUUID();
    givenOpenInquestClaim(submissionId, claimId);
    when(client.getClaimInquestData(claimId)).thenReturn(ResponseEntity.notFound().build());
    InquestDetailsForm form = completeForm();
    BeanPropertyBindingResult errors = new BeanPropertyBindingResult(form, "inquestDetailsForm");

    String view =
        controller.save(
            submissionId,
            claimId,
            form,
            errors,
            new ConcurrentModel(),
            new RedirectAttributesModelMap());

    ArgumentCaptor<ClaimInquestDataWrite> request =
        ArgumentCaptor.forClass(ClaimInquestDataWrite.class);
    verify(client).createClaimInquestData(eq(claimId), request.capture());
    assertThat(request.getValue().interestedDepartmentCodes())
        .containsExactlyInAnyOrder("MOJ", "DHSC");
    assertThat(request.getValue().interestedPublicAuthorities()).containsExactly("NHS Trust");
    assertThat(view).isEqualTo("redirect:/submission/" + submissionId);
  }

  @Test
  void closedSubmissionCannotBeMutated() {
    UUID submissionId = UUID.randomUUID();
    UUID claimId = UUID.randomUUID();
    when(client.getSubmission(submissionId))
        .thenReturn(
            Mono.just(
                new SubmissionResponse()
                    .submissionId(submissionId)
                    .areaOfLaw(AreaOfLaw.LEGAL_HELP)
                    .status(SubmissionStatus.VALIDATION_SUCCEEDED)));

    String view =
        controller.save(
            submissionId,
            claimId,
            completeForm(),
            new BeanPropertyBindingResult(completeForm(), "inquestDetailsForm"),
            new ConcurrentModel(),
            new RedirectAttributesModelMap());

    assertThat(view).isEqualTo("redirect:/submission/" + submissionId);
    verify(client, never()).createClaimInquestData(eq(claimId), Mockito.any());
    verify(client, never()).replaceClaimInquestData(eq(claimId), Mockito.any());
  }

  @Test
  void completeXmlClaimCannotOpenManualJourney() {
    UUID submissionId = UUID.randomUUID();
    UUID claimId = UUID.randomUUID();
    givenOpenInquestClaim(submissionId, claimId);
    when(client.getClaimInquestData(claimId))
        .thenReturn(
            ResponseEntity.ok(
                new ClaimInquestData(null, null, null, null, null, null, null, "xml", true)));

    assertThatThrownBy(
            () ->
                controller.show(
                    submissionId, claimId, new InquestDetailsForm(), new ConcurrentModel()))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("404");
  }

  private void givenOpenInquestClaim(UUID submissionId, UUID claimId) {
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
  }

  private InquestDetailsForm completeForm() {
    InquestDetailsForm form = new InquestDetailsForm();
    form.setDeceasedForename("Ada");
    form.setDeceasedSurname("Lovelace");
    form.setDeceasedDateOfBirth(LocalDate.of(1815, 12, 10));
    form.setDeceasedDateOfDeath(LocalDate.of(1852, 11, 27));
    form.setCoronersInquestReference("COR-1");
    form.setInterestedDepartmentCodes(Set.of("MOJ", "DHSC"));
    form.setInterestedPublicAuthorities(List.of("NHS Trust"));
    return form;
  }
}
