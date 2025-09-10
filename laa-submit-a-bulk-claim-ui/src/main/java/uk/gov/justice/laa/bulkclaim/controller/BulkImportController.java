package uk.gov.justice.laa.bulkclaim.controller;

import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_DATE_TIME;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.SUBMISSION_ID;
import static uk.gov.justice.laa.bulkclaim.constants.SessionConstants.UPLOADED_FILENAME;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.justice.laa.bulkclaim.dto.FileUploadForm;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.bulkclaim.validation.BulkImportFileValidator;
import uk.gov.justice.laa.bulkclaim.validation.BulkImportFileVirusValidator;
import uk.gov.justice.laa.dstew.payments.claimsdata.model.CreateBulkSubmission201Response;

/** Controller for handling the bulk upload requests. */
@Slf4j
@RequiredArgsConstructor
@Controller
public class BulkImportController {

  public static final String FILE_UPLOAD_FORM_MODEL_ATTR = "fileUploadForm";

  private final BulkImportFileValidator bulkImportFileValidator;
  private final BulkImportFileVirusValidator bulkImportFileVirusValidator;
  private final DataClaimsRestService dataClaimsRestService;

  /**
   * Renders the upload page.
   *
   * @param model the model to be populated with data
   * @param oidcUser the authenticated user principal
   * @return the upload page
   */
  @GetMapping("/")
  public String showUploadPage(
      Model model, @AuthenticationPrincipal OidcUser oidcUser, SessionStatus sessionStatus) {

    // Clear the session due to new submission
    sessionStatus.setComplete();

    // Always ensure there's a form object in the model if not already present
    if (!model.containsAttribute(FILE_UPLOAD_FORM_MODEL_ATTR)) {
      model.addAttribute(FILE_UPLOAD_FORM_MODEL_ATTR, new FileUploadForm(null));
    }

    return "pages/upload";
  }

  /**
   * Performs a bulk upload for the given file.
   *
   * @param fileUploadForm the file to be uploaded
   * @param model the model to be populated with data
   * @param oidcUser the authenticated user principal
   * @return the submission page
   */
  @PostMapping("/upload")
  public String performUpload(
      Model model,
      @ModelAttribute(FILE_UPLOAD_FORM_MODEL_ATTR) FileUploadForm fileUploadForm,
      BindingResult bindingResult,
      @AuthenticationPrincipal OidcUser oidcUser,
      RedirectAttributes redirectAttributes) {

    bulkImportFileValidator.validate(fileUploadForm, bindingResult);
    if (bindingResult.hasErrors()) {
      return showErrorOnUpload(fileUploadForm, bindingResult, redirectAttributes);
    }

    bulkImportFileVirusValidator.validate(fileUploadForm, bindingResult);
    if (bindingResult.hasErrors()) {
      return showErrorOnUpload(fileUploadForm, bindingResult, redirectAttributes);
    }

    try {
      ResponseEntity<CreateBulkSubmission201Response> responseEntity =
          dataClaimsRestService
              .upload(fileUploadForm.file(), oidcUser.getPreferredUsername())
              .block();
      CreateBulkSubmission201Response bulkSubmissionResponse = responseEntity.getBody();
      log.info(
          "Claims API Upload response bulk submission UUID: {}",
          bulkSubmissionResponse.getBulkSubmissionId());
      redirectAttributes.addFlashAttribute(
          SUBMISSION_ID, bulkSubmissionResponse.getSubmissionIds().getFirst());
      redirectAttributes.addFlashAttribute(
          UPLOADED_FILENAME, fileUploadForm.file().getOriginalFilename());
      redirectAttributes.addFlashAttribute(SUBMISSION_DATE_TIME, LocalDateTime.now());
      return "redirect:/import-in-progress";
    } catch (Exception e) {
      log.error("Failed to upload file to Claims API with message: {}", e.getMessage());
      bindingResult.reject("bulkImport.validation.uploadFailed");
      return showErrorOnUpload(fileUploadForm, bindingResult, redirectAttributes);
    }
  }

  /**
   * Redirects back to the upload page with the errors.
   *
   * @param bindingResult binding result of errors
   * @return redirect back to upload page
   */
  private String showErrorOnUpload(
      FileUploadForm fileUploadForm,
      BindingResult bindingResult,
      RedirectAttributes redirectAttributes) {

    redirectAttributes.addFlashAttribute(FILE_UPLOAD_FORM_MODEL_ATTR, fileUploadForm);
    redirectAttributes.addFlashAttribute(
        "org.springframework.validation.BindingResult.fileUploadForm", bindingResult);
    return "redirect:/";
  }
}
