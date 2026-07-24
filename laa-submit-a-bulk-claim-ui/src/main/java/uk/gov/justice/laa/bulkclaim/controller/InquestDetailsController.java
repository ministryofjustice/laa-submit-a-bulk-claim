package uk.gov.justice.laa.bulkclaim.controller;

import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.justice.laa.bulkclaim.client.DataClaimsRestClient;
import uk.gov.justice.laa.bulkclaim.dto.inquest.InquestDetailsForm;
import uk.gov.justice.laa.bulkclaim.service.InquestDepartmentService;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.AreaOfLaw;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.ClaimResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionResponse;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.SubmissionStatus;

@Controller
@SessionAttributes("inquestDetailsForm")
public class InquestDetailsController {

  private static final String ACTOR_USER_ID = "Submit-a-bulk-claim";
  private static final String VIEW = "pages/inquest-details";

  private final DataClaimsRestClient dataClaimsRestClient;
  private final InquestDepartmentService inquestDepartmentService;
  private final Set<String> inquestMatterTypeCodes;
  private final Set<String> optionalFields;

  public InquestDetailsController(
      DataClaimsRestClient dataClaimsRestClient,
      InquestDepartmentService inquestDepartmentService,
      @Value("${app.inquest.matter-type-codes:INQUEST}") Set<String> inquestMatterTypeCodes,
      @Value("${app.inquest.optional-fields:}") Set<String> optionalFields) {
    this.dataClaimsRestClient = dataClaimsRestClient;
    this.inquestDepartmentService = inquestDepartmentService;
    this.inquestMatterTypeCodes = inquestMatterTypeCodes;
    this.optionalFields = optionalFields;
  }

  @ModelAttribute
  public InquestDetailsForm inquestDetailsForm() {
    return new InquestDetailsForm();
  }

  @GetMapping("/submissions/{submissionId}/claims/{claimId}/inquest-details")
  public String show(
      @PathVariable UUID submissionId,
      @PathVariable UUID claimId,
      @ModelAttribute InquestDetailsForm form,
      Model model) {
    requireOpenInquestClaim(submissionId, claimId);
    form.clear();
    findInquestData(claimId).ifPresent(form::populate);
    populateModel(model, submissionId, claimId);
    return VIEW;
  }

  @PostMapping("/submissions/{submissionId}/claims/{claimId}/inquest-details")
  public String save(
      @PathVariable UUID submissionId,
      @PathVariable UUID claimId,
      @ModelAttribute InquestDetailsForm form,
      BindingResult errors,
      Model model,
      RedirectAttributes redirectAttributes) {
    if (!isOpenDraft(submissionId)) {
      return redirectToSubmission(submissionId);
    }
    requireOpenInquestClaim(submissionId, claimId);
    validate(form, errors);
    if (errors.hasErrors()) {
      populateModel(model, submissionId, claimId);
      return VIEW;
    }

    var request = form.toWriteRequest(ACTOR_USER_ID);
    if (findInquestData(claimId).isPresent()) {
      dataClaimsRestClient.replaceClaimInquestData(claimId, request);
    } else {
      dataClaimsRestClient.createClaimInquestData(claimId, request);
    }
    redirectAttributes.addFlashAttribute("inquestDetailsSaved", true);
    return redirectToSubmission(submissionId);
  }

  private ClaimResponse requireOpenInquestClaim(UUID submissionId, UUID claimId) {
    SubmissionResponse submission = requireSubmission(submissionId);
    if (submission.getStatus() != SubmissionStatus.READY_FOR_SUBMISSION) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Submission is no longer a draft");
    }
    ClaimResponse claim =
        dataClaimsRestClient
            .getSubmissionClaim(submissionId, claimId)
            .blockOptional()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    if (submission.getAreaOfLaw() != AreaOfLaw.LEGAL_HELP
        || !inquestMatterTypeCodes.contains(claim.getMatterTypeCode())) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    return claim;
  }

  private boolean isOpenDraft(UUID submissionId) {
    return requireSubmission(submissionId).getStatus() == SubmissionStatus.READY_FOR_SUBMISSION;
  }

  private SubmissionResponse requireSubmission(UUID submissionId) {
    return dataClaimsRestClient
        .getSubmission(submissionId)
        .blockOptional()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  private void validate(InquestDetailsForm form, BindingResult errors) {
    rejectBlank("deceasedForename", form.getDeceasedForename(), errors);
    rejectBlank("deceasedSurname", form.getDeceasedSurname(), errors);
    rejectNull("deceasedDateOfBirth", form.getDeceasedDateOfBirth(), errors);
    rejectNull("deceasedDateOfDeath", form.getDeceasedDateOfDeath(), errors);
    rejectBlank("coronersInquestReference", form.getCoronersInquestReference(), errors);
    rejectEmpty("interestedDepartmentCodes", form.getInterestedDepartmentCodes(), errors);
    if (!optionalFields.contains("interestedPublicAuthorities")
        && (form.getInterestedPublicAuthorities() == null
            || form.getInterestedPublicAuthorities().stream().allMatch(String::isBlank))) {
      errors.rejectValue("interestedPublicAuthorities", "inquest.mandatory", "Enter a value");
    }
  }

  private void rejectBlank(String field, String value, BindingResult errors) {
    if (!optionalFields.contains(field) && (value == null || value.isBlank())) {
      errors.rejectValue(field, "inquest.mandatory", "Enter a value");
    }
  }

  private void rejectNull(String field, Object value, BindingResult errors) {
    if (!optionalFields.contains(field) && value == null) {
      errors.rejectValue(field, "inquest.mandatory", "Enter a value");
    }
  }

  private void rejectEmpty(String field, java.util.Collection<?> value, BindingResult errors) {
    if (!optionalFields.contains(field) && (value == null || value.isEmpty())) {
      errors.rejectValue(field, "inquest.mandatory", "Enter a value");
    }
  }

  private void populateModel(Model model, UUID submissionId, UUID claimId) {
    model.addAttribute("departments", inquestDepartmentService.getActiveDepartments());
    model.addAttribute("submissionId", submissionId);
    model.addAttribute("claimId", claimId);
  }

  private java.util.Optional<uk.gov.justice.laa.bulkclaim.dto.inquest.ClaimInquestData>
      findInquestData(UUID claimId) {
    try {
      return java.util.Optional.ofNullable(
          dataClaimsRestClient.getClaimInquestData(claimId).getBody());
    } catch (WebClientResponseException.NotFound exception) {
      return java.util.Optional.empty();
    }
  }

  private String redirectToSubmission(UUID submissionId) {
    return "redirect:/submission/" + submissionId;
  }
}
