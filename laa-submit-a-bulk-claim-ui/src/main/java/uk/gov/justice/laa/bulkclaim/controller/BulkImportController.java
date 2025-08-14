package uk.gov.justice.laa.bulkclaim.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uk.gov.justice.laa.bulkclaim.dto.FileUploadForm;
import uk.gov.justice.laa.bulkclaim.helper.ProviderHelper;
import uk.gov.justice.laa.bulkclaim.service.claims.DataClaimsRestService;
import uk.gov.justice.laa.bulkclaim.validation.BulkImportFileValidator;
import uk.gov.justice.laa.bulkclaim.validation.BulkImportFileVirusValidator;
import uk.gov.justice.laa.claims.model.CreateBulkSubmission201Response;

/** Controller for handling the bulk upload requests. */
@Slf4j
@RequiredArgsConstructor
@Controller
public class BulkImportController {

  public static final String FILE_UPLOAD_FORM_MODEL_ATTR = "fileUploadForm";

  private final ProviderHelper providerHelper;
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
  public String showUploadPage(Model model, @AuthenticationPrincipal OidcUser oidcUser) {

    // Always ensure there's a form object in the model if not already present
    if (!model.containsAttribute(FILE_UPLOAD_FORM_MODEL_ATTR)) {
      model.addAttribute(FILE_UPLOAD_FORM_MODEL_ATTR, new FileUploadForm(null));
    }

    try {
      providerHelper.populateProviders(model, oidcUser.getName());
    } catch (HttpClientErrorException e) {
      log.error("HTTP client error fetching providers with message: {} ", e.getMessage());
      if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
        return "pages/upload-forbidden";
      } else {
        return "error";
      }
    } catch (Exception e) {
      log.error("Error connecting to Provider API with message: {} ", e.getMessage());
      return "error";
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
          dataClaimsRestService.upload(fileUploadForm.file()).block();
      CreateBulkSubmission201Response bulkSubmissionResponse = responseEntity.getBody();
      log.info(
          "Claims API Upload response submission UUID: {}",
          bulkSubmissionResponse.getBulkSubmissionId());
      redirectAttributes.addFlashAttribute(
          "bulkSubmissionId", bulkSubmissionResponse.getBulkSubmissionId());
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
