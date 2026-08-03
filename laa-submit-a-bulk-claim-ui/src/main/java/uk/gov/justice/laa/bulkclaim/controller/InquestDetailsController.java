package uk.gov.justice.laa.bulkclaim.controller;

import java.util.UUID;
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
import uk.gov.justice.laa.bulkclaim.service.InquestClaimService;
import uk.gov.justice.laa.bulkclaim.service.InquestDepartmentService;
import uk.gov.justice.laa.bulkclaim.validation.InquestDetailsFormValidator;
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
  private final InquestClaimService inquestClaimService;
  private final InquestDetailsFormValidator formValidator;

  public InquestDetailsController(
      DataClaimsRestClient dataClaimsRestClient,
      InquestDepartmentService inquestDepartmentService,
      InquestClaimService inquestClaimService,
      InquestDetailsFormValidator formValidator) {
    this.dataClaimsRestClient = dataClaimsRestClient;
    this.inquestDepartmentService = inquestDepartmentService;
    this.inquestClaimService = inquestClaimService;
    this.formValidator = formValidator;
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
    formValidator.validate(form, errors);
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
    var status =
        inquestClaimService.status(
            claimId, claim.getMatterTypeCode(), submission.getAreaOfLaw(), submission.getStatus());
    if (status != InquestClaimService.Status.INCOMPLETE) {
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
